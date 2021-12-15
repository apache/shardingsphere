+++
title = "使用手册"
weight = 2
+++

## 使用手册

### 环境要求

纯 JAVA 开发，JDK 建议 1.8 以上版本。

支持迁移场景如下：

| 源端                   | 目标端                |
| --------------------- | -------------------- |
| MySQL(5.1.15 ~ 5.7.x) | MySQL(5.1.15 ~ 5.7.x)   |
| PostgreSQL(9.4 ~ )    | PostgreSQL(9.4 ~ )           |
| openGauss(2.1.0)      | openGauss(2.1.0)            |

**注意**：

如果后端连接以下数据库，请下载相应JDBC驱动jar包，并将其放入 `${shardingsphere-proxy}/lib` 目录。

| 数据库                 | JDBC驱动                              | 参考                 |
| --------------------- | ------------------------------------ | -------------------- |
| MySQL                 | [mysql-connector-java-5.1.47.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar ) | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |
| openGauss             | [opengauss-jdbc-2.0.1-compatibility.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/2.0.1-compatibility/opengauss-jdbc-2.0.1-compatibility.jar ) | |

功能支持情况：

| 功能                   | MySQL         | PostgreSQL    | openGauss     |
| --------------------- | ------------- | ------------- | ------------- |
| 全量迁移               | 支持           | 支持           | 支持           |
| 增量迁移               | 支持           | 支持           | 支持           |
| 自动建表               | 支持           | 不支持         | 支持           |
| 默认数据一致性校验算法   | 支持           | 不支持         | 不支持          |

**注意**：

还没开启`自动建表`的数据库需要手动创建分表。

### 权限要求
#### MySQL
MySQL 需要开启 `binlog`，且迁移时所使用用户需要赋予 Replication 相关权限。
执行以下命令，确认是否有开启binlog：

```
show variables like '%log_bin%';
show variables like '%binlog%';
```

如以下显示，则说明binlog已开启

```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
| binlog_row_image                        | FULL                                  |
+-----------------------------------------+---------------------------------------+
```

执行以下命令，查看该用户是否有迁移权限
```
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

#### PostgreSQL
PostgreSQL 需要开启 [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html)

### DistSQL 接口

弹性迁移组件提供了 DistSQL 接口

#### 预览当前分片规则

示例：
```sql
preview select count(1) from t_order;
```

返回信息：
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

#### 创建迁移任务

1. 添加新的数据源

详情请参见[RDL#数据源资源](/cn/user-manual/shardingsphere-proxy/usage/distsql/syntax/rdl/rdl-resource/)。

先在底层数据库系统创建需要的分库，下面的 `DistSQL` 需要用到。

示例：
```sql
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
-- ds_3, ds_4
```

2. 修改分片规则

详情请参见[RDL#数据分片](/cn/user-manual/shardingsphere-proxy/usage/distsql/syntax/rdl/rdl-sharding-rule/)。

`SHARDING TABLE RULE`支持2种类型：`TableRule`和`AutoTableRule`。对于同一个逻辑表，不能混合使用这2种格式，以下是两种分片规则的对比：
| 类型         | AutoTableRule（自动分片）                                    | TableRule（自定义分片）                                      |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 定义         | 5.x版本之后，引入的自动分片技术交由 ShardingSphere 自动管理分片，用户只需要指定分片数量和使用的数据源，无需再关心表的具体分布详情可参见：[#自动化分片算法](/cn/features/sharding/concept/sharding/#自动化分片算法) | 需要自定义分片配置，可设置物理数据节点，数据分片、表分片等   |
| 推荐使用场景 | 不需要关心实际表在哪个库、哪个库有几张表等问题只需考虑：SHARDING_COLUMN：设置用作分片键的列TYPE：设置分片算法和数量GENERATED_KEY：设置分布式自增ID | 需要设置以下参数：DATANODES：物理数据节点表达式DATABASE_STRATEGY：数据库分片策略TABLE_STRATEGY：表分片策略GENERATED_KEY：设置分布式自增ID |

`AutoTableRule`修改示例：
```sql
ALTER SHARDING TABLE RULE t_order (
RESOURCES(ds_2, ds_3, ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=10)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

比如说`RESOURCES`和`sharding-count`修改了会触发迁移。

不完整的`TableRule`修改示例：
```sql
ALTER SHARDING TABLE RULE t_order (
DATANODES("ds_${2..4}.t_order_${0..1}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
GENERATED_KEY(COLUMN=order_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);
```

**注意**：当前版本不支持通过修改`TableRule`触发迁移。对于同一个逻辑表，不能混合使用这2种格式。

#### 查询所有迁移任务

详情请参见[RAL#弹性伸缩](/cn/user-manual/shardingsphere-proxy/usage/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。

示例：
```sql
show scaling list;
```

返回信息：
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

#### 查询迁移任务进度

示例：
```sql
show scaling status {jobId};
```

返回信息：
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
当前迁移任务已完成，新的分片规则已生效。如果迁移失败，新的分片规则不会生效。

`status`的取值：

| 取值                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| PREPARING                                         | 准备中                                                        |
| RUNNING                                           | 运行中                                                        |
| EXECUTE_INVENTORY_TASK                            | 全量迁移中                                                     |
| EXECUTE_INCREMENTAL_TASK                          | 增量迁移中                                                     |
| FINISHED                                          | 已完成（整个流程完成了，新规则已生效）                                                         |
| PREPARING_FAILURE                                 | 准备阶段失败                                                    |
| EXECUTE_INVENTORY_TASK_FAILURE                    | 全量迁移阶段失败                                                 |
| EXECUTE_INCREMENTAL_TASK_FAILURE                  | 增量迁移阶段失败                                                 |

如果`status`出现失败的情况，可以查看`proxy`的日志查看错误堆栈分析问题。


#### 手动模式
`Sharding-Scaling`提供了一些命令，可以手动执行。详情可见：[RAL#弹性伸缩](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。
```
check scaling {jobId};
```

check和checkout是给手动模式使用的，就是clusterAutoSwitchAlgorithm没配置的情况。并且各自有使用条件。

check做数据一致性校验，那需要判断全量和增量都完成的情况下执行才有可能校验通过。目前的默认实现是对两端的全量数据进行CRC32计算、然后比对结果

checkout是让新规则生效，前置步骤没完成是不会执行成功的。

#### 预览新的分片规则是否生效

示例：
```sql
preview select count(1) from t_order;
```

返回信息：
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

#### 其他DistSQL
详情请参见[RAL#弹性伸缩](/cn/user-manual/shardingsphere-proxy/usage/distsql/syntax/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)。
