+++
title = "权限配置"
weight = 2
+++

用于执行登录 Sharding Proxy 的权限验证。
配置用户名、密码、可访问的数据库后，必须使用正确的用户名、密码才可登录。

```yaml
authentication:
  users:
    root: # 自定义用户名
      password: root # 自定义密码
    sharding: # 自定义用户名
      password: sharding # 自定义密码
      hostname: '%' # 允许访问Sharding Proxy的主机。
```
