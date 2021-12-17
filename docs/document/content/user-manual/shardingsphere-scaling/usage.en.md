+++
title = "Manual"
weight = 2
+++

## Manual

### Environment

JAVA，JDK 1.8+.

The migration scene we support:

| Source                     | Target                  |
| -------------------------- | ----------------------- |
| MySQL(5.1.15 ~ 5.7.x)      | MySQL(5.1.15 ~ 5.7.x)   |
| PostgreSQL(9.4 ~ )         | PostgreSQL(9.4 ~ )      |
| openGauss(2.1.0)           | openGauss(2.1.0)        |

**Attention**: 

If the backend database is in following table, please download JDBC driver jar and put it into `${shardingsphere-proxy}/lib` directory.

| RDBMS                 | JDBC driver                          | Reference            |
| --------------------- | ------------------------------------ | -------------------- |
| MySQL                 | [mysql-connector-java-5.1.47.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar ) | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |
| openGauss             | [opengauss-jdbc-2.0.1-compatibility.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/2.0.1-compatibility/opengauss-jdbc-2.0.1-compatibility.jar ) | |

Supported features:

| Feature                                  | MySQL         | PostgreSQL    | openGauss     |
| ---------------------------------------- | ------------- | ------------- | ------------- |
| Inventory migration                      | Supported     | Supported     | Supported     |
| Incremental migration                    | Supported     | Supported     | Supported     |
| Create table automatically               | Supported     | Unsupported   | Supported     |
| Default data consistency check algorithm | Supported     | Unsupported   | Unsupported   |

**Attention**:

For RDBMS which `Create table automatically` feature is not supported, we need to create sharding tables manually.

### Privileges

We need to enable `binlog` for MySQL. Privileges of users scaling used should include Replication privileges.

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

Execute the following SQL to confirm whether the user has migration permission or not:

```sql
SHOW GRANTS 'user';
```

```
+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

PostgreSQL need to support and open [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) feature.

### DistSQL API for auto mode

#### Preview current sharding rule

Example:
```sql
preview select count(1) from t_order;
```

Response:
```
mysql> preview select count(1) from t_order;
+------------------+--------------------------------+
| data_source_name | sql                            |
+------------------+--------------------------------+
| ds_0             | select count(1) from t_order_0 |
| ds_0             | select count(1) from t_order_1 |
| ds_1             | select count(1) from t_order_0 |
| ds_1             | select count(1) from t_order_1 |
+------------------+--------------------------------+
4 rows in set (0.00 sec)
```

#### Start scaling job

1. Add new data source resources

Please refer to [RDL#Data Source](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/) for more details.

Create database on underlying RDBMS first, it will be used in following `DistSQL`.

Example:
```sql
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
-- ds_3, ds_4
```

2. Alter sharding table rule

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
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=10)),
GENERATED_KEY(COLUMN=order_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

If `RESOURCES` and `sharding-count` is changed, then scaling job will be emitted.

Uncompleted example of alter `TableRule`:
```sql
ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="ds_${user_id % 3 + 2}"))
);

ALTER SHARDING TABLE RULE t_order (
DATANODES("ds_${2..4}.t_order_${0..1}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
GENERATED_KEY(COLUMN=order_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
), t_order_item (
DATANODES("ds_${2..4}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_item_inline),
GENERATED_KEY(COLUMN=order_item_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

If `algorithm-expression` of `database_inline` and `DATANODES` of `t_order` is changed, then scaling job will be emitted.

#### List scaling jobs

Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

Example:
```sql
show scaling list;
```

Response:
```
mysql> show scaling list;
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
| id                 | tables                | sharding_total_count | active | create_time         | stop_time           |
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
| 659853312085983232 | t_order_item, t_order | 2                    | 0      | 2021-10-26 20:21:31 | 2021-10-26 20:24:01 |
| 660152090995195904 | t_order_item, t_order | 2                    | 0      | 2021-10-27 16:08:43 | 2021-10-27 16:11:00 |
+--------------------+-----------------------+----------------------+--------+---------------------+---------------------+
2 rows in set (0.04 sec)
```

#### Get scaling progress

Example:
```sql
show scaling status {jobId};
```

Response:
```
mysql> show scaling status 660152090995195904;
+------+-------------+----------+-------------------------------+--------------------------+
| item | data_source | status   | inventory_finished_percentage | incremental_idle_minutes |
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
| ALMOST_FINISHED                                   | almost finished                                              |
| FINISHED                                          | finished (The whole process is completed, and the new rules have been taken effect) |
| PREPARING_FAILURE                                 | preparation failed                                           |
| EXECUTE_INVENTORY_TASK_FAILURE                    | inventory task failed                                        |
| EXECUTE_INCREMENTAL_TASK_FAILURE                  | incremental task failed                                      |

If `status` fails, you can check the log of `proxy` to view the error stack and analyze the problem.

#### Preview new sharding rule

Example:
```sql
preview select count(1) from t_order;
```

Response:
```
mysql> preview select count(1) from t_order;
+------------------+--------------------------------+
| data_source_name | sql                            |
+------------------+--------------------------------+
| ds_2             | select count(1) from t_order_0 |
| ds_2             | select count(1) from t_order_1 |
| ds_3             | select count(1) from t_order_0 |
| ds_3             | select count(1) from t_order_1 |
| ds_4             | select count(1) from t_order_0 |
| ds_4             | select count(1) from t_order_1 |
+------------------+--------------------------------+
6 rows in set (0.01 sec)
```

#### Other DistSQL
Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

### DistSQL API for manual mode

Data consistency check and switch configuration could be emitted manually. Please refer to [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) for more details.

Attention: It's still under development.
