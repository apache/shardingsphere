+++
title = "使用手册"
weight = 2
+++

## openGauss 使用手册

### 环境要求

支持的 openGauss 版本：2.x ~ 3.x。

### 权限要求

1. 调整源端 WAL 配置。

`postgresql.conf` 示例配置：
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

详情请参见 [Write Ahead Log](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) 和 [Replication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html)。

2. 赋予源端 openGauss 账号 replication 权限。

`pg_hba.conf` 示例配置：

```
host replication repl_acct 0.0.0.0/0 md5
# 0.0.0.0/0 表示允许任意 IP 地址访问，可以根据实际情况调整成 CDC Server 的 IP 地址
```

详情请参见 [Configuring Client Access Authentication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) 和 [Example: Logic Replication Code](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html)。

3. 赋予 openGauss 账号 DDL DML 权限。

如果使用非超级管理员账号，要求该账号在用到的数据库上，具备 CREATE 和 CONNECT 的权限。

示例：
```sql
GRANT CREATE, CONNECT ON DATABASE source_ds TO cdc_user;
```

还需要账号对迁移的表和 schema 具备访问权限，以 test schema 下的 t_order 表为例。

```sql
\c source_ds

GRANT USAGE ON SCHEMA test TO GROUP cdc_user;
GRANT SELECT ON TABLE test.t_order TO cdc_user;
```

openGauss 有 OWNER 的概念，如果是数据库，SCHEMA，表的 OWNER，则可以省略对应的授权步骤。

openGauss 不允许普通账户在 public schema 下操作。所以如果迁移的表在 public schema 下，需要额外授权。

```sql
GRANT ALL PRIVILEGES TO cdc_user;
```

详情请参见 [openGauss GRANT](https://docs.opengauss.org/zh/docs/2.0.1/docs/Developerguide/GRANT.html)


### 完整流程示例

#### 前提条件

1. 在 MySQL 已准备好源端库、表、数据。

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0 DEFAULT CHARSET utf8;

USE migration_ds_0;

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. 在 MySQL 准备目标端库。

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12 DEFAULT CHARSET utf8;
```

#### 操作步骤

1. 在 proxy 新建逻辑数据库并配置好存储单元和规则。

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
STORAGE_UNITS(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

如果是迁移到异构数据库，那目前需要在 proxy 执行建表语句。

2. 在 proxy 配置源端存储单元。

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
```
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| id                                         | tables       | job_item_count | active | create_time         | stop_time |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| j0102p00002333dcb3d9db141cef14bed6fbf1ab54 | ds_0.t_order | 1              | true   | 2023-09-20 14:41:32 | NULL      |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
```

5. 查看数据迁移详情。

```sql
SHOW MIGRATION STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

示例结果：
```
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | tables       | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | ds_0.t_order | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           |                          |               |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. 执行数据一致性校验。

```sql
CHECK MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54' BY TYPE (NAME='DATA_MATCH');
```

数据一致性校验算法类型来自：
```sql
SHOW MIGRATION CHECK ALGORITHMS;
```

示例结果：
```
+-------------+--------------+--------------------------------------------------------------+----------------------------+
| type        | type_aliases | supported_database_types                                     | description                |
+-------------+--------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH |              | MySQL,MariaDB,H2                                             | Match CRC32 of records.    |
| DATA_MATCH  |              | SQL92,MySQL,PostgreSQL,openGauss,Oracle,SQLServer,MariaDB,H2 | Match raw data of records. |
+-------------+--------------+--------------------------------------------------------------+----------------------------+
```

目标端开启数据加密的情况需要使用`DATA_MATCH`。

异构迁移需要使用`DATA_MATCH`。

查询数据一致性校验进度：
```sql
SHOW MIGRATION CHECK STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

示例结果：
```
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| tables       | result | check_failed_tables | active | inventory_finished_percentage | inventory_remaining_seconds | incremental_idle_seconds | check_begin_time        | check_end_time          | duration_seconds | algorithm_type | algorithm_props | error_message |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| ds_0.t_order | true   |                     | false  | 100                           | 0                           |                          | 2023-09-20 14:45:31.992 | 2023-09-20 14:45:33.519 | 1                | DATA_MATCH     |                 |               |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
```

7. 完成作业。

```sql
COMMIT MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

更多 DistSQL 请参见 [RAL #数据迁移](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E6%95%B0%E6%8D%AE%E8%BF%81%E7%A7%BB)。

#### 前提条件

1. 准备好 CDC 源端的库、表、数据。

```sql
DROP DATABASE IF EXISTS ds_0;
CREATE DATABASE ds_0;

DROP DATABASE IF EXISTS ds_1;
CREATE DATABASE ds_1;
```

#### 操作步骤

1. 在 `server.yaml` 中开启 CDC 功能。

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: cdc
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500

authority:
  users:
    - user: root@%
      password: root
  privilege:
    type: ALL_PERMITTED

#开启 GLT 功能参考 CDC 部署手册
#transaction:
#  defaultType: XA
#  providerType: Atomikos
#
#globalClock:
#  enabled: true
#  type: TSO
#  provider: redis
#  props:
#    host: 127.0.0.1
#    port: 6379

props:
  system-log-level: INFO
  check-table-metadata-enabled: false
  proxy-default-port: 3307 # Proxy default port.
  cdc-server-port: 33071 # CDC server port
  proxy-frontend-database-protocol-type: openGauss
```

2. 在 proxy 新建逻辑数据库并配置好存储单元和规则。

2.1. 创建逻辑库。

```sql
CREATE DATABASE sharding_db;

\c sharding_db
```
2.2. 注册存储单元。

```sql
REGISTER STORAGE UNIT ds_0 (
    URL="jdbc:opengauss://127.0.0.1:5432/ds_0",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_1 (
    URL="jdbc:opengauss://127.0.0.1:5432/ds_1",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

2.3. 创建分片规则。

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="2")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

2.4. 创建表和初始化数据

在 proxy 执行建表语句。

```sql
CREATE TABLE t_order (id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

INSERT INTO t_order (id, user_id, status) VALUES (1,1,'ok1'),(2,2,'ok2'),(3,3,'ok3');
```

3. 启动 CDC Client

引入 CDC Client 依赖，然后按照 [Example](https://github.com/apache/shardingsphere/blob/master/kernel/data-pipeline/scenario/cdc/client/src/test/java/org/apache/shardingsphere/data/pipeline/cdc/client/example/Bootstrap.java) 启动 CDC Client。

观察 CDC Client 启动后，是否有如下的日志

```
 records: [before {
  name: "id"
  value {
    type_url: "type.googleapis.com/google.protobuf.Empty"
  }
}
```

4. 通过 DistSQL 查看 CDC 任务状态

CDC 任务的启动和停止目前只能通过 CDC Client 控制，可以在 proxy 中执行对应的 DistSQL 查看 CDC 任务的运行情况

1. 查看 CDC 任务列表

SHOW STREAMING LIST;

运行结果

```
sharding_db=> SHOW STREAMING LIST;
                     id                     |  database   | tables  | job_item_count | active |     create_time     | stop_time 
--------------------------------------------+-------------+---------+----------------+--------+---------------------+-----------
 j0302p0000702a83116fcee83f70419ca5e2993791 | sharding_db | t_order | 1              | true   | 2023-10-27 22:01:27 | 
(1 row)
```

2. 查看 CDC 任务详情

SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;

运行结果

```
sharding_db=> SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;
 item | data_source |          status          | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message
------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------
 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 1                       | 100                           | 101                      |
 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | true   | 2                       | 100                           | 100                      |
(2 rows)
```

3. 删除 CDC 任务

DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;

此时也会删除 openGauss 物理库上的 replication slots

```
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```