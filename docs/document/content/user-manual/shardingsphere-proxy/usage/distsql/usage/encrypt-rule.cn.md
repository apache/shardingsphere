+++
title = "数据加密"
weight = 3
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
CREATE DATABASE encrypt_db;
```

3. 使用新创建的数据库

```sql
USE encrypt_db;
```

4. 配置数据源信息

```sql
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
);
```

5. 创建加密表

```sql
CREATE TABLE `t_encrypt` (
  `order_id` int NOT NULL,
  `user_plain` varchar(45) DEFAULT NULL,
  `user_cipher` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

6. 创建加密规则

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),
(NAME=order_id, CIPHER =order_cipher,TYPE(NAME=MD5))
));
```

7. 修改加密规则

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),
));
```

8. 删除加密规则

```sql
DROP ENCRYPT RULE t_encrypt;
```

9. 删除数据源

```sql
DROP RESOURCE ds_0;
```

10. 删除分布式数据库

```sql
DROP DATABASE encrypt_db;
```

### 注意事项

1. 当前, `DROP DATABASE` 只会移除`逻辑的分布式数据库`，不会删除用户真实的数据库。
2. `DROP TABLE` 会将逻辑分片表和数据库中真实的表全部删除。
3. `CREATE DATABASE` 只会创建`逻辑的分布式数据库`，所以需要用户提前创建好真实的数据库。
