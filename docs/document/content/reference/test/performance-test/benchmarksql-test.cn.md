+++
title = "BenchmarkSQL 性能测试"
weight = 2
+++

## 测试方法

ShardingSphere Proxy 支持通过 [BenchmarkSQL 5.0](https://sourceforge.net/projects/benchmarksql/) 进行 TPC-C 测试。
除本文说明的内容外，BenchmarkSQL 操作步骤按照原文档 `HOW-TO-RUN.txt` 即可。

## 测试工具微调

与单机数据库压测不同，分布式数据库解决方案难免在功能支持上有所取舍。使用 BenchmarkSQL 压测 ShardingSphere Proxy 建议进行如下调整。

### 移除外键与 extraHistID

修改 BenchmarkSQL 目录下 `run/runDatabaseBuild.sh`，文件第 17 行。

修改前：
```bash
AFTER_LOAD="indexCreates foreignKeys extraHistID buildFinish"
```

修改后：
```bash
AFTER_LOAD="indexCreates buildFinish"
```

## 压测相关参数建议

### ShardingSphere 数据分片建议

对 BenchmarkSQL 的数据分片，可以考虑以 warehouse id 作为分片键。其中一个表 `bmsql_item` 没有 warehouse id，可以取 `i_id` 作为分片键。  

BenchmarkSQL 中有如下 SQL 涉及多表：

```sql
SELECT c_discount, c_last, c_credit, w_tax     
FROM bmsql_customer     
    JOIN bmsql_warehouse ON (w_id = c_w_id)     
WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```

```sql
SELECT o_id, o_entry_d, o_carrier_id     
FROM bmsql_oorder     
WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ?       
  AND o_id = (          
      SELECT max(o_id)               
      FROM bmsql_oorder               
      WHERE o_w_id = ? AND o_d_id = ? AND o_c_id = ?          
      )
```

如果以 warehouse id 作为分片键，以上 SQL 涉及的表可以配置为 bindingTable：
```yaml
rules:
  - !SHARDING
    bindingTables:
      - bmsql_warehouse, bmsql_customer
      - bmsql_stock, bmsql_district, bmsql_order_line
```

以 warehouse id 为分片键的数据分片配置可以参考本文附录。

### PostgreSQL JDBC URL 参数建议

对 BenchmarkSQL 所使用的配置文件中的 JDBC URL 进行调整，即参数名 `conn` 的值。
增加参数 `defaultRowFetchSize=1` 可能减少 Delivery 业务耗时。

props.pg 文件节选，建议修改的位置为第 3 行 `conn` 的参数值：
```properties
db=postgres
driver=org.postgresql.Driver
conn=jdbc:postgresql://localhost:5432/postgres?defaultRowFetchSize=1
user=benchmarksql
password=PWbmsql

warehouses=1
loadWorkers=4

terminals=1
```

### ShardingSphere Proxy server.yaml 参数建议

`proxy-backend-query-fetch-size` 参数值默认值为 -1，修改为 `1000` 可能减少 Delivery 业务耗时。  

`server.yaml` 文件节选：
```yaml
props:
  proxy-backend-query-fetch-size: 1000
```

其他参数如 `max-connections-size-per-query` 等可以在压测过程中适当增大，比如取 Actual tables 最大的数量。
假如有个表分 4 库 x 4 表，共 16 个表，参数值可以尝试取 16。
实际效果与取决于数据分片方式，如果分片配置能够让所有 SQL 都路由到单点，该参数可能对性能没有影响。  

## 附录

### BenchmarkSQL 数据分片参考配置

Pool size 请根据实际压测情况适当调整。

```yaml
schemaName: bmsql_sharding
dataSources:
  ds_0:
    url: jdbc:postgresql://db0.ip:5432/bmsql
    username: postgres
    password: postgres
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 1000
    minPoolSize: 1000
  ds_1:
    url: jdbc:postgresql://db1.ip:5432/bmsql
    username: postgres
    password: postgres
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 1000
    minPoolSize: 1000
  ds_2:
    url: jdbc:postgresql://db2.ip:5432/bmsql
    username: postgres
    password: postgres
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 1000
    minPoolSize: 1000
  ds_3:
    url: jdbc:postgresql://db3.ip:5432/bmsql
    username: postgres
    password: postgres
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 1000
    minPoolSize: 1000

rules:
  - !SHARDING
    bindingTables:
      - bmsql_warehouse, bmsql_customer
      - bmsql_stock, bmsql_district, bmsql_order_line
    defaultDatabaseStrategy:
      none:
    defaultTableStrategy:
      none:
    keyGenerators:
      snowflake:
        props:
          worker-id: 123
        type: SNOWFLAKE
    tables:
      bmsql_config:
        actualDataNodes: ds_0.bmsql_config

      bmsql_warehouse:
        actualDataNodes: ds_${0..3}.bmsql_warehouse
        databaseStrategy:
          standard:
            shardingColumn: w_id
            shardingAlgorithmName: bmsql_warehouse_database_inline

      bmsql_district:
        actualDataNodes: ds_${0..3}.bmsql_district
        databaseStrategy:
          standard:
            shardingColumn: d_w_id
            shardingAlgorithmName: bmsql_district_database_inline

      bmsql_customer:
        actualDataNodes: ds_${0..3}.bmsql_customer
        databaseStrategy:
          standard:
            shardingColumn: c_w_id
            shardingAlgorithmName: bmsql_customer_database_inline

      bmsql_item:
        actualDataNodes: ds_${0..3}.bmsql_item
        databaseStrategy:
          standard:
            shardingColumn: i_id
            shardingAlgorithmName: bmsql_item_database_inline

      bmsql_history:
        actualDataNodes: ds_${0..3}.bmsql_history
        databaseStrategy:
          standard:
            shardingColumn: h_w_id
            shardingAlgorithmName: bmsql_history_database_inline

      bmsql_oorder:
        actualDataNodes: ds_${0..3}.bmsql_oorder_${0..3}
        databaseStrategy:
          standard:
            shardingColumn: o_w_id
            shardingAlgorithmName: bmsql_oorder_database_inline
        tableStrategy:
          standard:
            shardingColumn: o_c_id
            shardingAlgorithmName: bmsql_oorder_table_inline

      bmsql_stock:
        actualDataNodes: ds_${0..3}.bmsql_stock
        databaseStrategy:
          standard:
            shardingColumn: s_w_id
            shardingAlgorithmName: bmsql_stock_database_inline

      bmsql_new_order:
        actualDataNodes: ds_${0..3}.bmsql_new_order
        databaseStrategy:
          standard:
            shardingColumn: no_w_id
            shardingAlgorithmName: bmsql_new_order_database_inline

      bmsql_order_line:
        actualDataNodes: ds_${0..3}.bmsql_order_line
        databaseStrategy:
          standard:
            shardingColumn: ol_w_id
            shardingAlgorithmName: bmsql_order_line_database_inline

    shardingAlgorithms:
      bmsql_warehouse_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${w_id & 3}

      bmsql_district_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${d_w_id & 3}

      bmsql_customer_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${c_w_id & 3}

      bmsql_item_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${i_id & 3}

      bmsql_history_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${h_w_id & 3}

      bmsql_oorder_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${o_w_id & 3}

      bmsql_oorder_table_inline:
        type: INLINE
        props:
          algorithm-expression: bmsql_oorder_${o_c_id & 3}

      bmsql_stock_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${s_w_id & 3}

      bmsql_new_order_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${no_w_id & 3}

      bmsql_order_line_database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${ol_w_id & 3}
```
