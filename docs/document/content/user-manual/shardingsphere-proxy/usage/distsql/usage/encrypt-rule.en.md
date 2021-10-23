+++
title = "Encrypt"
weight = 3
+++

## Usage

### Pre-work

1. Start the MySQL service
2. Create MySQL database (refer to ShardingProxy data source configuration rules)
3. Create a role or user with creation permission for ShardingProxy
4. Start Zookeeper service (for persistent configuration)

### Start ShardingProxy

1. Add `mode` and `authentication` configurations to `server.yaml` (please refer to the example of ShardingProxy)
2. Start ShardingProxy ([Related introduction](/en/quick-start/shardingsphere-proxy-quick-start/))

### Create a distributed database and sharding tables

1. Connect to ShardingProxy
2. Create a distributed database

```sql
CREATE DATABASE encrypt_db;
```

3. Use newly created database

```sql
USE encrypt_db;
```

4. Configure data source information

```sql
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
);
```
5. Create encrypt table

```sql
CREATE TABLE `t_encrypt` (
  `order_id` int NOT NULL,
  `user_plain` varchar(45) DEFAULT NULL,
  `user_cipher` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

6. Create encrypt rule

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),
(NAME=order_id, CIPHER =order_cipher,TYPE(NAME=MD5))
));
```

7. Alter encrypt rule

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'='123456abc'))),
));
```

8. Drop encrypt rule

```sql
DROP ENCRYPT RULE t_encrypt;
```

9. Drop resource

```sql
DROP RESOURCE ds_0;
```

10. Drop distributed database

```sql
DROP DATABASE encrypt_db;
```

### Notice

1. Currently, `DROP DATABASE` will only remove the `logical distributed database`, not the user's actual database. 
2. `DROP TABLE` will delete all logical fragmented tables and actual tables in the database.
3. `CREATE DATABASE` will only create a `logical distributed database`, so users need to create actual databases in advance.
