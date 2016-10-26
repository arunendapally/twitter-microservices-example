# Problem

- Need to build a service that: 
    1) provides a single point of access to data created in many different places
    2) has very low latency requirements
    - Example: 
        - User Information service
        - User fields
        - Aggregates of other user activity
            - Tweets
            - Following other users
            - Being followed by other users
            - Liking other users' tweets
- Standard solutions can be:
    1) complex because data must come from multiple sources
    2) high latency (i.e. slow) because of many inefficient queries
- Materialized views provide a solution to both of these problems:
    1) single, simple key-value lookup, where value contains all required information
    2) key-value lookup is fast, ideally O(1) or maybe O(log n)
- However, MVs have their own problem: how do you get data into them from multiple sources?

# Standard Solutions

- Two parts:
    1. Monolith vs Microservices
    2. Where data is stored and how it's queried
- Focus more on #2

## Normalized Relational Database

- Tables:
    - users
        - user_id
        - username
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
- Getting user for userId is easy: SELECT * FROM users WHERE user_id = ?
- Aggregating counts of a user's tweets, follows, likes, etc 
    - Query multiple other tables
    - DB does a lot of work at query time
        - Increases service response latency
        - Puts load on DB
        - Repeated many times
    - SELECT COUNT(*) FROM tweets WHERE user_id = ?
    - SELECT COUNT(*) FROM follows WHERE follower_id = ?
    - SELECT COUNT(*) FROM follows WHERE followee_id = ?
    - SELECT COUNT(*) FROM likes WHERE user_id = ?

## Normalized Relational Database + Cache

- Read from cache first
    - Fast, simple key lookup
        - TODO Redis examples for above
- If cache has data, then return it
- Otherwise:
    - Read from DB (same SQL as above)
    - Write results to cache
    - Return results
- Problems with this approach:
    - Complexity
        - Now query 2 different data stores, and write to 1 of them
    - Cache misses
        - Still putting load on DB
    - Stale data
        - If cache has TTL, data is stale for that long
        - If service writes to DB and cache, then it's coupled to cache and is more complex
    - Network latency of remote data store
- Can we solve all of these problems?
    - If cache was always updated, we would only have to read from cache
    - Solves complexity: only 1 store to read from, no writes
    - Solves cache misses: current data is always in cache
    - Solves stale data: current data is always in cache

## Updating Cache

- On one side, we have writes to primary data stores
- On the other side, we have a service reading from cache
- Have to connect the two somehow
- Send data changes to Kafka topics
    - Change data capture
    - Application dual-writes (yuck)
- Stream processing consumes topics, eventually writes to cache

# Solution

- All data sources/services publish events to Kafka
    - Types of events
        - Record stream
        - Changelog stream
    - Approaches to publishing events
        - Dual-write
        - Change data capture
        - Single-write (stream only)
- Process these streams to:
    - Compute materialized views
    - Populate materialized views

# Example

# Issues

# Results

# Future Work
