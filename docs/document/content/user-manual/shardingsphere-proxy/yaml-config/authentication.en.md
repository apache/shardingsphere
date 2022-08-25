+++
title = "Authorization"
weight = 1
+++

Authorization configuration provided for users who can connect to ShardingSphere-Proxy. Users can be granted different authorities.

## Background

ShardingSphere-Proxy uses the global rule, Authority Rule (identified as !AUTHORITY), to configure user and authorization information.

Thanks to ShardingSphere's pluggable architecture, Proxy provides two levels of authority providers, namely: 

- `ALL_PERMITTED`: grant all authorities by default without authentication.
- `DATABASE_PERMITTED`: grant users the authority to specify a logical database, mapped through `user-database-mappings`.

The administrator can choose which authority provider to use as needed when configuring the Authority Rule. 

## Parameter

```yaml
rules:
  - !AUTHORITY
    users:
      - # Specify the username, authorized host, and password for logging in to the compute node. Format: <username>@<hostname>:<password>. When the hostname is % or an empty string, it indicates that the authorized host is not limited.
    provider:
      type: # The authority provider type for storage node. The default value is ALL_PERMITTED.
```

## Sample

### ALL_PERMITTED
```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root
      - my_user@:pwd
    provider:
      type: ALL_PERMITTED
```

The above configuration indicates: 
- The user `root` can connect to Proxy only through [localhost](http://localhost), and the password is `root`.
- The user `my_user` can connect to Proxy through any host, and the password is `pwd`.
- The `provider` type is `ALL_PERMITTED`, which indicates that users are granted all authorities by default without authentication.

### DATABASE_PERMITTED
```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root
      - my_user@:pwd
    provider:
      type: DATABASE_PERMITTED
      props:
        user-database-mappings: root@localhost=sharding_db, root@localhost=test_db, my_user@=sharding_db
```

The above configuration indicates: 
- The `provider` type is `DATABASE_PERMITTED`, which indicates that users are granted database-level authority and configuration is needed.
- The user `root` can connect to Proxy only through [localhost](http://localhost) and can access `sharding_db` and `test_db`.
- The user `my_user` can connect to Proxy through any host and can access `sharding_db`.

## Related References

Please refer to [Authority Provider](/en/dev-manual/proxy) for specific implementation of authority provider.
