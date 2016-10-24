*I will be using this project in an [upcoming talk at Prairie.Code()](http://prairiecode.amegala.com/sessions/updating-materialized-views-and-caches-using-kafka).*

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
    - # lists (maybe omit this? or add later?)
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

## Impl based on Kafka Streams

- Other services do their own thing, and export their data as Kafka topics
  - Two different types of topics/streams:
    1. Record stream
    2. Changelog stream
  - Message keys and values should either be primitive types or Avro byte arrays
  - Each topic should have message key and value schemas registered in Schema Registry
  - Streams provided by services:
    - User service
      - Changelog stream of (userId, user) messages
      - userId is a String
      - user is a User object serialized using Avro
      - Compacted Kafka topic
      - Any time a user changes, a new message with the full user object is appended to topic
      - This topic will contain every user in the system
      - It will grow unbounded in the number of users
    - Tweet service
      - Changelog stream of (tweetId, tweet) messages
      - This topic will contain every tweet in the system, and will grow unbounded in the number of tweets
        - Likely several orders of magnitude larger than number of users
        - At Twitter scale, this will be a huge topic
        - May actually be infeasible, but let's assume tweets are entities in this domain, and we want all of them in a topic
        - Can create 100s/1000s of partitions to handle scale of data
      - If this was a record stream:
        - Its size would be bounded by retention policy (e.g. all tweets from last 7 or 30 days) 
        - However, it would not contain *all* tweets
        - Would need something (e.g. Kafka Connect) replicating all tweets to long-term storage (e.g. HDFS, Cassandra)
        - However, doing ^^ makes it more difficult for another service to consume *all* tweets
          - Has to get old tweets from long-term storage, then get new tweets from kafka topic
          - The precise transition point from old to new can be tricky, i.e. offsets
    - Follow service
      - Changelog of (followId, follow) messages
      - Follow contains:
        - followerId (user that clicked the Follow button on another user's profile)
        - followeeId (user that was followed)
        - timestamp
      - Could argue this maybe should be record stream, but follows are a core domain concept in Twitter
    - Like service
      - Maybe we say this is a record stream
      - Implies that we lose likes after retention period
      - Could probably argue this either way, but sorta feels like activity stream
      - Messages are (likeId, like)
- How would User Info service use these topics in Kafka Streams?
  - It's essentially replicating all users, and then aggregating counts of tweets, follows and likes for each user
  - Create a KTable from users changelog topic
    - This will replicate all users into local RocksDB state
  - Treat other topics as record streams (even though they are changelogs) and create KStreams from them?
    - We don't need to replicate tweets, follows and likes into local state stores
    - We need to take each new tweet/follow/like and increment a count on our own local user object
    - Feels like a group and reduce...
    - Group by userId
    - ^^ means that all streams need to be repartitioned by userId
      - users changelog stream already partitioned by userId
      - repartition tweets by userId
      - repartition follows by followerId: use this for # following count
      - repartition follows by followeeId: use this for # followers count
      - repartition likes by userId
  - There isn't really an existing high-level dsl operator that does what we want
    - Can't do a KTable-to-multiple-KStream join :(
    - But ^^ is pretty much what we want:
      - Start with KTable[String, User]
      - Join multiple KStreams into that KTable of users
        - KStream[String, Tweet] of tweets, repartitioned by userId that created the tweet
        - KStream[String, Follow] of follows, repartitioned by userId that followed another user
        - KStream[String, Follow] of follows, repartitioned by userId that was followed by another user
        - KStream[String, Like] of likes, repartitioned by userId that liked the tweet
      - Output a KTable[String, UserInformation]
        - One entry for each user in original KTable[String, User]
        - UserInformation is essentially User + counts
        - On new msg from KTable[String, User]: update UserInformation in local state, update(oldState: UserInformation, newUser: User): UserInformation
        - On new msg from one of the KStream[String, V]: update UserInformation in local state, update(oldState: UserInformation, newValue: V): UserInformation
- Could make this a generic operator
  - join(
      table:   KTable[K, V0],  update0: (Option[V], V0) => V,
      stream1: KStream[K, V1], update1: (Option[V], V1) => V,
      stream2: KStream[K, V2], update2: (Option[V], V2) => V,
      stream3: KStream[K, V3], update3: (Option[V], V3) => V,
      ...): KTable[K, V]
  - That type signature is tricky because it's essentially varargs of KStream[K, Vn] where Vn can vary for each arg
  - Might be more complex to impl than above... but that's essentially what we want to do
- The final KTable[String, UserInformation] gets changelogged to a Kafka topic
  - This is the data that the microservice http endpoint needs to provide
    - GET /users/:userId
    - return the UserInformation for userId in 200 JSON, or 404 if not found
  - The microservice needs to consume Kafka topic on startup, and then just serve key lookups
  - Kafka consumer stores state in local RocksDB
  - Use local RocksDB in http endpoint
- Why not just do the join processing in http microservice?
  - Logic is then repeated in every microservice instance
  - This does not scale out: cannot parallelize join processing by partitioning into multiple microservice instances
  - Better to decouple:
    - One service to join the streams and produce resulting user info data
    - One service to make user info data available via http
- HTTP microservice can be partitioned
  - Partition user info state across multiple http service instances
  - Then need a router in front of these services to route http requests to correct instance
  - This is only needed when size of user info RocksDB exceeds disk available to single computer
  - At 1KB per user:
    - 100GB = 100M users
    - 1 TB = 1B users
    - 10TB = 10B users
- HTTP microservice can be replicated
  - If http reqs/sec exceed capacity of single http service instance
  - Then run multiple http service instances
  - Each instance contains same data
  - Need load balancer in front to spread http reqs across instances
    - Round robin?
    - Something more complex?
- Join processing service can scale out
  - Kafka Streams elastically scales out: http://www.confluent.io/blog/elastic-scaling-in-kafka-streams/
  - If join service can't keep up with incoming stream volume, just run more instances of service
  - Can scale up to kafka topic partition count (create more topic partitions after that)
- Can also make HTTP microservice generic:
  - All it does is replicate a Kafka topic into local RocksDB and serve RocksDB values by key
  - ^^ can be very generic
  - Replicate a Kafka topic of (K, V) messages into RocksDB of (K, V) entries
    - Don't even need to deserialize K or V
    - Get byte arrays from Kafka topic
    - Put byte arrays into RocksDB
    - May be a use for different K and V in RocksDB, but not for this use case
  - Expose values in RocksDB via HTTP by key lookup
    - Get key as String from HTTP framework
    - Need String => Array[Byte] conversion to do key lookup
    - Get value from RocksDB as bytes
    - Need Array[Byte] => Json to return in HTTP response
      - The Json type depends on JSON framework being used
      - i.e. Circe
    - Default key serializer
      - keyString.getBytes("UTF-8")
    - Default value deserializer
      - Values are probably usually Avro records
      - And probably usually integrated with Schema Registry
      - So deserialize bytes to Avro
      - Can Avro just generate JSON directly?
        - If so, then just use that
        - If not, then need some Avro => Json converter
    - Of course, let user specify custom String => Array[Byte] and Array[Byte] => Json serdes
    - The rest of the code handles:
      - Receiving HTTP req (at some custom path), extract key String
      - Return 200 OK with JSON or 404
- The Kafka Streams join processing service can also write user info to a cache like Redis
  - Does not output a kafka topic
  - Outputs key/value writes to Redis
  - HTTP service then reads from Redis
  - No RocksDB in HTTP service
- Interactive Queries / Queryable State
  - https://cwiki.apache.org/confluence/display/KAFKA/KIP-67%3A+Queryable+state+for+Kafka+Streams
  - Similar to the approach outlined here, except the Kafka Streams RocksDB state becomes queryable, so the join and http services are merged into a single service
  - Pros
    - User info state only exists in 1 place, instead of 3: join service rocksdb, kafka topic, http service rocksdb
      - But join service's rocksdb store backed by changelog kafka topic
      - Could be 4 for http service if it can't consume state changelog topic and needs another regular topic
    - Only need 1 deployable instead of 2
  - Cons
    - Join and HTTP concerns are coupled
    - Cannot scale join and http independently
    - Have to deploy changes to join and http services together
    - Maturity: still in KIP phase? Is there even a prototype?

- Status
  - what is done?
    - in-memory key-value store
    - serdes using confluent schema registry & avro
    - user info web service: get userId in http request, lookup user in kv store, return user as json in http resp
    - kafka consumer that writes to kv store
    - docker-compose.yml with kafka brokers
    - rocksdb kv store impl
    - serde impls using read schema registry client
    - consume from twitter streaming api, writer tweets & users to kafka topics
    - stream join service that produces user info to kafka topic that http service consumes
    - follow service
    - compute follow counts
  - what to do next?
    - like service
    - compute like counts
    - what about deleting tweets? unfollowing? unliking?
    - fix in-memory kafka test: not shutting down/cleaning up properly
    - generic kv lookup web service and concrete instance for user info