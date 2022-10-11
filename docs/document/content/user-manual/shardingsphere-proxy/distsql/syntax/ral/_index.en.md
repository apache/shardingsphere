+++
title = "RAL Syntax"
weight = 3
chapter = true
+++

RAL (Resource & Rule Administration Language) responsible for hint, circuit breaker, configuration import and export, scaling control and other management functions.

## Hint

| Statement                                            | Function                                                                                                    | Example                                        |
| :--------------------------------------------------- | :---------------------------------------------------------------------------------------------------------- | :--------------------------------------------- |
| SET READWRITE_SPLITTING HINT SOURCE = [auto / write] | For current connection, set readwrite splitting routing strategy (automatic or forced to write data source) | SET READWRITE_SPLITTINGHINT SOURCE = write     |
| SET SHARDING HINT DATABASE_VALUE = yy                | For current connection, set sharding value for database sharding only, yy: sharding value                   | SET SHARDING HINT DATABASE_VALUE = 100         |
| ADD SHARDING HINT DATABASE_VALUE xx = yy             | For current connection, add sharding value for table, xx: logic table, yy: database sharding value          | ADD SHARDING HINT DATABASE_VALUE t_order = 100 |
| ADD SHARDING HINT TABLE_VALUE xx = yy                | For current connection, add sharding value for table, xx: logic table, yy: table sharding value             | ADD SHARDING HINT TABLE_VALUE t_order = 100    |
| CLEAR HINT                                           | For current connection, clear all hint settings                                                             | CLEAR HINT                                     |
| CLEAR [SHARDING HINT / READWRITE_SPLITTING HINT]     | For current connection, clear hint settings of sharding or readwrite splitting                              | CLEAR READWRITE_SPLITTING HINT                 |
| SHOW [SHARDING / READWRITE_SPLITTING] HINT STATUS    | For current connection, query hint settings of sharding or readwrite splitting                              | SHOW READWRITE_SPLITTING HINT STATUS           |

## Migration

| Statement                                               | Function                                       | Example                                         |
| :------------------------------------------------------ | :--------------------------------------------- | :---------------------------------------------- |
| MIGRATE TABLE ds.schema.table INTO table                | Migrate table from source to target            | MIGRATE TABLE ds_0.public.t_order INTO t_order  |
| SHOW MIGRATION LIST                                     | Query running list                             | SHOW MIGRATION LIST                             |
| SHOW MIGRATION STATUS jobId                             | Query migration status                         | SHOW MIGRATION STATUS 1234                      |
| STOP MIGRATION jobId                                    | Stop migration                                 | STOP MIGRATION 1234                             |
| START MIGRATION jobId                                   | Start stopped migration                        | START MIGRATION 1234                            |
| ROLLBACK MIGRATION jobId                                | Rollback migration                             | ROLLBACK MIGRATION 1234                         |
| COMMIT MIGRATION jobId                                  | Commit migration                               | COMMIT MIGRATION 1234                           |
| CHECK MIGRATION jobId                                   | Data consistency check                         | CHECK MIGRATION 1234                            |
| SHOW MIGRATION CHECK ALGORITHMS                         | Show available consistency check algorithms    | SHOW MIGRATION CHECK ALGORITHMS                 |
| CHECK MIGRATION jobId (by type(name=algorithmTypeName)? | Data consistency check with defined algorithm  | CHECK MIGRATION 1234 by type(name="DATA_MATCH") |

## Circuit Breaker

| Statement                                                                       | Function                           | Example                                    |
| :------------------------------------------------------------------------------ | :--------------------------------- | :----------------------------------------- |
| [ENABLE / DISABLE] READWRITE_SPLITTING (READ)? resourceName [FROM databaseName] | Enable or disable read data source | ENABLE READWRITE_SPLITTING READ resource_0 |
| [ENABLE / DISABLE] INSTANCE instanceId                                          | Enable or disable proxy instance   | DISABLE INSTANCE instance_1                |
| SHOW INSTANCE LIST                                                              | Query proxy instance information   | SHOW INSTANCE LIST                         |
| SHOW READWRITE_SPLITTING (READ)? resourceName [FROM databaseName]               | Query all read resources status    | SHOW READWRITE_SPLITTING READ RESOURCES    |

## Global Rule

| Statement                                                                                                                                                                                                           | Function                                                                                                                                                                                                             | Example                                                                                                                                                                                                             |
| :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| SHOW AUTHORITY RULE                                                                                                                                                                                                 | Query authority rule configuration                                                                                                                                                                                   | SHOW AUTHORITY RULE                                                                                                                                                                                                 |
| SHOW TRANSACTION RULE                                                                                                                                                                                               | Query transaction rule configuration                                                                                                                                                                                 | SHOW TRANSACTION RULE                                                                                                                                                                                               |
| SHOW SQL_PARSER RULE                                                                                                                                                                                                | Query SQL parser rule configuration                                                                                                                                                                                  | SHOW SQL_PARSER RULE                                                                                                                                                                                                |
| ALTER TRANSACTION RULE(DEFAULT=xx,TYPE(NAME=xxx, PROPERTIES(key1=value1,key2=value2...)))                                                                                                                           | Alter transaction rule configuration, `DEFAULT`: default transaction type, support LOCAL, XA, BASE; `NAME`: name of transaction manager, support Atomikos, Narayana and Bitronix                                     | ALTER TRANSACTION RULE(DEFAULT="XA",TYPE(NAME="Narayana", PROPERTIES("databaseName"="jbossts","host"="127.0.0.1")))                                                                                                 |
| ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=xx, PARSE_TREE_CACHE(INITIAL_CAPACITY=xx, MAXIMUM_SIZE=xx, CONCURRENCY_LEVEL=xx), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=xxx, MAXIMUM_SIZE=xxx, CONCURRENCY_LEVEL=xxx) | Alter SQL parser rule configuration, `SQL_COMMENT_PARSE_ENABLE`: whether to parse the SQL comment, `PARSE_TREE_CACHE`: local cache configuration of syntax tree, `SQL_STATEMENT_CACHE`: local cache of SQL statement | ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=false, PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100) |

## Other

| Statement                                                           | Function                                                                                                                                                | Example                                                    |
| :------------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------ | :--------------------------------------------------------- |
| SHOW INSTANCE INFO                                                  | Query the instance information of the proxy                                                                                                             | SHOW INSTANCE INFO                                         |
| SHOW MODE INFO                                                      | Query the mode configuration of the proxy                                                                                                               | SHOW MODE INFO                                             |
| SET VARIABLE proxy_property_name = xx                               | proxy_property_name is one of [properties configuration](/en/user-manual/shardingsphere-proxy/yaml-config/props/) of proxy, name is split by underscore | SET VARIABLE sql_show = true                               |  
| SET VARIABLE transaction_type = xx                                  | Modify transaction_type of the current connection, supports LOCAL, XA, BASE                                                                             | SET VARIABLE transaction_type = "XA"                       |
| SET VARIABLE agent_plugins_enabled = [TRUE / FALSE]                 | Set whether the agent plugins are enabled, the default value is false                                                                                   | SET VARIABLE agent_plugins_enabled = TRUE                  |
| SHOW ALL VARIABLES                                                  | Query proxy all properties configuration                                                                                                                | SHOW ALL VARIABLES                                         |
| SHOW VARIABLE variable_name                                         | Query proxy variable, name is split by underscore                                                                                                       | SHOW VARIABLE sql_show                                     |
| REFRESH DATABASE METADATA                                           | Refresh the metadata of logical database                                                                                                                | REFRESH DATABASE METADATA                                  |
| REFRESH TABLE METADATA                                              | Refresh the metadata of all tables                                                                                                                      | REFRESH TABLE METADATA                                     |
| REFRESH TABLE METADATA tableName                                    | Refresh the metadata of the specified table                                                                                                             | REFRESH TABLE METADATA t_order                             |
| REFRESH TABLE METADATA tableName FROM RESOURCE resourceName         | Refresh the tables' metadata in the specified data source                                                                                               | REFRESH TABLE METADATA t_order FROM RESOURCE ds_1          |
| REFRESH TABLE METADATA FROM RESOURCE resourceName SCHEMA schemaName | Refresh the tables' metadata in a schema of a specified data source. If there are no tables in the schema, the schema will be deleted.                  | REFRESH TABLE METADATA FROM RESOURCE ds_1 SCHEMA db_schema |
| SHOW TABLE METADATA tableName [, tableName] ...                     | Query table metadata                                                                                                                                    | SHOW TABLE METADATA t_order                                |
| EXPORT DATABASE CONFIG [FROM database_name] [, file="file_path"]    | Export resources and rule configurations to YAML format                                                                                                 | EXPORT DATABASE CONFIG FROM readwrite_splitting_db         |
| IMPORT DATABASE CONFIG FILE="file_path"                             | Import resources and rule configuration from YAML, only supports import into an empty database                                                          | IMPORT DATABASE CONFIG FILE = "/xxx/config-sharding.yaml"  |
| SHOW RULES USED RESOURCE resourceName [from database]               | Query the rules for using the specified resource in database                                                                                            | SHOW RULES USED RESOURCE ds_0 FROM databaseName            |

## Notice

ShardingSphere-Proxy does not support hint by default, to support it, set `proxy-hint-enabled` to true in `conf/server.yaml`.
