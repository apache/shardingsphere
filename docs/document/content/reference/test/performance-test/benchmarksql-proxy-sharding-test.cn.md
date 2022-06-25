+++
title = "BenchmarkSQL ShardingSphere Proxy 分片性能测试"
weight = 2
+++

## 测试目的

使用 BenchmarkSQL 工具测试 ShardingSphere Proxy 的分片性能。

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

## 压测环境或参数建议

**注意：本节中提到的任何参数都不是绝对值，都需要根据实际测试结果进行调整或取舍。**

### 建议使用 Java 17 运行 ShardingSphere

编译 ShardingSphere 可以使用 Java 8。

使用 Java 17 可以在默认情况下尽量提升 ShardingSphere 的性能。

### ShardingSphere 数据分片建议

对 BenchmarkSQL 的数据分片，可以考虑以各个表中的 warehouse id 作为分片键。  

其中一个表 `bmsql_item` 没有 warehouse id，数据量固定 10 万行：
- 可以取 `i_id` 作为分片键。但可能会导致同一个 Proxy 连接同时持有多个不同数据源的连接。
- 或考虑不做分片，存在单个数据源内。可能会导致某一数据源压力较大。
- 或对 `i_id` 进行范围分片，例如 1-50000 分布在数据源 0、50001-100000 分布在数据源 1。

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

对 BenchmarkSQL 所使用的配置文件中的 JDBC URL 进行调整，即参数名 `conn` 的值：
- 增加参数 `defaultRowFetchSize=50` 可能减少多行结果集的 fetch 次数，需要根据实际测试结果适当增大或减小。
- 增加参数 `reWriteBatchedInserts=true` 可能减少批量插入的耗时，例如准备数据或 New Order 业务的批量插入，需要根据实际测试结果决定是否启用。

props.pg 文件节选，建议修改的位置为第 3 行 `conn` 的参数值：
```properties
db=postgres
driver=org.postgresql.Driver
conn=jdbc:postgresql://localhost:5432/postgres?defaultRowFetchSize=50&reWriteBatchedInserts=true
user=benchmarksql
password=PWbmsql
```

### ShardingSphere Proxy server.yaml 参数建议

`proxy-backend-query-fetch-size` 参数值默认值为 -1，修改为 `50` 左右可以尽量减少多行结果集的 fetch 次数。 
`proxy-frontend-executor-size` 参数默认值为 CPU * 2，可以根据实际测试结果减少至 CPU * 0.5 左右；如果涉及 NUMA，可以根据实际测试结果设置为单个 CPU 的物理核数。

`server.yaml` 文件节选：
```yaml
props:
  proxy-backend-query-fetch-size: 50
  # proxy-frontend-executor-size: 32 # 4 路 32C aarch64
  # proxy-frontend-executor-size: 12 # 2 路 12C24T x86
```

## 附录

### BenchmarkSQL 数据分片参考配置

Pool size 请根据实际压测情况适当调整。

```yaml
databaseName: bmsql_sharding
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
        type: SNOWFLAKE
    tables:
      bmsql_config:
        actualDataNodes: ds_0.bmsql_config

      bmsql_warehouse:
        actualDataNodes: ds_${0..3}.bmsql_warehouse
        databaseStrategy:
          standard:
            shardingColumn: w_id
            shardingAlgorithmName: mod_4

      bmsql_district:
        actualDataNodes: ds_${0..3}.bmsql_district
        databaseStrategy:
          standard:
            shardingColumn: d_w_id
            shardingAlgorithmName: mod_4

      bmsql_customer:
        actualDataNodes: ds_${0..3}.bmsql_customer
        databaseStrategy:
          standard:
            shardingColumn: c_w_id
            shardingAlgorithmName: mod_4

      bmsql_item:
        actualDataNodes: ds_${0..3}.bmsql_item
        databaseStrategy:
          standard:
            shardingColumn: i_id
            shardingAlgorithmName: mod_4

      bmsql_history:
        actualDataNodes: ds_${0..3}.bmsql_history
        databaseStrategy:
          standard:
            shardingColumn: h_w_id
            shardingAlgorithmName: mod_4

      bmsql_oorder:
        actualDataNodes: ds_${0..3}.bmsql_oorder
        databaseStrategy:
          standard:
            shardingColumn: o_w_id
            shardingAlgorithmName: mod_4

      bmsql_stock:
        actualDataNodes: ds_${0..3}.bmsql_stock
        databaseStrategy:
          standard:
            shardingColumn: s_w_id
            shardingAlgorithmName: mod_4

      bmsql_new_order:
        actualDataNodes: ds_${0..3}.bmsql_new_order
        databaseStrategy:
          standard:
            shardingColumn: no_w_id
            shardingAlgorithmName: mod_4

      bmsql_order_line:
        actualDataNodes: ds_${0..3}.bmsql_order_line
        databaseStrategy:
          standard:
            shardingColumn: ol_w_id
            shardingAlgorithmName: mod_4

    shardingAlgorithms:
      mod_4:
        type: MOD
        props:
          sharding-count: 4
```

## BenchmarkSQL 5.0 PostgreSQL 语句列表

### Create tables

```sql
create table bmsql_config (
  cfg_name    varchar(30) primary key,
  cfg_value   varchar(50)
);

create table bmsql_warehouse (
  w_id        integer   not null,
  w_ytd       decimal(12,2),
  w_tax       decimal(4,4),
  w_name      varchar(10),
  w_street_1  varchar(20),
  w_street_2  varchar(20),
  w_city      varchar(20),
  w_state     char(2),
  w_zip       char(9)
);

create table bmsql_district (
  d_w_id       integer       not null,
  d_id         integer       not null,
  d_ytd        decimal(12,2),
  d_tax        decimal(4,4),
  d_next_o_id  integer,
  d_name       varchar(10),
  d_street_1   varchar(20),
  d_street_2   varchar(20),
  d_city       varchar(20),
  d_state      char(2),
  d_zip        char(9)
);

create table bmsql_customer (
  c_w_id         integer        not null,
  c_d_id         integer        not null,
  c_id           integer        not null,
  c_discount     decimal(4,4),
  c_credit       char(2),
  c_last         varchar(16),
  c_first        varchar(16),
  c_credit_lim   decimal(12,2),
  c_balance      decimal(12,2),
  c_ytd_payment  decimal(12,2),
  c_payment_cnt  integer,
  c_delivery_cnt integer,
  c_street_1     varchar(20),
  c_street_2     varchar(20),
  c_city         varchar(20),
  c_state        char(2),
  c_zip          char(9),
  c_phone        char(16),
  c_since        timestamp,
  c_middle       char(2),
  c_data         varchar(500)
);

create sequence bmsql_hist_id_seq;

create table bmsql_history (
  hist_id  integer,
  h_c_id   integer,
  h_c_d_id integer,
  h_c_w_id integer,
  h_d_id   integer,
  h_w_id   integer,
  h_date   timestamp,
  h_amount decimal(6,2),
  h_data   varchar(24)
);

create table bmsql_new_order (
  no_w_id  integer   not null,
  no_d_id  integer   not null,
  no_o_id  integer   not null
);

create table bmsql_oorder (
  o_w_id       integer      not null,
  o_d_id       integer      not null,
  o_id         integer      not null,
  o_c_id       integer,
  o_carrier_id integer,
  o_ol_cnt     integer,
  o_all_local  integer,
  o_entry_d    timestamp
);

create table bmsql_order_line (
  ol_w_id         integer   not null,
  ol_d_id         integer   not null,
  ol_o_id         integer   not null,
  ol_number       integer   not null,
  ol_i_id         integer   not null,
  ol_delivery_d   timestamp,
  ol_amount       decimal(6,2),
  ol_supply_w_id  integer,
  ol_quantity     integer,
  ol_dist_info    char(24)
);

create table bmsql_item (
  i_id     integer      not null,
  i_name   varchar(24),
  i_price  decimal(5,2),
  i_data   varchar(50),
  i_im_id  integer
);

create table bmsql_stock (
  s_w_id       integer       not null,
  s_i_id       integer       not null,
  s_quantity   integer,
  s_ytd        integer,
  s_order_cnt  integer,
  s_remote_cnt integer,
  s_data       varchar(50),
  s_dist_01    char(24),
  s_dist_02    char(24),
  s_dist_03    char(24),
  s_dist_04    char(24),
  s_dist_05    char(24),
  s_dist_06    char(24),
  s_dist_07    char(24),
  s_dist_08    char(24),
  s_dist_09    char(24),
  s_dist_10    char(24)
);
```

### Create indexes

```sql
alter table bmsql_warehouse add constraint bmsql_warehouse_pkey
  primary key (w_id);

alter table bmsql_district add constraint bmsql_district_pkey
  primary key (d_w_id, d_id);

alter table bmsql_customer add constraint bmsql_customer_pkey
  primary key (c_w_id, c_d_id, c_id);

create index bmsql_customer_idx1
  on  bmsql_customer (c_w_id, c_d_id, c_last, c_first);

alter table bmsql_oorder add constraint bmsql_oorder_pkey
  primary key (o_w_id, o_d_id, o_id);

create unique index bmsql_oorder_idx1
  on  bmsql_oorder (o_w_id, o_d_id, o_carrier_id, o_id);

alter table bmsql_new_order add constraint bmsql_new_order_pkey
  primary key (no_w_id, no_d_id, no_o_id);

alter table bmsql_order_line add constraint bmsql_order_line_pkey
  primary key (ol_w_id, ol_d_id, ol_o_id, ol_number);

alter table bmsql_stock add constraint bmsql_stock_pkey
  primary key (s_w_id, s_i_id);

alter table bmsql_item add constraint bmsql_item_pkey
  primary key (i_id);
```

### New Order 业务

stmtNewOrderSelectWhseCust
```sql
UPDATE bmsql_district 
    SET d_next_o_id = d_next_o_id + 1 
    WHERE d_w_id = ? AND d_id = ?
```

stmtNewOrderSelectDist
```sql
SELECT d_tax, d_next_o_id 
    FROM bmsql_district 
    WHERE d_w_id = ? AND d_id = ? 
    FOR UPDATE
```

stmtNewOrderUpdateDist
```sql
UPDATE bmsql_district 
    SET d_next_o_id = d_next_o_id + 1 
    WHERE d_w_id = ? AND d_id = ?
```

stmtNewOrderInsertOrder
```sql
INSERT INTO bmsql_oorder (
    o_id, o_d_id, o_w_id, o_c_id, o_entry_d, 
    o_ol_cnt, o_all_local) 
VALUES (?, ?, ?, ?, ?, ?, ?)
```

stmtNewOrderInsertNewOrder
```sql
INSERT INTO bmsql_new_order (
    no_o_id, no_d_id, no_w_id) 
VALUES (?, ?, ?)
```

stmtNewOrderSelectStock
```sql
SELECT s_quantity, s_data, 
       s_dist_01, s_dist_02, s_dist_03, s_dist_04, 
       s_dist_05, s_dist_06, s_dist_07, s_dist_08, 
       s_dist_09, s_dist_10 
    FROM bmsql_stock 
    WHERE s_w_id = ? AND s_i_id = ? 
    FOR UPDATE
```

stmtNewOrderSelectItem
```sql
SELECT i_price, i_name, i_data 
    FROM bmsql_item 
    WHERE i_id = ?
```

stmtNewOrderUpdateStock
```sql
UPDATE bmsql_stock 
    SET s_quantity = ?, s_ytd = s_ytd + ?, 
        s_order_cnt = s_order_cnt + 1, 
        s_remote_cnt = s_remote_cnt + ? 
    WHERE s_w_id = ? AND s_i_id = ?
```

stmtNewOrderInsertOrderLine
```sql
INSERT INTO bmsql_order_line (
    ol_o_id, ol_d_id, ol_w_id, ol_number, 
    ol_i_id, ol_supply_w_id, ol_quantity, 
    ol_amount, ol_dist_info) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
```

### Payment 业务

stmtPaymentSelectWarehouse
```sql
SELECT w_name, w_street_1, w_street_2, w_city, 
       w_state, w_zip 
    FROM bmsql_warehouse 
    WHERE w_id = ? 
```

stmtPaymentSelectDistrict
```sql
SELECT d_name, d_street_1, d_street_2, d_city, 
       d_state, d_zip 
    FROM bmsql_district 
    WHERE d_w_id = ? AND d_id = ?
```

stmtPaymentSelectCustomerListByLast
```sql
SELECT c_id 
    FROM bmsql_customer 
    WHERE c_w_id = ? AND c_d_id = ? AND c_last = ? 
    ORDER BY c_first
```

stmtPaymentSelectCustomer
```sql
SELECT c_first, c_middle, c_last, c_street_1, c_street_2, 
       c_city, c_state, c_zip, c_phone, c_since, c_credit, 
       c_credit_lim, c_discount, c_balance 
    FROM bmsql_customer 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ? 
    FOR UPDATE
```

stmtPaymentSelectCustomerData
```sql
SELECT c_data 
    FROM bmsql_customer 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```

stmtPaymentUpdateWarehouse
```sql
UPDATE bmsql_warehouse 
    SET w_ytd = w_ytd + ? 
    WHERE w_id = ?
```

stmtPaymentUpdateDistrict
```sql
UPDATE bmsql_district 
    SET d_ytd = d_ytd + ? 
    WHERE d_w_id = ? AND d_id = ?
```

stmtPaymentUpdateCustomer
```sql
UPDATE bmsql_customer 
    SET c_balance = c_balance - ?, 
        c_ytd_payment = c_ytd_payment + ?, 
        c_payment_cnt = c_payment_cnt + 1 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```

stmtPaymentUpdateCustomerWithData
```sql
UPDATE bmsql_customer 
    SET c_balance = c_balance - ?, 
        c_ytd_payment = c_ytd_payment + ?, 
        c_payment_cnt = c_payment_cnt + 1, 
        c_data = ? 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```

stmtPaymentInsertHistory
```sql
INSERT INTO bmsql_history (
    h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, 
    h_date, h_amount, h_data) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

### Order Status 业务

stmtOrderStatusSelectCustomerListByLast
```sql
SELECT c_id 
    FROM bmsql_customer 
    WHERE c_w_id = ? AND c_d_id = ? AND c_last = ? 
    ORDER BY c_first
```

stmtOrderStatusSelectCustomer
```sql
SELECT c_first, c_middle, c_last, c_balance 
    FROM bmsql_customer 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```

stmtOrderStatusSelectLastOrder
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

stmtOrderStatusSelectOrderLine
```sql
SELECT ol_i_id, ol_supply_w_id, ol_quantity, 
       ol_amount, ol_delivery_d 
    FROM bmsql_order_line 
    WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ? 
    ORDER BY ol_w_id, ol_d_id, ol_o_id, ol_number
```

### Stock level 业务

stmtStockLevelSelectLow
```sql
SELECT count(*) AS low_stock FROM (
    SELECT s_w_id, s_i_id, s_quantity 
        FROM bmsql_stock 
        WHERE s_w_id = ? AND s_quantity < ? AND s_i_id IN (
            SELECT ol_i_id 
                FROM bmsql_district 
                JOIN bmsql_order_line ON ol_w_id = d_w_id 
                 AND ol_d_id = d_id 
                 AND ol_o_id >= d_next_o_id - 20 
                 AND ol_o_id < d_next_o_id 
                WHERE d_w_id = ? AND d_id = ? 
        ) 
    ) AS L
```

### Delivery BG 业务

stmtDeliveryBGSelectOldestNewOrder
```sql
SELECT no_o_id 
    FROM bmsql_new_order 
    WHERE no_w_id = ? AND no_d_id = ? 
    ORDER BY no_o_id ASC
```

stmtDeliveryBGDeleteOldestNewOrder
```sql
DELETE FROM bmsql_new_order 
    WHERE no_w_id = ? AND no_d_id = ? AND no_o_id = ?
```

stmtDeliveryBGSelectOrder
```sql
SELECT o_c_id 
    FROM bmsql_oorder 
    WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?
```

stmtDeliveryBGUpdateOrder
```sql
UPDATE bmsql_oorder 
    SET o_carrier_id = ? 
    WHERE o_w_id = ? AND o_d_id = ? AND o_id = ?
```

stmtDeliveryBGSelectSumOLAmount
```sql
SELECT sum(ol_amount) AS sum_ol_amount 
    FROM bmsql_order_line 
    WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?
```

stmtDeliveryBGUpdateOrderLine
```sql
UPDATE bmsql_order_line
SET ol_delivery_d = ?
WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = ?
```

stmtDeliveryBGUpdateCustomer
```sql
UPDATE bmsql_customer 
    SET c_balance = c_balance + ?, 
        c_delivery_cnt = c_delivery_cnt + 1 
    WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
```
