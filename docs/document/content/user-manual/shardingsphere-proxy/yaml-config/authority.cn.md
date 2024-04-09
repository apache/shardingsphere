+++
title = "认证和授权"
weight = 1
+++

## 背景信息

在 ShardingSphere-Proxy 中，通过 `authority` 来配置用户的认证和授权信息。

得益于 ShardingSphere 的可插拔架构，Proxy 提供了两种级别的权限提供者，分别是：

- `ALL_PERMITTED`：每个用户都拥有所有权限，无需专门授权；
- `DATABASE_PERMITTED`：为用户授予指定逻辑库的权限，通过 `user-database-mappings` 进行定义。

在配置 `authority` 时，管理员可根据需要选择使用哪一种权限提供者。

## 参数解释

```yaml
authority:
  users:
    - user: # 用于登录计算节点的用户名和授权主机的组合，格式：<username>@<hostname>，hostname 为 % 或空字符串表示不限制授权主机，username 和 hostname 大小写不敏感
      password: # 用户密码
      admin: # 可选项，管理员身份标识。若为 true，该用户拥有最高权限，缺省值为 false
      authenticationMethodName: # 可选项，用于为用户指定密码认证方式
  authenticators: # 可选项，默认不需要配置，Proxy 根据前端协议类型自动选择
    authenticatorName:
      type: # 密码认证类型
  defaultAuthenticator: # 可选项，指定一个 authenticatorName 作为默认的密码认证方式
  privilege:
    type: # 权限提供者类型，缺省值为 ALL_PERMITTED
```

## 配置示例

### 极简配置

```yaml
authority:
  users:
    - user: root@%
      password: root
    - user: sharding
      password: sharding
```

说明：
- 定义了两个用户：`root@%` 和 `sharding`；
- 未定义 `authenticators` 和 `authenticationMethodName`，Proxy 将根据前端协议自动选择;
- 未指定 `privilege type`，采用默认的 `ALL_PERMITTED`。

### 认证配置

自定义认证配置能够满足用户在一些特定场景下的需求。
以 `openGauss` 作为前端协议类型为例，其默认的认证算法为 `scram-sha-256`。
如果用户 `sharding` 需要用旧版本的 psql 客户端（不支持 `scram-sha-256`）连接 Proxy，则管理员可能允许 sharding 使用 md5 方式进行密码认证。
配置方式如下：

```yaml
authority:
  users:
    - user: root@127.0.0.1
      password: root
    - user: sharding
      password: sharding
      authenticationMethodName: md5
  authenticators:
    md5:
      type: MD5
  privilege:
    type: ALL_PERMITTED
```

说明：
- 定义了两个用户：`root@127.0.0.1` 和 `sharding`；
- 为用户 `sharding` 指定了 `MD5` 方式进行密码认证；
- 没有为 `root@127.0.0.1` 指定认证方式，Proxy 将根据前端协议自动选择；
- 指定权限提供者为 `ALL_PERMITTED`。

### 授权配置

#### ALL_PERMITTED

```yaml
authority:
  users:
    - user: root@127.0.0.1
      password: root
    - user: sharding
      password: sharding
  privilege:
    type: ALL_PERMITTED
```

说明：
- 定义了两个用户：`root@127.0.0.1` 和 `sharding`；
- 未定义 `authenticators` 和 `authenticationMethodName`，Proxy 将根据前端协议自动选择;
- 指定权限提供者为 `ALL_PERMITTED`。

#### DATABASE_PERMITTED

```yaml
authority:
  users:
    - user: root@127.0.0.1
      password: root
    - user: sharding
      password: sharding
  privilege:
    type: DATABASE_PERMITTED
    props:
      user-database-mappings: root@127.0.0.1=*, sharding@%=test_db, sharding@%=sharding_db
```

说明：
- 定义了两个用户：`root@127.0.0.1` 和 `sharding`；
- 未定义 `authenticators` 和 `authenticationMethodName`，Proxy 将根据前端协议自动选择；
- 指定权限提供者为 `DATABASE_PERMITTED`，并授权 `root@127.0.0.1` 用户访问所有逻辑库(`*`)，sharding 用户仅能访问 test_db 和 sharding_db。

## 相关参考

权限提供者具体实现可以参考 [权限提供者](/cn/user-manual/shardingsphere-proxy/yaml-config/authority/)。
