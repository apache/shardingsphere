+++
title = "使用手册"
weight = 2
+++

## MySQL 使用手册

### 环境要求

支持的 MySQL 版本：5.1.15 ~ 8.0.x。

### 权限要求

1. 开启 `binlog`

MySQL 5.7 `my.cnf` 示例配置：

```
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=row
binlog-row-image=full
max_connections=600
```

执行以下命令，确认是否有开启 binlog：

```
show variables like '%log_bin%';
show variables like '%binlog%';
```

如以下显示，则说明 binlog 已开启

```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
| binlog_row_image                        | FULL                                  |
+-----------------------------------------+---------------------------------------+
```

2. 赋予 MySQL 账号 Replication 相关权限。

执行以下命令，查看该用户是否有迁移权限：

```
SHOW GRANTS FOR 'user';
```

示例结果：

```
+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

### 完整流程示例

#### 前提条件

1. 在 MySQL 已准备好源端库、表、数据。

示例：

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0 DEFAULT CHARSET utf8;

USE migration_ds_0;

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. 在 MySQL 准备目标端库。

示例：

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12 DEFAULT CHARSET utf8;
```

#### 操作步骤

1. 在 proxy 新建逻辑数据库并配置好资源和规则。

```sql
CREATE DATABASE sharding_db;

USE sharding_db

REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_10?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_11?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_12?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);

CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

如果是迁移到异构数据库，那目前需要在 proxy 执行建表语句。

2. 在 proxy 配置源端资源。

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. 启动数据迁移。

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

或者指定目标端逻辑库：

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

4. 查看数据迁移作业列表。

```sql
SHOW MIGRATION LIST;
```

示例结果：

```sql
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                    | tables  | sharding_total_count | active | create_time         | stop_time |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j01016e501b498ed1bdb2c373a2e85e2529a6 | t_order | 1                    | true   | 2022-10-13 11:16:01 | NULL      |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. 查看数据迁移详情。

```sql
SHOW MIGRATION STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           | 81                       |               |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. 执行数据一致性校验。

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6' BY TYPE (NAME='CRC32_MATCH');
Query OK, 0 rows affected (0.09 sec)
```

数据一致性校验算法类型来自：
```sql
SHOW MIGRATION CHECK ALGORITHMS;
+-------------+--------------------------------------------------------------+----------------------------+
| type        | supported_database_types                                     | description                |
+-------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH | MySQL                                                        | Match CRC32 of records.    |
| DATA_MATCH  | SQL92,MySQL,MariaDB,PostgreSQL,openGauss,Oracle,SQLServer,H2 | Match raw data of records. |
+-------------+--------------------------------------------------------------+----------------------------+
```

目标端开启数据加密的情况需要使用`DATA_MATCH`。

异构迁移需要使用`DATA_MATCH`。

查询数据一致性校验进度：
```sql
SHOW MIGRATION CHECK STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| t_order | true   | 100                 | 0                 | 2022-10-13 11:18:15.171 | 2022-10-13 11:18:15.878 | 0                |               |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
```

数据一致性校验是在后台异步执行的，可以通过如下命令启动或者停止

停止示例：

```sql
STOP MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

启动示例：

```sql
START MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

7. 完成作业。

```sql
COMMIT MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

8. 刷新元数据。

```sql
REFRESH TABLE METADATA;
```

更多 DistSQL 请参见 [RAL #数据迁移](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E6%95%B0%E6%8D%AE%E8%BF%81%E7%A7%BB)。

## PostgreSQL 使用手册

### 环境要求

支持的 PostgreSQL 版本：9.4 或以上版本。

### 权限要求

1. 开启 [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html)。

2. 调整 WAL 配置。

`postgresql.conf` 示例配置：
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

详情请参见 [Write Ahead Log](https://www.postgresql.org/docs/9.6/runtime-config-wal.html) 和 [Replication](https://www.postgresql.org/docs/9.6/runtime-config-replication.html )。

3. 配置 PostgreSQL 允许 Proxy 拥有 replication 权限。

`pg_hba.conf` 示例配置：
```
host replication repl_acct 0.0.0.0/0 md5
```

详情请参见 [The pg_hba.conf File](https://www.postgresql.org/docs/9.6/auth-pg-hba-conf.html)。

### 完整流程示例

#### 前提条件

1. 在 PostgreSQL 已准备好源端库、表、数据。

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0;

\c migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. 在 PostgreSQL 准备目标端库。

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12;
```

#### 操作步骤

1. 在 proxy 新建逻辑数据库并配置好资源和规则。

```sql
CREATE DATABASE sharding_db;

\c sharding_db

REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_10",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_11",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_12",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);

CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

如果是迁移到异构数据库，那目前需要在 proxy 执行建表语句。

2. 在 proxy 配置源端资源。

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_0",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. 启动数据迁移。

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

或者指定目标端逻辑库：

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

也可以指定源端schema：

```sql
MIGRATE TABLE ds_0.public.t_order INTO sharding_db.t_order;
```

4. 查看数据迁移作业列表。

```sql
SHOW MIGRATION LIST;
```

示例结果：

```sql
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                    | tables  | sharding_total_count | active | create_time         | stop_time |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j01016e501b498ed1bdb2c373a2e85e2529a6 | t_order | 1                    | true   | 2022-10-13 11:16:01 | NULL      |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. 查看数据迁移详情。

```sql
SHOW MIGRATION STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           | 81                       |               |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. 执行数据一致性校验。

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6';
Query OK, 0 rows affected (0.09 sec)
```

查询数据一致性校验进度：
```sql
SHOW MIGRATION CHECK STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| t_order | true   | 100                 | 0                 | 2022-10-13 11:18:15.171 | 2022-10-13 11:18:15.878 | 0                |               |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
```

数据一致性校验是在后台异步执行的，可以通过如下命令启动或者停止

停止示例：

```sql
STOP MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

启动示例：

```sql
START MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

7. 完成作业。

```sql
COMMIT MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

8. 刷新元数据。

```sql
REFRESH TABLE METADATA;
```

更多 DistSQL 请参见 [RAL #数据迁移](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E6%95%B0%E6%8D%AE%E8%BF%81%E7%A7%BB)。

## openGauss 使用手册

### 环境要求

支持的 openGauss 版本：2.0.1 ~ 3.0.0。

### 权限要求

1. 调整 WAL 配置。

`postgresql.conf` 示例配置：
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

详情请参见 [Write Ahead Log](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) 和 [Replication](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html)。

2. 配置 openGauss 允许 Proxy 拥有 replication 权限。

`pg_hba.conf` 示例配置：
```
host replication repl_acct 0.0.0.0/0 md5
```

详情请参见 [Configuring Client Access Authentication](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) 和 [Example: Logic Replication Code](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html)。

### 完整流程示例

#### 前提条件

1. 在 openGauss 已准备好源端库、表、数据。

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0;

\c migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. 在 openGauss 准备目标端库。

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12;
```

#### 操作步骤

1. 在 proxy 新建逻辑数据库并配置好资源和规则。

```sql
CREATE DATABASE sharding_db;

\c sharding_db

REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_10",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_11",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_12",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);

CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

如果是迁移到异构数据库，那目前需要在 proxy 执行建表语句。

2. 在 proxy 配置源端资源。

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_0",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. 启动数据迁移。

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

或者指定目标端逻辑库：

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

也可以指定源端schema：

```sql
MIGRATE TABLE ds_0.public.t_order INTO sharding_db.t_order;
```

4. 查看数据迁移作业列表。

```sql
SHOW MIGRATION LIST;
```

示例结果：

```sql
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                    | tables  | sharding_total_count | active | create_time         | stop_time |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j01016e501b498ed1bdb2c373a2e85e2529a6 | t_order | 1                    | true   | 2022-10-13 11:16:01 | NULL      |
+---------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. 查看数据迁移详情。

```sql
SHOW MIGRATION STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           | 81                       |               |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. 执行数据一致性校验。

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6';
Query OK, 0 rows affected (0.09 sec)
```

查询数据一致性校验进度：
```sql
SHOW MIGRATION CHECK STATUS 'j01016e501b498ed1bdb2c373a2e85e2529a6';
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| t_order | true   | 100                 | 0                 | 2022-10-13 11:18:15.171 | 2022-10-13 11:18:15.878 | 0                |               |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
```

数据一致性校验是在后台异步执行的，可以通过如下命令启动或者停止

停止示例：

```sql
STOP MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

启动示例：

```sql
START MIGRATION CHECK 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

7. 完成作业。

```sql
COMMIT MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6';
```

8. 刷新元数据。

```sql
REFRESH TABLE METADATA;
```

更多 DistSQL 请参见 [RAL #数据迁移](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E6%95%B0%E6%8D%AE%E8%BF%81%E7%A7%BB)。
