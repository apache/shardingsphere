+++
title = "Authorization"
weight = 1
+++

Authorization configuration provided for users who can connect to ShardingSphere-Proxy. Users can be granted different authorities.

## Background

ShardingSphere-Proxy uses `authority` to configure user and authorization information.

Thanks to ShardingSphere's pluggable architecture, Proxy provides two levels of privilege providers, namely: 

- `ALL_PERMITTED`: grant all authorities by default without authentication.
- `DATABASE_PERMITTED`: grant users the authority to specify a logical database, mapped through `user-database-mappings`.

The administrator can choose which privilege provider to use as needed when configuring `authority`. 

## Parameter

```yaml
authority:
  users:
    - user: # Specify the username, and authorized host for logging in to the compute node. Format: <username>@<hostname>. When the hostname is % or an empty string, it indicates that the authorized host is not limited.
      password: # Password
  privilege:
    type: # Privilege provider type. The default value is ALL_PERMITTED.
```

## Sample

### ALL_PERMITTED
```yaml
authority:
  users:
    - user: root@localhost
      password: root
    - user: my_user
      password: pwd
  privilege:
    type: ALL_PERMITTED
```

The above configuration indicates: 
- The user `root` can connect to Proxy only through `localhost`, and the password is `root`.
- The user `my_user` can connect to Proxy through any host, and the password is `pwd`.
- The `privilege` type is `ALL_PERMITTED`, which indicates that users are granted all authorities by default without authentication.

### DATABASE_PERMITTED
```yaml
authority:
  users:
    - user: root@localhost
      password: root
    - user: my_user
      password: pwd
  privilege:
    type: DATABASE_PERMITTED
    props:
      user-database-mappings: root@localhost=sharding_db, root@localhost=test_db, my_user@=sharding_db
```

The above configuration indicates: 
- The `privilege` type is `DATABASE_PERMITTED`, which indicates that users are granted database-level authority and configuration is needed.
- The user `root` can connect to Proxy only through `localhost` and can access `sharding_db` and `test_db`.
- The user `my_user` can connect to Proxy through any host and can access `sharding_db`.

## Related References

Please refer to [Authority Provider](/en/dev-manual/proxy) for specific implementation of authority provider.
