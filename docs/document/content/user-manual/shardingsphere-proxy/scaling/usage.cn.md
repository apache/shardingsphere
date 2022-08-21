+++
title = "使用手册"
weight = 2
+++

## 使用手册

### 环境要求

纯  JAVA 开发，JDK  建议 1.8 以上版本。

支持的数据库及版本如下：

| 源端                   | 目标端                   |
| --------------------- | ----------------------- |
| MySQL(5.1.15 ~ 5.7.x) | MySQL(5.1.15 ~ 5.7.x)   |
| PostgreSQL(9.4 ~ )    | PostgreSQL(9.4 ~ )      |
| openGauss(2.1.0)      | openGauss(2.1.0)        |

功能支持情况：

| 功能                   | MySQL         | PostgreSQL   | openGauss     |
| --------------------- | ------------- |--------------| ------------- |
| 全量迁移               | 支持           | 支持           | 支持           |
| 增量迁移               | 支持           | 支持           | 支持           |
| 自动建表               | 支持           | 支持           | 支持            |
| DATA_MATCH一致性校验   | 支持           | 支持           | 支持           |
| CRC32_MATCH一致性校验  | 支持           | 不支持          | 不支持          |

**注意**：

还没开启 `自动建表` 的数据库需要手动创建分表。

### 权限要求
#### MySQL
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

#### PostgreSQL

1. 开启 [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html)。

2. 调整 WAL 配置。

`postgresql.conf` 示例配置：
```
wal_level = logical
max_replication_slots = 10
max_connections = 600
```

详情请参见 [Write Ahead Log](https://www.postgresql.org/docs/9.6/runtime-config-wal.html) 和 [Replication](https://www.postgresql.org/docs/9.6/runtime-config-replication.html )。

### DistSQL 自动模式接口

#### 预览当前分片规则

示例：
```sql
preview SELECT COUNT(1) FROM t_order;
```

返回信息：
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

#### 创建迁移任务

1. 添加新的数据源。

详情请参见 [RDL #数据源资源](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/)。

先在底层数据库系统创建需要的分库，下面的 `DistSQL` 需要用到。

示例：
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

2. 修改待迁移表的分片规则。

待迁移表可以是所有表，也可以是部分表。绑定表只能一块迁移。

目前只有通过执行 `ALTER SHARDING TABLE RULE` DistSQL 来触发迁移。

详情请参见 [RDL #数据分片](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/)。

`SHARDING TABLE RULE` 支持 2 种类型：`TableRule` 和 `AutoTableRule`。以下是两种分片规则的对比：

| 类型         | AutoTableRule（自动分片）                                      | TableRule（自定义分片）                                        |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 定义         | [自动化分片算法](/cn/features/sharding/concept/sharding/#自动化分片算法) | [自定义分片算法](/cn/features/sharding/concept/sharding/#自定义分片算法)   |

DistSQL 字段含义和 YAML 配置保持一致，详情请参见 [YAML 配置#数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)。

`AutoTableRule` 修改示例：
```sql
ALTER SHARDING TABLE RULE t_order (
RESOURCES(ds_2, ds_3, ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

`RESOURCES` 从 `(ds_0, ds_1)` 改为了 `(ds_2, ds_3, ds_4)`，`sharding-count` 从 `4` 改为了 `6`，会触发迁移。

`TableRule` 修改示例：
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

`database_inline` 的 `algorithm-expression` 从 `ds_${user_id % 2}` 改为 `ds_${user_id % 3 + 2}`，`t_order` 的 `DATANODES` 从 `ds_${0..1}.t_order_${0..1}` 改为 `ds_${2..4}.t_order_${0..1}`，会触发迁移。

目前 `ALTER SHARDING ALGORITHM` 会即时生效、但是规则还没生效，可能会导致源端 insert 异常，所以建议优先修改为 `AutoTableRule`。

#### 查询所有迁移任务

详情请参见 [RAL #弹性伸缩](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。

示例：
```sql
SHOW MIGRATION LIST;
```

返回信息：
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

#### 查询迁移任务进度

示例：
```sql
SHOW MIGRATION STATUS {jobId};
```

返回信息：
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
当前迁移任务已完成，新的分片规则已生效。如果迁移失败，新的分片规则不会生效。

`status` 的取值：

| 取值                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| PREPARING                                         | 准备中                                                        |
| RUNNING                                           | 运行中                                                        |
| EXECUTE_INVENTORY_TASK                            | 全量迁移中                                                     |
| EXECUTE_INCREMENTAL_TASK                          | 增量迁移中                                                     |
| FINISHED                                          | 已完成（整个流程完成了，新规则已生效）                              |
| PREPARING_FAILURE                                 | 准备阶段失败                                                    |
| EXECUTE_INVENTORY_TASK_FAILURE                    | 全量迁移阶段失败                                                 |
| EXECUTE_INCREMENTAL_TASK_FAILURE                  | 增量迁移阶段失败                                                 |

如果 `status` 出现失败的情况，可以查看 `proxy` 的日志查看错误堆栈分析问题。

#### 预览新的分片规则是否生效

示例：
```sql
preview SELECT COUNT(1) FROM t_order;
```

返回信息：
```
mysql> preview SELECT COUNT(1) FROM t_order;
+------------------+-------------------------------------------------------------------------+
| data_source_name | actual_sql                                                              |
+------------------+-------------------------------------------------------------------------+
| ds_2             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_3             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
| ds_4             | SELECT COUNT(1) FROM t_order_0 UNION ALL SELECT COUNT(1) FROM t_order_1 |
+------------------+-------------------------------------------------------------------------+
3 rows in set (0.21 sec)
```

#### 其他 DistSQL
详情请参见 [RAL #弹性伸缩](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。

### DistSQL 手动模式完整流程示例

手动模式下，数据校验、切换配置等操作可以手动执行。详情请参见：[RAL #弹性伸缩](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。

本示例演示从已有 MySQL 数据库迁移到 proxy。

除了明确说明在 MySQL 执行的 SQL，其他都是在 proxy 执行。

#### 新建源端库

已有数据不需要这个步骤。这里是模拟一个源端库用于测试。

在 MySQL 执行 SQL：
```sql
DROP DATABASE IF EXISTS scaling_ds_0;
CREATE DATABASE scaling_ds_0 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_1;
CREATE DATABASE scaling_ds_1 DEFAULT CHARSET utf8;
```

#### 登录 proxy

```shell
mysql -h127.0.0.1 -P3307 -uroot -proot
```

#### 创建并配置逻辑库

创建逻辑库：
```sql
CREATE DATABASE scaling_db;

USE scaling_db
```

加入源端数据库资源：
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

配置规则：
把现有系统中的表配置到规则里，使用 tables 规则 INLINE 算法，方便适配已有的表名。
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

#### 创建测试表并初始化数据

该步骤在实际使用中不需要。

```sql
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) CHARSET utf8mb4, PRIMARY KEY (order_id));
CREATE TABLE t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) CHARSET utf8mb4, creation_date DATE, PRIMARY KEY (item_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (1,1,2,'ok'),(2,2,4,'ok'),(3,3,6,'ok'),(4,4,1,'ok'),(5,5,3,'ok'),(6,6,5,'ok');
```

#### 执行迁移

预览分片：
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

在 MySQL 创建目标端库：
```sql
DROP DATABASE IF EXISTS scaling_ds_10;
CREATE DATABASE scaling_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_11;
CREATE DATABASE scaling_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS scaling_ds_12;
CREATE DATABASE scaling_ds_12 DEFAULT CHARSET utf8;
```

加入目标端数据库资源：
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

修改分片规则触发迁移：
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

查看当前迁移任务的进度：
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
当 status 达到 EXECUTE_INCREMENTAL_TASK，全量迁移已完成，在增量迁移阶段。


选择一个业务低峰期，对源端库或数据操作入口做停写。

proxy 停写：
```sql
mysql> STOP MIGRATION SOURCE WRITING "0130317c30317c3054317c7363616c696e675f6462";
Query OK, 0 rows affected (0.07 sec)
```

数据一致性校验：
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

切换元数据：
```sql
mysql> APPLY MIGRATION "0130317c30317c3054317c7363616c696e675f6462";
Query OK, 0 rows affected (0.22 sec)
```

预览分片是否已生效：
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
数据已经分片到新的数据库资源。

可选择性删除不再使用的 ds_0 和 ds_1。
