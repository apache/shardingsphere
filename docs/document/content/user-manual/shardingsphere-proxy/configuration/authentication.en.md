+++
title = "Authentication"
weight = 2
+++

It is used to verify the authentication to log in ShardingSphere-Proxy, which must use correct user name and password after the configuration of them.

```yaml
users:
  - root@:root # <username>@<hostname>:<password>
  - sharding@%:sharding
```
