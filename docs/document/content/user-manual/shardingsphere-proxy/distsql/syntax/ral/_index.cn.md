+++
title = "RAL 语法"
weight = 3
chapter = true
+++

RAL (Resource & Rule Administration Language) 为 Apache ShardingSphere 的管理语言，
负责强制路由、事务类型切换、弹性伸缩、分片执行计划查询等增量功能的操作。

## 强制路由

| 语句                                                  | 说明                                                            | 示例                                           |
|:---------------------------------------------------- |:-------------------------------------------------------------- |:---------------------------------------------- |
| SET READWRITE_SPLITTING HINT SOURCE = [auto / write] | 针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）              | SET READWRITE_SPLITTING HINT SOURCE = write    |
| SET SHARDING HINT DATABASE_VALUE = yy                | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值 | SET SHARDING HINT DATABASE_VALUE = 100         |
| ADD SHARDING HINT DATABASE_VALUE tableName = yy             | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值     | ADD SHARDING HINT DATABASE_VALUE t_order= 100 |
| ADD SHARDING HINT TABLE_VALUE tableName xx = yy                | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值        | ADD SHARDING HINT TABLE_VALUE t_order = 100   |
| CLEAR HINT SETTINGS                                          | 针对当前连接，清除 hint 所有设置                                    | CLEAR HINT                                   |
| CLEAR [SHARDING HINT / READWRITE_SPLITTING HINT]     | 针对当前连接，清除 sharding 或 readwrite_splitting 的 hint 设置     | CLEAR READWRITE_SPLITTING HINT                |
| SHOW [SHARDING / READWRITE_SPLITTING] HINT STATUS    | 针对当前连接，查询 sharding 或 readwrite_splitting 的 hint 设置     | SHOW READWRITE_SPLITTING HINT STATUS          |

## 弹性伸缩

| 语句                                                 | 说明                                                           | 示例                                            |
|:--------------------------------------------------- |:------------------------------------------------------------- |:----------------------------------------------- |
| SHOW SCALING LIST                                   | 查询运行列表                                                    | SHOW SCALING LIST                               |
| SHOW SCALING STATUS jobId                            | 查询任务状态，xx：任务 id                                         | SHOW SCALING LIST 1234                        |
| START SCALING jobId                                    | 开始运行任务，xx：任务 id                                         | START SCALING 1234                              |
| STOP SCALING jobId                                     | 停止运行任务，xx：任务 id                                         | STOP SCALING 12345                              |
| DROP SCALING jobId                                     | 移除任务，xx：任务 id                                            | DROP SCALING 1234                               |
| RESET SCALING jobId                                    | 重置任务进度，xx：任务 id                                         | RESET SCALING 1234                              |
| CHECK SCALING jobId                                    | 数据一致性校验，使用 `server.yaml` 里的校验算法，xx：任务 id         | CHECK SCALING 1234                              |
| SHOW SCALING CHECK ALGORITHMS                       | 展示可用的一致性校验算法                                          | SHOW SCALING CHECK ALGORITHMS                   |
| CHECK SCALING {jobId} by type(name={algorithmType}) | 数据一致性校验，使用指定的校验算法                                  | CHECK SCALING 1234 by type(name=DEFAULT)        |
| STOP SCALING SOURCE WRITING jobId                      | 旧的 ShardingSphere 数据源停写，xx：任务 id                        | STOP SCALING SOURCE WRITING 1234                |
| RESTORE SCALING SOURCE WRITING jobId                   | 旧的 ShardingSphere 数据源恢复写，xx：任务 id                      | RESTORE SCALING SOURCE WRITING 1234             |
| APPLY SCALING jobId                                    | 切换至新的 ShardingSphere 元数据，xx：任务 id                      | APPLY SCALING 1234                              |

## 熔断

| 语句                                                           | 说明                                                | 示例                                            |
|:------------------------------------------------------------- |:-------------------------------------------------- |:----------------------------------------------  |
| [ENABLE / DISABLE] READWRITE_SPLITTING (READ)? resourceName [FROM schemaName] | 启用 / 禁用读库                                      | ENABLE READWRITE_SPLITTING READ resource_0      |
| [ENABLE / DISABLE] INSTANCE [IP=xxx, PORT=xxx / instanceId]   | 启用 / 禁用 proxy 实例                               | DISABLE INSTANCE 127.0.0.1@3307            |
| SHOW INSTANCE LIST                                            | 查询 proxy 实例信息                                  | SHOW INSTANCE LIST                              |
| SHOW READWRITE_SPLITTING (READ)? resourceName [FROM schemaName]         | 查询所有读库的状态                                    | SHOW READWRITE_SPLITTING READ RESOURCES         |

## 全局规则

| 语句                                                                                                                                                                                                                 | 说明                                                                                                                                        | 示例                                                                                                                                                                                                                  |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SHOW AUTHORITY RULE                                                                                                                                                                                                 | 查询权限规则配置                                                                                                                                | SHOW AUTHORITY RULE                                                                                                                                                                                                 |
| SHOW TRANSACTION RULE                                                                                                                                                                                               | 查询事务规则配置                                                                                                                                | SHOW TRANSACTION RULE                                                                                                                                                                                               |
| SHOW SQL_PARSER RULE                                                                                                                                                                                                | 查询解析引擎规则配置                                                                                                                             | SHOW SQL_PARSER RULE                                                                                                                                                                                                |
| ALTER TRANSACTION RULE(DEFAULT=xx,TYPE(NAME=xxx, PROPERTIES("key1"="value1","key2"="value2"...)))                                                                                                                   | 更新事务规则配置，`DEFAULT`：默认事务类型，支持 LOCAL、XA、BASE；`NAME`：事务管理器名称，支持 Atomikos、Narayana 和 Bitronix                             | ALTER TRANSACTION RULE(DEFAULT=XA,TYPE(NAME=Narayana, PROPERTIES("databaseName"="jbossts","host"="127.0.0.1")))                                                                                                     |
| ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=xx, PARSE_TREE_CACHE(INITIAL_CAPACITY=xx, MAXIMUM_SIZE=xx, CONCURRENCY_LEVEL=xx), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=xxx, MAXIMUM_SIZE=xxx, CONCURRENCY_LEVEL=xxx) | 更新解析引擎规则配置，`SQL_COMMENT_PARSE_ENABLE`：是否解析 SQL 注释，`PARSE_TREE_CACHE`：语法树本地缓存配置，`SQL_STATEMENT_CACHE`：SQL 语句本地缓存配置项 | ALTER SQL_PARSER RULE SQL_COMMENT_PARSE_ENABLE=false, PARSE_TREE_CACHE(INITIAL_CAPACITY=10, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=1), SQL_STATEMENT_CACHE(INITIAL_CAPACITY=11, MAXIMUM_SIZE=11, CONCURRENCY_LEVEL=100) |

## 其他

| 语句                                                                         | 说明                                                         | 示例                                            |
|:--------------------------------------------------------------------------- |:----------------------------------------------------------- |:----------------------------------------------- |
| SHOW INSTANCE MODE                                                          | 查询当前 proxy 的 mode 配置                                    | SHOW INSTANCE MODE                                |
| COUNT SCHEMA RULES [FROM schema]                                            | 查询 schema 中的规则数量                                      | COUNT SCHEMA RULES                               |
| SET VARIABLE proxy_property_name = xx                                       | proxy_property_name 为 proxy 的[属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/) ，需使用下划线命名 | SET VARIABLE sql_show = true            |
| SET VARIABLE transaction_type = xx                                          | 修改当前连接的事务类型, 支持 LOCAL，XA，BASE                     | SET VARIABLE transaction_type = XA               |
| SET VARIABLE agent_plugins_enabled = [TRUE / FALSE]                         | 设置 agent 插件的启用状态，默认值 false                         | SET VARIABLE agent_plugins_enabled = TRUE        |
| SHOW ALL VARIABLES                                                          | 查询 proxy 所有的属性配置                                      | SHOW ALL VARIABLES                               |
| SHOW VARIABLE variable_name                                                 | 查询 proxy 属性，需使用下划线命名                            | SHOW VARIABLE sql_show                           |
| PREVIEW SQL                                                                 | 预览实际 SQL                                                  | PREVIEW SELECT * FROM t_order                    |
| PARSE SQL                                                                   | 解析 SQL 并输出抽象语法树                                        PARSE SELECT * FROM t_order                      |
| REFRESH TABLE METADATA                                                      | 刷新所有表的元数据                                              | REFRESH TABLE METADATA                          |
| REFRESH TABLE METADATA [tableName / tableName FROM resource resourceName]   | 刷新指定表的元数据                                              | REFRESH TABLE METADATA t_order FROM resource ds_1 |
| SHOW TABLE METADATA tableName [, tableName] ...                             | 查询表的元数据                                                 | SHOW TABLE METADATA t_order                       |
| EXPORT SCHEMA CONFIG [FROM schema_name] [, file="file_path"]                | 查询 / 导出 schema 中的资源和规则配置                            | EXPORT SCHEMA CONFIG FROM readwrite_splitting_db  |
| SHOW RULES USED RESOURCE resourceName [from schema]                         | 查询 schema 中使用指定资源的规则                                 | SHOW RULES USED RESOURCE ds_0 FROM schemaName     |

## 注意事项

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `properties` 的属性 `proxy-hint-enabled` 设置为 true。
