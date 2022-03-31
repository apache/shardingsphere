+++
title = "RAL Syntax"
weight = 3
chapter = true
+++

RAL (Resource & Rule Administration Language) responsible for the added-on feature of hint, transaction type switch, scaling, sharding execute planning and so on.

## Hint

| Statement                                            | Function                                                                                                    | Example                                        |
|:---------------------------------------------------- |:----------------------------------------------------------------------------------------------------------- |:---------------------------------------------- |
| SET READWRITE_SPLITTING HINT SOURCE = [auto / write] | For current connection, set readwrite splitting routing strategy (automatic or forced to write data source) | SET READWRITE_SPLITTINGHINT SOURCE = write    |
| SET SHARDING HINT DATABASE_VALUE = yy                | For current connection, set sharding value for database sharding only, yy: sharding value                   | SET SHARDING HINT DATABASE_VALUE = 100         |
| ADD SHARDING HINT DATABASE_VALUE tableName= yy              | For current connection, add sharding value for table, xx: logic table, yy: database sharding value          | ADD SHARDING HINT DATABASE_VALUE t_order = 100 |
| ADD SHARDING HINT TABLE_VALUE tableName = yy                | For current connection, add sharding value for table, xx: logic table, yy: table sharding value             | ADD SHARDING HINT TABLE_VALUE t_order = 100    |
| CLEAR HINT SETTINGS                                            | For current connection, clear all hint settings                                                             | CLEAR HINT                                     |
| CLEAR [SHARDING HINT / READWRITE_SPLITTING HINT]     | For current connection, clear hint settings of sharding or readwrite splitting                              | CLEAR READWRITE_SPLITTING HINT                 |
| SHOW [SHARDING / READWRITE_SPLITTING] HINT STATUS    | For current connection, query hint settings of sharding or readwrite splitting                              | SHOW READWRITE_SPLITTING HINT STATUS           |

## Scaling

| Statement                                            | Function                                                          | Example                                  |
|:---------------------------------------------------- |:----------------------------------------------------------------- |:---------------------------------------- |
| SHOW SCALING LIST                                    | Query running list                                                | SHOW SCALING LIST                        |
| SHOW SCALING STATUS jobId                               | Query scaling status, xx: jobId                                   | SHOW SCALING LIST 1234                 |
| START SCALING jobId                                     | Start scaling, xx: jobId                                          | START SCALING 1234                       |
| STOP SCALING jobId                                      | Stop scaling, xx: jobId                                           | STOP SCALING 1234                        |
| DROP SCALING jobId                                      | Drop scaling, xx: jobId                                           | DROP SCALING 1234                        |
| RESET SCALING jobId                                     | reset progress, xx: jobId                                         | RESET SCALING 1234                       |
| CHECK SCALING jobId                                     | Data consistency check with algorithm in `server.yaml`, xx: jobId | CHECK SCALING 1234                       |
| SHOW SCALING CHECK ALGORITHMS                        | Show available consistency check algorithms                       | SHOW SCALING CHECK ALGORITHMS            |
| CHECK SCALING {jobId} by type(name={algorithmType})  | Data consistency check with defined algorithm                     | CHECK SCALING 1234 by type(name=DEFAULT) |
| STOP SCALING SOURCE WRITING jobId                       | The source ShardingSphere data source is discontinued, xx: jobId  | STOP SCALING SOURCE WRITING 1234         |
| RESTORE SCALING SOURCE WRITING jobId                    | Restore source data source writing, xx: jobId                     | RESTORE SCALING SOURCE WRITING 1234      |
| APPLY SCALING jobId                                       | Switch to target ShardingSphere metadata, xx: jobId               | APPLY SCALING 1234                       |

## Circuit Breaker

| Statement                                                     | Function                           | Example                                    |
|:------------------------------------------------------------- |:---------------------------------- |:------------------------------------------ |
| [ENABLE / DISABLE] READWRITE_SPLITTING (READ)? resourceName [FROM schemaName] | Enable or disable read data source | ENABLE READWRITE_SPLITTING READ resource_0 |
| [ENABLE / DISABLE] INSTANCE [IP=xxx, PORT=xxx / instanceId]   | Enable or disable proxy instance   | DISABLE INSTANCE 127.0.0.1@3307            |
| SHOW INSTANCE LIST                                            | Query proxy instance information   | SHOW INSTANCE LIST                         |
| SHOW READWRITE_SPLITTING (READ)? resourceName [FROM schemaName]         | Query all read resources status    | SHOW READWRITE_SPLITTING READ RESOURCES    |

## Global Rule

| Statement                                                                                                                                                                                                           | Function                                                                                                                                                                                                             | Example                                                                                                                                                                                                             |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SHOW AUTHORITY RULE                                                                                                                                                                                                 | Query authority rule configuration                                                                                                                                                                                   | SHOW AUTHORITY RULE                                                                                                                                                                                                 |
| SHOW TRANSACTION RULE                                                                                                                                                                                               | Query transaction rule configuration                                                                                                                                                                                 | SHOW TRANSACTION RULE                                                                                                                                                                                               |
| SHOW SQL_PARSER RULE                                                                                                                                                                                                | Query SQL parser rule configuration                                                                                                                                                                                  | SHOW SQL_PARSER RULE                                                                                                                                                                                                |
| ALTER TRANSACTION RULE(DEFAULT=xx,TYPE(NAME=xxx, PROPERTIES("key1"="value1","key2"="value2"...)))                                                                                                                   | Alter transaction rule configuration，`DEFAULT`: default transaction type，support LOCAL、XA、BASE; `NAME`: name of transaction manager, support Atomikos, Narayana and Bitronix                                      | ALTER TRANSACTION RULE(DEFAULT=XA,TYPE(NAME=Narayana, PROPERTIES("databaseName"="jbossts","host"="127.0.0.1")))                                                                                                     |
| ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=xx, PARSE_TREE_CACHE(INITIAL_CAPACITY=xx, MAXIMUM_SIZE=xx, CONCURRENCY_LEVEL=xx), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=xxx, MAXIMUM_SIZE=xxx, CONCURRENCY_LEVEL=xxx) | Alter SQL parser rule configuration, `SQL_COMMENT_PARSE_ENABLE`: whether to parse the SQL comment, `PARSE_TREE_CACHE`: local cache configuration of syntax tree, `SQL_STATEMENT_CACHE`: local cache of SQL statement | ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=false, PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100) |

## Other

| Statement                                                                   | Function                                                                           | Example                                   |
|:--------------------------------------------------------------------------- |:---------------------------------------------------------------------------------- |:----------------------------------------- |
| SHOW INSTANCE MODE                                                         | Query the mode configuration of the proxy                                          | SHOW INSTANCE MODE                        |
| COUNT SCHEMA RULES [FROM schema]                                            | Query the number of rules in a schema                                              | count schema rules                               |
| SET VARIABLE proxy_property_name = xx                                       | proxy_property_name is one of [properties configuration](/en/user-manual/shardingsphere-proxy/yaml-config/props/) of proxy, name is split by underscore            | SET VARIABLE sql_show = true            |  
| SET VARIABLE transaction_type = xx                                          | Modify transaction_type of the current connection, supports LOCAL, XA, BASE        | SET VARIABLE transaction_type = XA        |
| SET VARIABLE agent_plugins_enabled = [TRUE / FALSE]                         | Set whether the agent plugins are enabled, the default value is false              | SET VARIABLE agent_plugins_enabled = TRUE |
| SHOW ALL VARIABLES                                                          | Query proxy all properties configuration                                           | SHOW ALL VARIABLES                        |
| SHOW VARIABLE variable_name                                                 | Query proxy variable, name is split by underscore                                  | SHOW VARIABLE sql_show                    |
| PREVIEW SQL                                                                 | Preview the actual SQLs                                                            | PREVIEW SELECT * FROM t_order             |
| PARSE SQL                                                                   | Parse SQL and output abstract syntax tree                                          | PARSE SELECT * FROM t_order               |
| REFRESH TABLE METADATA                                                      | Refresh the metadata of all tables                                                 | REFRESH TABLE METADATA                    |
| REFRESH TABLE METADATA [tableName / tableName FROM resource resourceName]   | Refresh the metadata of a table                                                    | REFRESH TABLE METADATA t_order FROM resource ds_1  |
| SHOW TABLE METADATA tableName [, tableName] ...                             | Query table metadata                                                               | SHOW TABLE METADATA t_order                        |
| EXPORT SCHEMA CONFIG [FROM schema_name] [, file="file_path"]                | Query / export resources and rule configuration in schema                          | EXPORT SCHEMA CONFIG FROM readwrite_splitting_db   |
| SHOW RULES USED RESOURCE resourceName [from schema]                         | Query the rules for using the specified resource in schema                         | SHOW RULES USED RESOURCE ds_0 FROM schemaName  |

## Notice

ShardingSphere-Proxy does not support hint by default, to support it, set `proxy-hint-enabled` to true in `conf/server.yaml`.
