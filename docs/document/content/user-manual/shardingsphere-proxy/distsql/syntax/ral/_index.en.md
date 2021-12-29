+++
title = "RAL Syntax"
weight = 3
chapter = true
+++

RAL (Resource & Rule Administration Language) responsible for the added-on feature of hint, transaction type switch, scaling, sharding execute planning and so on.

## Hint

| Statement                                            | Function                                                                                                    | Example                                        |
|:---------------------------------------------------- |:----------------------------------------------------------------------------------------------------------- |:---------------------------------------------- |
| set readwrite_splitting hint source = [auto / write] | For current connection, set readwrite splitting routing strategy (automatic or forced to write data source) | set readwrite_splitting hint source = write    |
| set sharding hint database_value = yy                | For current connection, set sharding value for database sharding only, yy: sharding value                   | set sharding hint database_value = 100         |
| add sharding hint database_value xx= yy              | For current connection, add sharding value for table, xx: logic table, yy: database sharding value          | add sharding hint database_value t_order = 100 |
| add sharding hint table_value xx = yy                | For current connection, add sharding value for table, xx: logic table, yy: table sharding value             | add sharding hint table_value t_order = 100    |
| clear hint                                           | For current connection, clear all hint settings                                                             | clear hint                                     |
| clear [sharding hint / readwrite_splitting hint]     | For current connection, clear hint settings of sharding or readwrite splitting                              | clear readwrite_splitting hint                 |
| show [sharding / readwrite_splitting] hint status    | For current connection, query hint settings of sharding or readwrite splitting                              | show readwrite_splitting hint status           |

## Scaling

| Statement                                            | Function                                                          | Example                                  |
|:---------------------------------------------------- |:----------------------------------------------------------------- |:---------------------------------------- |
| show scaling list                                    | Query running list                                                | show scaling list                        |
| show scaling status xx                               | Query scaling status, xx: jobId                                   | show scaling status 1234                 |
| start scaling xx                                     | Start scaling, xx: jobId                                          | start scaling 1234                       |
| stop scaling xx                                      | Stop scaling, xx: jobId                                           | stop scaling 1234                        |
| drop scaling xx                                      | Drop scaling, xx: jobId                                           | drop scaling 1234                        |
| reset scaling xx                                     | reset progress, xx: jobId                                         | reset scaling 1234                       |
| check scaling xx                                     | Data consistency check with algorithm in `server.yaml`, xx: jobId | check scaling 1234                       |
| show scaling check algorithms                        | Show available consistency check algorithms                       | show scaling check algorithms            |
| check scaling {jobId} by type(name={algorithmType})  | Data consistency check with defined algorithm                     | check scaling 1234 by type(name=DEFAULT) |
| stop scaling source writing xx                       | The source ShardingSphere data source is discontinued, xx: jobId  | stop scaling source writing 1234         |
| checkout scaling xx                                  | Switch to target ShardingSphere data source, xx: jobId            | checkout scaling 1234                    |

## Circuit Breaker

| Statement                                                     | Function                           | Example                                    |
|:------------------------------------------------------------- |:---------------------------------- |:------------------------------------------ |
| [enable / disable] readwrite_splitting read xxx [from schema] | Enable or disable read data source | enable readwrite_splitting read resource_0 |
| [enable / disable] instance [IP=xxx, PORT=xxx / instanceId]   | Enable or disable proxy instance   | disable instance 127.0.0.1@3307            |
| show instance list                                            | Query proxy instance information   | show instance list                         |
| show readwrite_splitting read resources [from schema]         | Query all read resources status    | show readwrite_splitting read resources    |


## 解析引擎配置

| Statement                                                                                                                                                                                                                | Function                                                                                                                                                                                                                            | Example                                                                                                                                                                                                                   |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SHOW SQL_PARSER RULE                                                                                                                                                                                              | Query the relevant configuration of the current parsing engine                                                                                                                                                                      | SHOW SQL_PARSER RULE                                                                                                                                                                                                 |
| ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=xx,PARSER_TREE_CACHE(INITIAL_CAPACITY=xx, MAXIMUM_SIZE=xx,CONCURRENCY_LEVEL=xx), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=xxx, MAXIMUM_SIZE=xxx,CONCURRENCY_LEVEL=xxx) | Update the current parsing engine configuration, SQL_COMMENT_PARSE_ENABLE: whether to parse the SQL comment configuration, PARSER_TREE_CACHE: syntax tree local cache configuration, SQL_STATEMENT_CACHE: sql statement local cache | ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=false,PARSER_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11,CONCURRENCY_LEVEL=1), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11,CONCURRENCY_LEVEL=100)    |


## Other

| Statement                                                                   | Function                                                                           | Example                                   |
|:--------------------------------------------------------------------------- |:---------------------------------------------------------------------------------- |:----------------------------------------- |
| set variable proxy_property_name = xx                                       | proxy_property_name is one of [properties configuration](/en/user-manual/shardingsphere-proxy/props/)  of proxy，name is split by underscore            | set variable sql_show = true            |  
| set variable transaction_type = xx                                          | Modify transaction_type of the current connection, supports LOCAL, XA, BASE        | set variable transaction_type = XA        |
| set variable agent_plugins_enabled = [true / false]                         | Set whether the agent plugins are enabled, the default value is false              | set variable agent_plugins_enabled = true |
| show all variables                                                          | Query proxy all properties configuration                                           | show all variable                         |
| show variable proxy_property_name                                           | Query proxy properties configuration, name is split by underscore                  | show variable sql_show                    |
| show variable transaction_type                                              | Query the transaction type of the current connection                               | show variable transaction_type            |
| show variable cached_connections                                            | Query the number of cached physical database connections in the current connection | show variable cached_connections          |
| show variable agent_plugins_enabled                                         | Query whether the agent plugin are enabled                                         | show variable agent_plugins_enabled       |
| preview SQL                                                                 | Preview the actual SQLs                                                            | preview select * from t_order             |
| parse SQL                                                                   | Parse the actual SQLs                                                              | parse select * from t_order               |
| refresh table metadata                                                      | Refresh the metadata of all tables                                                 | refresh table metadata                    |
| refresh table metadata [tableName / tableName from resource resourceName]   | Refresh the metadata of a table                                                    | refresh table metadata t_order from resource ds_1                   |

## Notice

ShardingSphere-Proxy does not support hint by default, to support it, set `proxy-hint-enabled` to true in `conf/server.yaml`.
