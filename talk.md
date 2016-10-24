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
- Typical solutions can be:
    1) complex because data must come from multiple sources
    2) high latency (i.e. slow) because of many inefficient queries
- Materialized views provide a solution to both of these problems:
    1) single, simple key-value lookup, where value contains all required information
    2) key-value lookup is fast, ideally O(1) or maybe O(log n)
- However, MVs have their own problem: how do you get data into them from multiple sources?

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
