+++
pre = "<b>7.1. </b>"
title = "数据分片"
weight = 1
chapter = true
+++

## 适用场景
适用于将单一节点的数据水平拆分存储至多个数据节点，并且期望业务 SQL 不做改造，依然可以针对单一存储节点书写的场景。
## 前提条件
假设用户期望对 `t_order` 表进行水平拆分至两个数据库实例共 4 张表，并且要求拆分后 SQL 依然针对 `t_order` 表书写。
## 数据规划
我们计划按照 `user_id % 2` 进行分库， `order_id % 2` 进行分表。
## 操作步骤
1. 下载 ShardingSphere-proxy
2. 按照配置示例所示，配置 proxy 的分片功能，然后启动 proxy。
3. 连接 proxy 后，创建 `t_order` 表
``` sql
## 可以通过 PREVIEW 语法查看真实创建表语法
sharding_db=> PREVIEW CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
 data_source_name |                                                      actual_sql
------------------+-----------------------------------------------------------------------------------------------------------------------
 ds_1             | CREATE TABLE t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
 ds_1             | CREATE TABLE t_order_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
 ds_0             | CREATE TABLE t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
 ds_0             | CREATE TABLE t_order_1 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
(4 rows)
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
``` 
4. 向 `t_order` 表中插入数据
``` sql
sharding_db=> PREVIEW INSERT INTO t_order values (1, 1,'OK'),(2, 2, 'OK');
 data_source_name |                actual_sql
------------------+-------------------------------------------
 ds_1             | INSERT INTO t_order_1 values (1, 1, 'OK')
 ds_0             | INSERT INTO t_order_0 values (2, 2, 'OK')
(2 rows)
INSERT INTO t_order values (1, 1,'OK'),(2, 2, 'OK');
```
5. 查询 `t_order` 表
``` sql
sharding_db=> PREVIEW SELECT * FROM t_order;
 data_source_name |                        actual_sql
------------------+-----------------------------------------------------------
 ds_0             | SELECT * FROM t_order_0 UNION ALL SELECT * FROM t_order_1
 ds_1             | SELECT * FROM t_order_0 UNION ALL SELECT * FROM t_order_1
(2 rows)

sharding_db=> PREVIEW SELECT * FROM t_order WHERE user_id = 1 and order_id = 1;
 data_source_name |                         actual_sql
------------------+------------------------------------------------------------
 ds_1             | SELECT * FROM t_order_1 WHERE user_id = 1 and order_id = 1
(1 row)
```
## 配置示例
config-sharding.yaml
``` yaml
rules:
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: ds_${0..1}.t_order_${0..1}
        databaseStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: database_inline
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: table_inline

    shardingAlgorithms:
      database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${user_id % 2}
      table_inline:
        type: INLINE
        props:
          algorithm-expression: t_order_${order_id % 2}
```
## 相关参考
[YAML 配置：数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)