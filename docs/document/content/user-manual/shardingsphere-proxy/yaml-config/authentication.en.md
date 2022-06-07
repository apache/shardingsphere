+++
title = "Authority"
weight = 1
+++

It is used to set up initial user to login compute node, and authority data of storage node.

## Configuration Item Explanation

```yaml
rules:
  - !AUTHORITY
    users:
      - # Username, authorized host and password for compute node. Format: <username>@<hostname>:<password>, hostname is % or empty string means do not care about authorized host
    provider:
      type: # authority provider for storage node, the default value is ALL_PERMITTED
```

## Example

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

### DATABASE_PERMITTED
```yaml
rules:
  - !AUTHORITY
    users:
      - root@:root
      - my_user@:pwd
    provider:
      type: DATABASE_PERMITTED
      props:
        user-database-mappings: root@=sharding_db, root@=test_db, my_user@127.0.0.1=sharding_db
```
The above configuration means:
- The user `root` can access `sharding_db` when connecting from any host
- The user `root` can access `test_db` when connecting from any host
- The user `my_user` can access `sharding_db` only when connected from 127.0.0.1

Refer to [Authority Provider](/en/dev-manual/proxy) for more implementations.
