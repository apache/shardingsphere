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
      hostname: '%' # Which host can be allowed to access the Sharding Proxy.
```
