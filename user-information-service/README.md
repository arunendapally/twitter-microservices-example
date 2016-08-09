# Overview

Provides fast lookup of complex user information. Essentially a materialized view of users.

Consumes events published by other services and aggregates them together into a comprehensive view of a user.

# User Lookup

An HTTP endpoint that returns JSON.

`GET /users/:userId`

```json
{
  "userId": "12345",
  "username": "zcox",
  TODO...
}
```

Note that this service does not directly expose any public API for modifying data.

# Building the User Materialized View

These are internal implementation details, not a public interface. TODO...
