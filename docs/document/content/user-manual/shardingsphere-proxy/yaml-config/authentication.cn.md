+++
title = "权限"
weight = 1
+++

用于配置登录计算节点的初始用户，和存储节点数据授权。

## 配置项说明

```yaml
rules:
  - !AUTHORITY
    users:
      - # 用于登录计算节点的用户名，授权主机和密码的组合。格式：<username>@<hostname>:<password>，hostname 为 % 或空字符串表示不限制授权主机
    provider:
      type: # 存储节点数据授权的权限提供者类型，缺省值为 ALL_PERMITTED
```

## 配置示例

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
以上配置表示：
- root 用户从任意主机连接时，可访问 `sharding_db`。
- root 用户从任意主机连接时，可访问 `test_db` 。
- my_user 用户仅当从 127.0.0.1 连接时，可访问 `sharding_db`。

权限提供者具体实现可以参考 [权限提供者](/cn/dev-manual/proxy)。
