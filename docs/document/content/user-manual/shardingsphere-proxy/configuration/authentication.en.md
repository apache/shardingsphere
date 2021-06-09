+++
title = "Authentication"
weight = 2
+++

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root # <username>@<hostname>:<password>
      - sharding@:sharding
    provider:
      type: NATIVE
```

If the hostname is `%` or empty, it means no restrict to the user's host.

The type of the provider must be explicitly specified. Refer to [5.11 Proxy](/en/dev-manual/proxy) for more implementations.
