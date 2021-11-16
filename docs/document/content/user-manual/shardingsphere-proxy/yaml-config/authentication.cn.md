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
      type: # 存储节点数据授权的权限提供者类型
```

## 配置示例

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root
      - my_user@pwd
    provider:
      type: FOO_AUTHORITY_PROVIDER
```

权限提供者具体实现可以参考 [权限提供者](/cn/dev-manual/proxy)。
