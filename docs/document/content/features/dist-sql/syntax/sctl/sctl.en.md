+++
title = "SCTL"
weight = 1
+++

## Definition

SCTL（ShardingSphere Control Language）responsible for the added-on feature of hint, transaction type switch, sharding execute planning and so on.

## Usage

| Statement                               | Function                                                                                                          | Example                                        |
|:----------------------------------------|:------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------|
|sctl:set transaction_type=XX             | Modify transaction_type of the current connection, supports LOCAL, XA, BASE                                       | sctl:set transaction_type=XA                   |
|sctl:show transaction_type               | Query the transaction type of the current connection                                                              | sctl:show transaction_type                     |
|sctl:show cached_connections             | Query the number of cached physical database connections in the current connection                                | sctl:show cached_connections                   |
|sctl:explain SQL                         | View the execution plan for logical SQL.                                                                          | sctl:explain select * from t_order             |
|sctl:hint set PRIMARY_ONLY=true          | For current connection, set database operation force route to primary database only or not                        | sctl:hint set PRIMARY_ONLY=true                |
|sctl:hint set DatabaseShardingValue=yy   | For current connection, set sharding value for database sharding only, yy: sharding value                         | sctl:hint set DatabaseShardingValue=100        |
|sctl:hint addDatabaseShardingValue xx=yy | For current connection, add sharding value for database, xx: logic table, yy: sharding value                      | sctl:hint addDatabaseShardingValue t_order=100 |
|sctl:hint addTableShardingValue xx=yy    | For current connection, add sharding value for table, xx: logic table, yy: sharding value                         | sctl:hint addTableShardingValue t_order=100    |
|sctl:hint clear                          | For current connection, clear all hint settings                                                                   | sctl:hint clear                                |
|sctl:hint show status                    | For current connection, query hint status, primary_only:true/false, sharding_type:databases_only/databases_tables | sctl:hint show status                          |
|sctl:hint show table status              | For current connection, query sharding values of logic tables                                                     | sctl:hint show table status                    |

## Notice

ShardingSphere-Proxy does not support hint by default, to support it, set the `properties` property `proxy-hint-enabled` to true in `conf/server.yaml`.
