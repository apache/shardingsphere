+++
pre = "<b>7.1. </b>"
title = "Sharding"
weight = 1
chapter = true
+++

## Scenarios
Suitable for scenarios where the data level of a single node is split and stored in multiple data nodes, and it is expected that business SQL can still be written for a single storage node without modification.
## Prerequisites
Suppose you expect a horizontal split of the `t_order` table to a total of 4 tables in two DB instances, and you are required to write SQL against the `t_order` table after the split.
## Data Planning
We plan to divide the database according to `user_id % 2` and `order_id % 2` for the table.
## Procedure
1. Download ShardingSphere-proxy.
2. Configure the sharding feature of the proxy as shown in the configuration example, and then start the proxy.
3. After the proxy is connected, create the `t_order` table.
``` sql
## You can view the real creation table syntax through the PREVIEW syntax
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
4. Inserts data into the `t_order` table.
``` sql
sharding_db=> PREVIEW INSERT INTO t_order values (1, 1,'OK'),(2, 2, 'OK');
 data_source_name |                actual_sql
------------------+-------------------------------------------
 ds_1             | INSERT INTO t_order_1 values (1, 1, 'OK')
 ds_0             | INSERT INTO t_order_0 values (2, 2, 'OK')
(2 rows)
INSERT INTO t_order values (1, 1,'OK'),(2, 2, 'OK');
```
5. Query `t_order` table.
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
# Sample
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
## Related References
[YAML Configuration: Sharding](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/)