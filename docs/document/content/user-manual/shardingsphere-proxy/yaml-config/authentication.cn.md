+++
title = "权限"
weight = 1
+++

权限配置用于设置能够连接到 ShardingSphere-Proxy 的用户，并可以为他们授予不同的权限。

## 背景信息

在 ShardingSphere-Proxy 中，通过全局规则 Authority Rule （标识为 !AUTHORITY）来配置用户和授权信息。

得益于 ShardingSphere 的可插拔架构，Proxy 提供了两种级别的权限提供者，分别是：

- `ALL_PERMITTED`：授予所有权限，不鉴权；
- `DATABASE_PERMITTED`：为用户授予指定逻辑库的权限，通过 user-database-mappings 进行映射。

在配置 Authority Rule 时，管理员可根据需要选择使用哪一种权限提供者。

## 参数解释

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

以上配置表示：
- 用户 `root`，仅可从 `localhost` 连接 Proxy，密码为 `root`；
- 用户 `my_user`，可以从任意主机连接 Proxy，密码为 `pwd`；
- `provider` 类型为 `ALL_PERMITTED`，表示对用户授予所有权限，不鉴权。

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

以上配置表示：

- `provider` 类型为 `DATABASE_PERMITTED`，表示对用户授予库级别权限，需要配置；
- 用户 `root` 仅可从 `localhost` 主机连接，可访问 `sharding_db` 和 `test_db`；
- 用户 `my_user` 可从任意主机连接，可访问 `sharding_db`。

## 相关参考

权限提供者具体实现可以参考 [权限提供者](/cn/dev-manual/proxy)。
