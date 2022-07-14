+++
pre = "<b>7.3. </b>"
title = "Readwrite-splitting"
weight = 3
chapter = true
+++

## Scenarios
Suitable for a one-primary and multi-secondary database architecture. The primary database is responsible for transactional operations such as writing, modifying, and deleting data, and the secondary database is responsible for query operations.
Apache ShardingSphere's read/write splitting feature provides a variety of load balancing strategies.
## Prerequisites
Suppose the user has a database architecture of one primary and two secondary databases, and the user expects the two secondary databases to be able to bear different proportions of load.
## Data Planning
We will adopt a read/write splitting configuration and a load balancing strategy of `WEIGHT` for both secondary databases, so that the two secondary databases bear different loads.
## Procedure
1. Download ShardingSphere-proxy.
2. Use the read/write splitting configuration shown in the configuration example.
3. After the proxy is connected, create the `t_order` table.
``` sql
## You can view the actual creation table syntax through the PREVIEW syntax, and you can see that the routing results all point to the write_ds
readwrite_splitting_db=> PREVIEW CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
 data_source_name |                                                     actual_sql
------------------+---------------------------------------------------------------------------------------------------------------------
 write_ds         | CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))
(1 row)
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
``` 
4. Inserts data into the `t_order` table.
``` sql
readwrite_splitting_db=> PREVIEW INSERT INTO t_order values (1, 1,'OK');
 data_source_name |               actual_sql
------------------+----------------------------------------
 write_ds         | INSERT INTO t_order values (1, 1,'OK')
(1 row)
INSERT INTO t_order values (1, 1,'OK')
```
5. Query `t_order` table.
``` sql
## Multiple queries are routed to different secondary databases.
readwrite_splitting_db=> PREVIEW SELECT * FROM t_order;
 data_source_name |      actual_sql
------------------+-----------------------
 read_ds_1        | SELECT * FROM t_order
(1 row)

readwrite_splitting_db=> PREVIEW SELECT * FROM t_order;
 data_source_name |      actual_sql
------------------+-----------------------
 read_ds_0        | SELECT * FROM t_order
(1 row)
```

## Sample
config-encrypt.yaml
```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    readwrite_ds:
      staticStrategy:
        writeDataSourceName: write_ds
        readDataSourceNames:
          - read_ds_0
          - read_ds_1
      loadBalancerName: weight_lb
  loadBalancers:
    weight_lb:
      type: WEIGHT
      props:
        read_ds_0: 2
        read_ds_1: 1
```
## Related References
[YAML Configuration: Readwrite-splitting](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)