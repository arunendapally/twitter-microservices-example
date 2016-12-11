This repository contains code and [slides](https://github.com/zcox/twitter-microservices-example/blob/master/Updating%20Materialized%20Views%20and%20Caches%20Using%20Kafka.pdf) for a [talk at Prairie.Code()](http://prairiecode.amegala.com/sessions/updating-materialized-views-and-caches-using-kafka).


Imagine that we are asked to build a new service that provides a single point of read access to data from multiple sources within our company. Data is created and updated by those other sources, not this new service. We may need to query multiple tables or databases, performing complex (and potentially expensive) joins and aggregations. Other services within our company will obtain this derived data from our new service, using it in various ways.

To provide business value, this new service needs to be low latency, providing very fast response times under non-trivial load (e.g. 95th percentile HTTP response time under 5 msec and max 10 msec at 1000 HTTP requests/sec). Strong, transactional consistency with the other systems of record is not required; we can tolerate data update delays, but they should be bounded (e.g. 95th percentile updates available in this service within 5 sec and max 10 sec). Breaking these SLAs should result in humans being alerted.

To provide a concrete (but imaginary!) example, let's say we work at Twitter, and this new service provides read access to *user information*: various user profile fields that users can edit, along with summary counts of various things related to the user. This User Information Service can then be used by other services, e.g. front-end desktop and mobile services can quickly get user information to display, the HTTP API can get user information to return as JSON, etc.

![](img/twitter-desktop-profile-highlighted.png)
