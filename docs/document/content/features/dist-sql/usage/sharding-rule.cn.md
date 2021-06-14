+++
title = "数据分片"
weight = 1
+++

## 使用实战

### 前置工作

1. 启动MySQL服务
2. 创建MySQL数据库(参考ShardingProxy数据源配置规则)
3. 为ShardingProxy创建一个拥有创建权限的角色或者用户
4. 启动Zookeeper服务 (为了持久化配置)

### 启动ShardingProxy

1. 添加 `governance` 和 `authentication` 配置参数到 `server.yaml` (请参考相关example案例)
2. 启动 ShardingProxy ([相关介绍](/cn/quick-start/shardingsphere-proxy-quick-start/))

### 创建分布式数据库和分片表

1. 连接到ShardingProxy
2. 创建分布式数据库

```SQL
CREATE DATABASE sharding_db;
```

3. 使用新创建的数据库

```SQL
USE sharding_db;
```

2. 配置数据源信息

```SQL
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_1,
USER=root,
PASSWORD=root
);

ADD RESOURCE ds_1 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_2,
USER=root,
PASSWORD=root
);
```

3. 创建分片规则

```SQL
CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=4)),
GENERATED_KEY(COLUMN=order_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

4. 创建切分表

```SQL
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `user_id` int NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

5. 删除切分表

```SQL
DROP TABLE t_order;
```

6. 删除分片规则

```SQL
DROP SHARDING TABLE RULE t_order;
```

7. 删除数据源

```SQL
DROP RESOURCE ds_0, ds_1;
```

8. 删除分布式数据库

```SQL
DROP DATABASE sharding_db;
```

### 注意事项

1. 当前, `DROP DB`只会移除`逻辑的分布式数据库`，不会删除用户真实的数据库 (**TODO**)。
2. `DROP TABLE`会将逻辑分片表和数据库中真实的表全部删除。
3. `CREATE DB`只会创建`逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库（**TODO**）。
4. `自动分片算法`会持续增加，从而覆盖用户各大分片场景 (**TODO**)。
5. 重构`ShardingAlgorithmPropertiesUtil`（**TODO**）。
6. 保证所有客户端完成RDL执行（**TODO**）。
7. 增加 `ALTER DB` 和 `ALTER TABLE`的支持（**TODO**）。
