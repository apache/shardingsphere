+++
title = "使用"
weight = 2
chapter = true
+++

本章节将结合 DistSQL 的语法，并以实战的形式分别介绍如何使用 DistSQL 管理分布式数据库下的资源和规则。

## 前置工作

以 MySQL 为例，其他数据库可直接替换。

1. 启动 MySQL 服务；
2. 创建待注册资源的 MySQL 数据库；
3. 在 MySQL 中为 ShardingSphere-Proxy 创建一个拥有创建权限的角色或者用户；
4. 启动 ZooKeeper 服务；
5. 添加 `mode` 和 `authentication` 配置参数到 `server.yaml`；
6. 启动 ShardingSphere-Proxy；
7. 通过应用程序或终端连接到 ShardingSphere-Proxy；

## 创建数据库

1. 创建逻辑库。

```sql
CREATE DATABASE foo_db;
```

2. 使用新创建的逻辑库。

```sql
USE foo_db;
```

## 资源操作

详见具体规则示例。

## 规则操作

详见具体规则示例。

## 注意事项

1. 当前, `DROP DATABASE` 只会移除 `逻辑的分布式数据库`，不会删除用户真实的数据库；
2. `DROP TABLE` 会将逻辑分片表和数据库中真实的表全部删除；
3. `CREATE DATABASE` 只会创建 `逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库。
