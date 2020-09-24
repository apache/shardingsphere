+++
title = "Authentication"
weight = 2
+++

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
authentication:
  users:
    root: # Self-defined username
      password: root # Self-defined password
    sharding: # Self-defined username
      password: sharding # Self-defined password
      authorizedSchemas: sharding_db, primary_replica_replication_db # Schemas authorized to this user, please use commas to connect multiple schemas. Default authorized schemas is all of the schemas.
```
