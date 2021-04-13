+++
title = "权限配置"
weight = 2
+++

用于执行登录 Sharding Proxy 的权限验证。
配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录。

```yaml
  users:
  - root@:root # <username>@<hostname>:<password>
  - sharding@%:sharding
```
