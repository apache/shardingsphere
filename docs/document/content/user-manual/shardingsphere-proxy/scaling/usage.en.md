+++
title = "Manual"
weight = 2
+++

## Manual

### Environment

JAVA, JDK 1.8+.

The migration scene we support:

| Source                     | Target                  |
| -------------------------- | ----------------------- |
| MySQL(5.1.15 ~ 5.7.x)      | MySQL(5.1.15 ~ 5.7.x)   |
| PostgreSQL(9.4 ~ )         | PostgreSQL(9.4 ~ )      |
| openGauss(2.1.0)           | openGauss(2.1.0)        |

Supported features:

| Feature                                  | MySQL         | PostgreSQL    | openGauss     |
| ---------------------------------------- | ------------- |---------------| ------------- |
| Inventory migration                      | Supported     | Supported     | Supported     |
| Incremental migration                    | Supported     | Supported     | Supported     |
| Create table automatically               | Supported     | Supported     | Supported     |
| DATA_MATCH data consistency check        | Supported     | Supported     | Supported     |
| CRC32_MATCH data consistency check       | Supported     | Unsupported   | Unsupported   |

**Attention**:

For RDBMS which `Create table automatically` feature is not supported, we need to create sharding tables manually.

### Privileges

#### MySQL

1. Enable `binlog`

Configuration Example of MySQL 5.7 `my.cnf`:
```
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=row
binlog-row-image=full
max_connections=600
```

Execute the following SQL to confirm whether binlog is turned on or not:
```sql
show variables like '%log_bin%';
show variables like '%binlog%';
```

As shown below, it means binlog has been turned on:
```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
| binlog_row_image                        | FULL                                  |
+-----------------------------------------+---------------------------------------+
```

2. Privileges of account that scaling use should include Replication privileges.

Execute the following SQL to confirm whether the user has migration permission or not:
```sql
SHOW GRANTS FOR 'user';
```

Result Example:
```
+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

#### PostgreSQL

1. Enable [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) feature.

2. Adjust WAL configuration

Configuration Example of `postgresql.conf`:
```
wal_level = logical
max_replication_slots = 10
max_connections = 600
```

Please refer to [Write Ahead Log](https://www.postgresql.org/docs/9.6/runtime-config-wal.html) and [Replication](https://www.postgresql.org/docs/9.6/runtime-config-replication.html ) for more details.

### DistSQL API for auto mode

#### Preview current sharding rule

Example:
```sql
preview SELECT COUNT(1) FROM t_order;
```

Response:
```
mysql> preview SELECT COUNT(1) FROM t_order;
+------------------+-------------------------------------------------------------------------+
| data_source_name | actual_sql                                                              |
+------------------+-------------------------------------------------------------------------+
| ds_0             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_1             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
+------------------+-------------------------------------------------------------------------+
2 rows in set (0.65 sec)
```

#### Start scaling job

1. Add new data source resources

Please refer to [RDL#Data Source](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/) for more details.

Create database on underlying RDBMS first, it will be used in following `DistSQL`.

Example:
```sql
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_2?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
), ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_3?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
), ds_4 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_4?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);
```

2. Alter sharding table rule for tables to be scaled

We could scale all tables or partial tables. Binding tables must be scaled together.

Currently, scaling job could only be emitted by executing `ALTER SHARDING TABLE RULE` DistSQL.

Please refer to [RDL#Sharding](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/) for more details.

`SHARDING TABLE RULE` support two types: `TableRule` and `AutoTableRule`. Following is a comparison of the two sharding rule types: 

| Type         | AutoTableRule                                               | TableRule                                                    |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Definition   | [Auto Sharding Algorithm](/en/features/sharding/concept/sharding/#auto-sharding-algorithm) | [User-Defined Sharding Algorithm](/en/features/sharding/concept/sharding/#user-defined-sharding-algorithm)   |

Meaning of fields in DistSQL is the same as YAML configuration, please refer to [YAML Configuration#Sharding](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/) for more details.

Example of alter `AutoTableRule`:
```sql
ALTER SHARDING TABLE RULE t_order (
RESOURCES(ds_2, ds_3, ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

`RESOURCES` is altered from `(ds_0, ds_1)` to `(ds_2, ds_3, ds_4)`, and `sharding-count` is altered from `4` to `6`, it will emit scaling job.

Uncompleted example of alter `TableRule`:
```sql
ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME="INLINE",PROPERTIES("algorithm-expression"="ds_${user_id % 3 + 2}"))
);

ALTER SHARDING TABLE RULE t_order (
DATANODES("ds_${2..4}.t_order_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
), t_order_item (
DATANODES("ds_${2..4}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_item_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_item_id,TYPE(NAME="snowflake"))
);
```

`algorithm-expression` of `database_inline` is alerted from `ds_${user_id % 2}` to `ds_${user_id % 3 + 2}`, and `DATANODES` of `t_order` is alerted from `ds_${0..1}.t_order_${0..1}` to `ds_${2..4}.t_order_${0..1}`, it will emit scaling job.

Currently, `ALTER SHARDING ALGORITHM` will take effect immediately, but table rule will not, it might cause inserting data into source side failure, so alter sharding table rule to `AutoTableRule` is recommended for now.

#### List scaling jobs

Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

Example:
```sql
SHOW MIGRATION LIST;
```

Response:
```
mysql> SHOW MIGRATION LIST;
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
| id                 | tables                | sharding_total_count | active | create_time         | stop_time           |
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
| 659853312085983232 | t_order_item, t_order | 2                    | false  | 2021-10-26 20:21:31 | 2021-10-26 20:24:01 |
| 660152090995195904 | t_order_item, t_order | 2                    | false  | 2021-10-27 16:08:43 | 2021-10-27 16:11:00 |
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
2 rows in set (0.04 sec)
```

#### Get migration progress

Example:
```sql
SHOW MIGRATION STATUS {jobId};
```

Response:
```
mysql> SHOW MIGRATION STATUS 660152090995195904;
+------+-------------+----------+-------------------------------+--------------------------+
| item | data_source | status   | inventory_finished_percentage | incremental_idle_seconds |
+------+-------------+----------+-------------------------------+--------------------------+
| 0    | ds_1        | FINISHED | 100                           | 2834                     |
| 1    | ds_0        | FINISHED | 100                           | 2834                     |
+------+-------------+----------+-------------------------------+--------------------------+
2 rows in set (0.00 sec)
```
Current scaling job is finished, new sharding rule should take effect, and not if scaling job is failed.

`status` values:

| Value                                             | Description                                                  |
| ------------------------------------------------- | ------------------------------------------------------------ |
| PREPARING                                         | preparing                                                    |
| RUNNING                                           | running                                                      |
| EXECUTE_INVENTORY_TASK                            | inventory task running                                       |
| EXECUTE_INCREMENTAL_TASK                          | incremental task running                                     |
| FINISHED                                          | finished (The whole process is completed, and the new rules have been taken effect) |
| PREPARING_FAILURE                                 | preparation failed                                           |
| EXECUTE_INVENTORY_TASK_FAILURE                    | inventory task failed                                        |
| EXECUTE_INCREMENTAL_TASK_FAILURE                  | incremental task failed                                      |

If `status` fails, you can check the log of `proxy` to view the error stack and analyze the problem.

#### Preview new sharding rule

Example:
```sql
preview SELECT COUNT(1) FROM t_order;
```

Response:
```
mysql> PREVIEW SELECT COUNT(1) FROM t_order;
+------------------+-------------------------------------------------------------------------+
| data_source_name | actual_sql                                                              |
+------------------+-------------------------------------------------------------------------+
| ds_2             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_3             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_4             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
+------------------+-------------------------------------------------------------------------+
3 rows in set (0.21 sec)
```

#### Other DistSQL
Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

### DistSQL manual mode whole process example

On manual mode, data consistency check and switch configuration could be emitted manually. Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

This example show how to migrate data from MySQL to proxy.

Most SQLs should be executed in proxy, except few ones mentioned for MySQL.

#### Create source databases

It's not needed in practice. It just simulates databases for testing.

Execute SQLs in MySQL:
```sql
DROP DATABASE IF EXISTS scaling_ds_0;
CREATE DATABASE scaling_ds_0 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_1;
CREATE DATABASE scaling_ds_1 DEFAULT CHARSET utf8;
```

#### Login proxy

```shell
mysql -h127.0.0.1 -P3307 -uroot -proot
```

#### Create and configure logical database

Create logical database:
```sql
CREATE DATABASE scaling_db;

USE scaling_db
```

Add source database resource:
```sql
ADD RESOURCE ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="50","idleTimeout"="60000")
), ds_1 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="50","idleTimeout"="60000")
);
```

Configure rules:
Configure tables of existing system in sharding rule, sharding table rules and INLINE algorithm will be used to fit existing tables name.
```sql
CREATE SHARDING ALGORITHM database_inline (
TYPE(NAME="INLINE",PROPERTIES("algorithm-expression"="ds_${user_id % 2}"))
);
CREATE SHARDING ALGORITHM t_order_inline (
TYPE(NAME="INLINE",PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
);
CREATE SHARDING ALGORITHM t_order_item_inline (
TYPE(NAME="INLINE",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}"))
);

CREATE SHARDING TABLE RULE t_order (
DATANODES("ds_${0..1}.t_order_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
), t_order_item (
DATANODES("ds_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_item_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_item_id,TYPE(NAME="snowflake"))
);

CREATE SHARDING SCALING RULE scaling_manual2 (
DATA_CONSISTENCY_CHECKER(TYPE(NAME="CRC32_MATCH"))
);
```

#### Create test tables and initialize records

It's not needed in practice.

```sql
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) CHARSET utf8mb4, PRIMARY KEY (order_id));
CREATE TABLE t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) CHARSET utf8mb4, creation_date DATE, PRIMARY KEY (item_id));

INSERT INTO T_ORDER (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
INSERT INTO T_ORDER_ITEM (item_id, order_id, user_id, status) VALUES (1,1,2,'ok'),(2,2,4,'ok'),(3,3,6,'ok'),(4,4,1,'ok'),(5,5,3,'ok'),(6,6,5,'ok');
```

#### Run migration

Preview sharding:
```sql
mysql> PREVIEW SELECT COUNT(1) FROM t_order;
+------------------+-------------------------------------------------------------------------+
| data_source_name | actual_sql                                                              |
+------------------+-------------------------------------------------------------------------+
| ds_0             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_1             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
+------------------+-------------------------------------------------------------------------+
2 rows in set (0.65 sec)
```

Create target databases in MySQL:
```sql
DROP DATABASE IF EXISTS scaling_ds_10;
CREATE DATABASE scaling_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_11;
CREATE DATABASE scaling_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_12;
CREATE DATABASE scaling_ds_12 DEFAULT CHARSET utf8;
```

Add target database resource:
```sql
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_10?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="50","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_11?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="50","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:mysql://127.0.0.1:3306/scaling_ds_12?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="50","idleTimeout"="60000")
);
```

Alter sharding rule to emit scaling job:
```sql
ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME="INLINE",PROPERTIES("algorithm-expression"="ds_${user_id % 3 + 2}"))
);

ALTER SHARDING TABLE RULE t_order (
DATANODES("ds_${2..4}.t_order_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
), t_order_item (
DATANODES("ds_${2..4}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_item_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_item_id,TYPE(NAME="snowflake"))
);
```

Query job progress:
```sql
mysql> SHOW MIGRATION LIST;
+--------------------------------------------+----------------------+----------------------+--------+---------------------+-----------+
| id                                         | tables               | sharding_total_count | active | create_time         | stop_time |
+--------------------------------------------+----------------------+----------------------+--------+---------------------+-----------+
| 0130317c30317c3054317c7363616c696e675f6462 | t_order,t_order_item | 2                    | true   | 2022-04-16 17:22:19 | NULL      |
+--------------------------------------------+----------------------+----------------------+--------+---------------------+-----------+
1 row in set (0.34 sec)

mysql> SHOW MIGRATION STATUS "0130317c30317c3054317c7363616c696e675f6462";
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| item | data_source | status                   | active | inventory_finished_percentage | incremental_idle_seconds |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 100                           | 8                        |
| 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | true   | 100                           | 7                        |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
2 rows in set (0.02 sec)
```
When `status` is `EXECUTE_INCREMENTAL_TASK`, it means inventory migration stage is successful, it's running on incremental migration stage.

Choose an idle time of business system, stop source database writing or stop upper database operation.

Stop source writing in proxy:
```sql
mysql> STOP MIGRATION SOURCE WRITING "0130317c30317c3054317c7363616c696e675f6462";
Query OK, 0 rows affected (0.07 sec)
```

Data consistency check:
```sql
mysql> CHECK MIGRATION "0130317c30317c3054317c7363616c696e675f6462" BY TYPE (NAME="CRC32_MATCH");
+--------------+----------------------+----------------------+-----------------------+-------------------------+
| table_name   | source_records_count | target_records_count | records_count_matched | records_content_matched |
+--------------+----------------------+----------------------+-----------------------+-------------------------+
| t_order      | 6                    | 6                    | true                  | true                    |
| t_order_item | 6                    | 6                    | true                  | true                    |
+--------------+----------------------+----------------------+-----------------------+-------------------------+
2 rows in set (2.16 sec)
```

Apply metadata:
```sql
mysql> APPLY MIGRATION "0130317c30317c3054317c7363616c696e675f6462";
Query OK, 0 rows affected (0.22 sec)
```

Preview sharding again:
```sql
mysql> PREVIEW SELECT COUNT(1) FROM t_order;
+------------------+-------------------------------------------------------------------------+
| data_source_name | actual_sql                                                              |
+------------------+-------------------------------------------------------------------------+
| ds_2             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_3             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_4             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
+------------------+-------------------------------------------------------------------------+
3 rows in set (0.21 sec)
```
Sharding already take effect.

Optionally, unused `ds_0` and `ds_1` could be removed.
