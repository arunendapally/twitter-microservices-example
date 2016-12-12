This repository contains code and [slides](https://github.com/zcox/twitter-microservices-example/blob/master/Updating%20Materialized%20Views%20and%20Caches%20Using%20Kafka.pdf) for a [talk at Prairie.Code()](http://prairiecode.amegala.com/sessions/updating-materialized-views-and-caches-using-kafka).


Imagine that we are asked to build a new service that provides a single point of read access to data from multiple sources within our company. Data is created and updated by those other sources, not this new service. We may need to query multiple tables or databases, performing complex (and potentially expensive) joins and aggregations. Other services within our company will obtain this derived data from our new service, using it in various ways.

To provide business value, this new service needs to be low latency, providing very fast response times under non-trivial load (e.g. 95th percentile HTTP response time under 5 msec, and max 10 msec, at 1000 HTTP requests/sec). This service may be provide part of a UI, where [fast responses are important for a good experience](https://www.nngroup.com/articles/response-times-3-important-limits/). Transactional consistency with the other systems of record is not required; we can tolerate data update delays, but they should be bounded (e.g. 95th percentile updates available in this service within 5 sec, and max 10 sec). Breaking these SLAs should result in humans being alerted.

To provide a concrete (but imaginary!) example, let's say we work at Twitter, and this new service provides read access to *user information* comprised of various user profile fields that users can edit, along with summary counts of various things related to the user. This User Information Service can then be used by other services, e.g. front-end desktop and mobile services can quickly get user information to display, the HTTP API can get user information to return as JSON, etc.

![](img/twitter-desktop-profile-highlighted.png)

The User Information Service will provide a single HTTP resource: `GET /users/:userId`, which will either return a `404` response if `userId` does not exist, or a `200` response with user information in a JSON object.

```json
{
  "userId": "...",
  "username": "...",
  "name": "...",
  "description": "...",
  "location": "...",
  "webPageUrl": "...",
  "joinedDate": "...",
  "profileImageUrl": "...",
  "backgroundImageUrl": "...",
  "tweetCount": 123,
  "followingCount": 234,
  "followerCount": 345,
  "likeCount": 456
}
```

Let's assume that the data this service needs is stored in a relational database (e.g. Postgres) in normalized tables. (Twitter's actual data storage is probably not like this, but many existing systems that we're all familiar with do follow this standard model, so let's roll with it.)

- users
  - user_id
  - username
  - name
  - description
- tweets
  - tweet_id
  - text
  - user_id
- follows
  - follow_id
  - follower_id
  - followee_id
- likes
  - like_id
  - user_id
  - tweet_id

A classic implementation of this service would likely end up querying the DB directly using SQL.

```
SELECT * FROM users WHERE user_id = ?

SELECT COUNT(*) FROM tweets WHERE user_id = ?

SELECT COUNT(*) FROM follows WHERE follower_id = ?

SELECT COUNT(*) FROM follows WHERE followee_id = ?

SELECT COUNT(*) FROM likes WHERE user_id = ?
```

Many existing services that generate lots of revenue are implemented like this, and if this approach meets all requirements, then great!

However, many developers have discovered problems with this approach over the years. First off, it's somewhat complex: we are performing multiple queries across multiple tables. If our service had different requirements, these aggregations could be much more complex, with grouping and filtering clauses, as well as joins across multiple tables.

Second, it's expensive: these queries are aggregating a (potentially) large number of rows. While I may not have a large number of followers, [Katy Perry has over 90 million](https://twitter.com/katyperry/followers). This puts load on the database, increases response latency of our service, and these aggregations are repeated on every request for the same `userId`.

From an architectural perspective, our new service would be sharing data stores with other services. In the world of microservices, this is generally considered an anit-pattern. The shared data store tightly couples the services together, preventing them from evolving independently.

The standard solution to the above problems is to add a cache, such as Redis. Our service can compute the results of complex queries and store them in the cache, creating a materialized view, which is then easily queried using a simple, fast key lookup. This materialized view cache belongs only to our new service, decoupling it a bit from other services.

However, introducing this cache into our service presents its own problems. While our service can now do a single fast key lookup to get the materialized view from the cache instead of querying the DB, it still has to query the DB and populate the cache on a cache miss, so the complex DB queries remain. When a key is found in the cache, how do we know if the materialized view is up-to-date or stale? We can use a TTL on cache entires to bound staleness, but the lower the TTL, the less effective the cache is.

These problems would be solved if we could just update the materialized views in the cache whenever any data changed in those source tables. Our new service would have no complex DB queries, only simple, fast cache lookups. There would never be cache misses, and the materialized views in the cache would never be stale. How can we accomplish this?

If we have a mechanism to send inserted and updated rows in those tables to Kafka topics, then we can consume those data changes and update the cached materialized views. 
