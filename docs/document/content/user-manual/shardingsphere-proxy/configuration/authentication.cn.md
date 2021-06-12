+++
title = "权限配置"
weight = 2
+++

用于执行登录 Sharding Proxy 的权限验证。
配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录。

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root  # <username>@<hostname>:<password>
      - sharding@:sharding
    provider:
      type: NATIVE
```

hostname 为 `%` 或空字符串，则代表不限制 host。

provider 的 type 必须显式指定，具体实现可以参考 [5.11 Proxy](/cn/dev-manual/proxy)
