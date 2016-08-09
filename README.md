# User Information Service

## Requirements

- Get user information
  - Input: userId
  - Output:
    - userId
    - username
    - name
    - bio text
    - location
    - web page url
    - joined date
    - profile image url
    - background image url
    - # tweets
    - # following
    - # followers
    - # likes
    - # lists
  - Performance:
    - Response latency:
      - 95th percentile: 2 msec
      - max: 5 msec
    - Data update latency:
      - 95th percentile: 1 sec
      - max: 10 sec
  - Example clients:
    - Desktop browser user page
    - Desktop browser signed-in home page
    - Mobile browser user page
    - Mobile app user page
    - Various APIs (e.g. https://dev.twitter.com/rest/reference/get/users/show)

## Potential Designs

### Shared Library, Shared Tables

- Shared library that other projects can depend on
  - Code reuse FTW!
  - Tightly coupling independent services FTL
- Query database tables that other services write to
  - User service writes user fields to users table, e.g. name, bio
  - Tweet service writes tweets to tweets table, with user_id foreign key
  - Follow service writes to following table, e.g. source_user_id & source_target_id foreign keys
  - Like service writes to likes table, e.g. user_id foreign key
  - User Info service queries these tables and joins them all by user_id to get counts
  - Fully normalized data FTW
  - Tightly coupled services via shared tables FTL
  - Slow queries FTL
  - Complex queries FTL

### Query-Based Microservices

- Isolated data stores
- User Info service queries other services
  - User service provides user fields
  - Tweet service provides # tweets
  - Follow service provides # following/followers
  - Like service provides # likes
- We just moved queries from DB to other services
- Network latency
- Communication/coordination overhead & complexity
- Coupled to other services (although not as tightly as shared lib/tables)

### Event-Based Microservices

- Services perform domain logic and publish events
  - User service publishes user changes
  - Tweet service publishes tweets
  - Follow service publishes follow/unfollow events
  - Like service publishes like events
- Each service can use whatever data store it wants
- Events are published to Kafka topics
  - Directly produced by service
  - Changelog generated from database or key-value store
- Any service can consume any Kafka topics it wants
  - Replicate data from other services in local store
  - Join data from other services together in interesting ways
  - Join data from other services with data owned by this service
  - Aggregate data to pre-compute information
- User Info service
  - Local key-value store:
    - Key = userId
    - Value = all fields we need to return
  - Consume Kafka topics, update KV store on new message
    - User data changes => update user fields
    - Tweets => inc # tweets
    - Follow/unfollow => inc/dec # followings/followers
    - Likes => inc # likes
  - Data store options:
    - Never have cache misses: data changes pushed into store
    - Never have stale data: data changes pushed into store
    - Eventual consistency
      - This data store is not updated transactionally with original data change
      - This is totally fine given requirements
      - If UI needs RYOW, it can just fake it
    - Redis
      - KV storage
      - Very fast
      - Redis cluster used by multiple instances of User Info service
      - Network latency tho
    - RocksDB
      - Same as Redis, but data is local to instance
      - Super fast lookups

# Example Implementation

- Simulate upstream services using Streaming API as much as possible
- Build User Info service for real

## Simulate User Service

- Each status (i.e. tweet) from Streaming API also contains user information
- We could just send the user info from each received tweet to kafka topic
- Or we could try to be more clever and only send to kafka if user info changed
  - This would require remembering all user info, and checking for change on each tweet

## Simulate Tweet Service

- This is easy, since this is what the Streaming API provides
- Just receive a tweet and publish it to kafka topic

## Simulate Follow Service

- Streaming API does not provide follow/unfollow information
- It does provide # following/followers in user info
  - But we want User Info service to aggregate this itself from follow/unfollow events
- We could have a Follow service that generates follow/unfollow events
  - Keep track of all users seen in streaming api
  - Over time, randomly generate follow/unfollow events between known users
  - Could attempt to match counts from streaming api, or just ignore those

## Simulate Like Service

- Streaming API does not provide like/unlike events
- Generate these randomly over time
  - Only generate like/unlike events for known tweetIds and userIds

## User Info Service using Redis

## User Info Service using RocksDB
