+++
title = "RAL 语法"
weight = 3
chapter = true
+++

RAL (Resource & Rule Administration Language) 为 Apache ShardingSphere 的管理语言，负责强制路由、熔断、配置导入导出、数据迁移控制等管理功能。

## 强制路由

| 语句                                                  | 说明                                                           | 示例                                           |
| :--------------------------------------------------- | :------------------------------------------------------------ | :--------------------------------------------- |
| SET READWRITE_SPLITTING HINT SOURCE = [auto / write] | 针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）              | SET READWRITE_SPLITTING HINT SOURCE = write   |
| SET SHARDING HINT DATABASE_VALUE = yy                | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值 | SET SHARDING HINT DATABASE_VALUE = 100        |
| ADD SHARDING HINT DATABASE_VALUE xx = yy             | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值    | ADD SHARDING HINT DATABASE_VALUE t_order= 100 |
| ADD SHARDING HINT TABLE_VALUE xx = yy                | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值       | ADD SHARDING HINT TABLE_VALUE t_order = 100   |
| CLEAR HINT                                           | 针对当前连接，清除 hint 所有设置                                   | CLEAR HINT                                    |
| CLEAR [SHARDING HINT / READWRITE_SPLITTING HINT]     | 针对当前连接，清除 sharding 或 readwrite_splitting 的 hint 设置    | CLEAR READWRITE_SPLITTING HINT                |
| SHOW [SHARDING / READWRITE_SPLITTING] HINT STATUS    | 针对当前连接，查询 sharding 或 readwrite_splitting 的 hint 设置    | SHOW READWRITE_SPLITTING HINT STATUS          |

## 数据迁移

| 语句                                                     | 说明                                    | 示例                                             |
| :------------------------------------------------------ | -------------------------------------- | :----------------------------------------------- |
| MIGRATE TABLE ds.schema.table INTO table                | 从源端迁移到目标端                         | MIGRATE TABLE ds_0.public.t_order INTO t_order  |
| SHOW MIGRATION LIST                                     | 查询运行列表                              | SHOW MIGRATION LIST                             |
| SHOW MIGRATION STATUS jobId                             | 查询作业状态                              | SHOW MIGRATION STATUS 1234                      |
| STOP MIGRATION jobId                                    | 停止作业                                 | STOP MIGRATION 12345                            |
| START MIGRATION jobId                                   | 开启停止的作业                            | START MIGRATION 1234                            |
| CHECK MIGRATION jobId                                   | 数据一致性校验                            | CHECK MIGRATION 1234                            |
| SHOW MIGRATION CHECK ALGORITHMS                         | 展示可用的一致性校验算法                    | SHOW MIGRATION CHECK ALGORITHMS                 |
| CHECK MIGRATION jobId (by type(name=algorithmTypeName)? | 数据一致性校验，使用指定的校验算法            | CHECK MIGRATION 1234 by type(name="DATA_MATCH") |
| ROLLBACK MIGRATION jobId                                | 撤销作业。注意：该语句会清理目标端表，请谨慎操作 | ROLLBACK MIGRATION 1234                         |
| COMMIT MIGRATION jobId                                  | 完成作业                                  | COMMIT MIGRATION 1234                           |

## 熔断

| 语句                                                                                                  | 说明                         | 示例                                                          |
|:-----------------------------------------------------------------------------------------------------|:-----------------------------|:-------------------------------------------------------------|
| ALTER READWRITE_SPLITTING RULE [ groupName ] (ENABLE / DISABLE) storageUnitName [FROM databaseName]  | 启用 / 禁用读库                | ALTER READWRITE_SPLITTING RULE group_1 ENABLE read_ds_1      |
| [ENABLE / DISABLE] COMPUTE NODE instanceId                                                           | 启用 / 禁用 proxy 实例         | DISABLE COMPUTE NODE instance_1                              |
| SHOW COMPUTE NODES                                                                                   | 查询 proxy 实例信息            | SHOW COMPUTE NODES                                           |
| SHOW STATUS FROM READWRITE_SPLITTING (RULES / RULE groupName) [FROM databaseName]                    | 查询所有读库的状态              | SHOW STATUS FROM READWRITE_SPLITTING RULES                   |

## 全局规则

| 语句                                                                                                                                                                                                                 | 说明                                                                                                                                            | 示例                                                                                                                                                                                                                |
| :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :---------------------------------------------------------------------------------------------------------------------------------------------- |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| SHOW AUTHORITY RULE                                                                                                                                                                                                 | 查询权限规则配置                                                                                                                                  | SHOW AUTHORITY RULE                                                                                                                                                                                                 |
| SHOW TRANSACTION RULE                                                                                                                                                                                               | 查询事务规则配置                                                                                                                                  | SHOW TRANSACTION RULE                                                                                                                                                                                               |
| SHOW SQL_PARSER RULE                                                                                                                                                                                                | 查询解析引擎规则配置                                                                                                                               | SHOW SQL_PARSER RULE                                                                                                                                                                                                |
| ALTER TRANSACTION RULE(DEFAULT=xx,TYPE(NAME=xxx, PROPERTIES(key1=value1,key2=value2...)))                                                                                                                           | 更新事务规则配置，`DEFAULT`：默认事务类型，支持 LOCAL、XA、BASE；`NAME`：事务管理器名称，支持 Atomikos、Narayana 和 Bitronix                                | ALTER TRANSACTION RULE(DEFAULT="XA",TYPE(NAME="Narayana", PROPERTIES("databaseName"="jbossts","host"="127.0.0.1")))                                                                                                 |
| ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=xx, PARSE_TREE_CACHE(INITIAL_CAPACITY=xx, MAXIMUM_SIZE=xx, CONCURRENCY_LEVEL=xx), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=xxx, MAXIMUM_SIZE=xxx, CONCURRENCY_LEVEL=xxx) | 更新解析引擎规则配置，`SQL_COMMENT_PARSE_ENABLE`：是否解析 SQL 注释，`PARSE_TREE_CACHE`：语法树本地缓存配置，`SQL_STATEMENT_CACHE`：SQL 语句本地缓存配置项    | ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=false, PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100) |

## 其他

| 语句                                                                 | 说明                                                                                                            | 示例                                                        |
|:--------------------------------------------------------------------| :-------------------------------------------------------------------------------------------------------------- |:-----------------------------------------------------------|
| SHOW COMPUTE NODE INFO                                              | 查询当前 proxy 的实例信息                                                                                          | SHOW COMPUTE NODE INFO                                     |
| SHOW COMPUTE NODE MODE                                              | 查询当前 proxy 的 mode 配置                                                                                       | SHOW COMPUTE NODE MODE                                     |
| SET DIST VARIABLE proxy_property_name = xx                          | proxy_property_name 为 proxy 的[属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/)，需使用下划线命名 | SET DIST VARIABLE sql_show = true                          |
| SET DIST VARIABLE transaction_type = xx                             | 修改当前连接的事务类型, 支持 LOCAL，XA，BASE                                                                         | SET DIST VARIABLE transaction_type = "XA"                  |
| SET DIST VARIABLE agent_plugins_enabled = [TRUE / FALSE]            | 设置 agent 插件的启用状态，默认值 false                                                                             | SET DIST VARIABLE agent_plugins_enabled = TRUE             |
| SHOW DIST VARIABLES                                                 | 查询 proxy 所有的属性配置                                                                                          | SHOW DIST VARIABLES                                        |
| SHOW DIST VARIABLE WHERE name = variable_name                       | 查询 proxy 属性，需使用下划线命名                                                                                   | SHOW DIST VARIABLE WHERE name = sql_show                   |
| REFRESH TABLE METADATA                                              | 刷新所有表的元数据                                                                                                 | REFRESH TABLE METADATA                                     |
| REFRESH TABLE METADATA tableName                                    | 刷新指定表的元数据                                                                                                 | REFRESH TABLE METADATA t_order                             |
| REFRESH TABLE METADATA tableName FROM RESOURCE resourceName         | 刷新指定数据源中表的元数据                                                                                          | REFRESH TABLE METADATA t_order FROM RESOURCE ds_1          |
| REFRESH TABLE METADATA FROM RESOURCE resourceName SCHEMA schemaName | 刷新指定 schema 中表的元数据，如果 schema 中不存在表，则会删除该 schema                                                 | REFRESH TABLE METADATA FROM RESOURCE ds_1 SCHEMA db_schema |
| SHOW TABLE METADATA tableName [, tableName] ...                     | 查询表的元数据                                                                                                    | SHOW TABLE METADATA t_order                                |
| EXPORT DATABASE CONFIG [FROM database_name] [, file="file_path"]    | 将 database 中的资源和规则配置导出为 YAML 格式                                                                       | EXPORT DATABASE CONFIG FROM readwrite_splitting_db         |
| IMPORT DATABASE CONFIG FILE="file_path"                             | 将 YAML 中的配置导入到 database 中，仅支持对空库进行导入操作                                                           | IMPORT DATABASE CONFIG FILE = "/xxx/config-sharding.yaml"  |
| SHOW RULES USED RESOURCE resourceName [from database]               | 查询 database 中使用指定资源的规则                                                                                  | SHOW RULES USED RESOURCE ds_0 FROM databaseName            |

## 注意事项

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `props` 的属性 `proxy-hint-enabled` 设置为 true。
