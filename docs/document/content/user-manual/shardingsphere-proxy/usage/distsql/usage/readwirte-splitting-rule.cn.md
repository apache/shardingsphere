+++
title = "读写分离"
weight = 2
+++

## 使用实战

### 前置工作

1. 启动 MySQL 服务
2. 创建 MySQL 数据库(参考 ShardingProxy 数据源配置规则)
3. 为 ShardingProxy 创建一个拥有创建权限的角色或者用户
4. 启动 Zookeeper 服务 (为了持久化配置)

### 启动 ShardingProxy

1. 添加 `mode` 和 `authentication` 配置参数到 `server.yaml` (请参考相关 example 案例)
2. 启动 ShardingProxy ([相关介绍](/cn/quick-start/shardingsphere-proxy-quick-start/))

### 创建分布式数据库和分片表

1. 连接到 ShardingProxy
2. 创建分布式数据库

```sql
CREATE DATABASE readwrite_splitting_db;
```

3. 使用新创建的数据库

```sql
USE readwrite_splitting_db;
```

4. 配置数据源信息

```sql
ADD RESOURCE write_ds (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
),read_ds (
HOST=127.0.0.1,
PORT=3307,
DB=ds_0,
USER=root,
PASSWORD=root
);
```

5. 创建读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME=random)
);
```

6. 修改读写分离规则

```sql
ALTER READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME=random,PROPERTIES(read_weight='2:0'))
)
```

7. 删除读写分离规则

```sql
DROP READWRITE_SPLITTING RULE group_0;
```

8. 删除数据源

```sql
DROP RESOURCE write_ds,read_ds;
```

9. 删除分布式数据库

```sql
DROP DATABASE readwrite_splitting_db;
```

### 注意事项

1. 当前, `DROP DATABASE` 只会移除`逻辑的分布式数据库`，不会删除用户真实的数据库。
2. `DROP TABLE` 会将逻辑分片表和数据库中真实的表全部删除。
3. `CREATE DATABASE` 只会创建`逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库。
