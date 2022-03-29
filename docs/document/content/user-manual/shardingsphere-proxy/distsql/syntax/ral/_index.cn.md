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
| set readwrite_splitting hint source = [auto / write] | 针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）              | set readwrite_splitting hint source = write    |
| set sharding hint database_value = yy                | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值 | set sharding hint database_value = 100         |
| add sharding hint database_value xx = yy             | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值     | add sharding hint database_value t_order= 100 |
| add sharding hint table_value xx = yy                | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值        | add sharding hint table_value t_order = 100   |
| clear hint                                           | 针对当前连接，清除 hint 所有设置                                    | clear hint                                    |
| clear [sharding hint / readwrite_splitting hint]     | 针对当前连接，清除 sharding 或 readwrite_splitting 的 hint 设置     | clear readwrite_splitting hint                |
| show [sharding / readwrite_splitting] hint status    | 针对当前连接，查询 sharding 或 readwrite_splitting 的 hint 设置     | show readwrite_splitting hint status          |

## 弹性伸缩

| 语句                                                 | 说明                                                           | 示例                                            |
|:--------------------------------------------------- |:------------------------------------------------------------- |:----------------------------------------------- |
| show scaling list                                   | 查询运行列表                                                    | show scaling list                               |
| show scaling status xx                              | 查询任务状态，xx：任务 id                                         | show scaling status 1234                        |
| start scaling xx                                    | 开始运行任务，xx：任务 id                                         | start scaling 1234                              |
| stop scaling xx                                     | 停止运行任务，xx：任务 id                                         | stop scaling 12345                              |
| drop scaling xx                                     | 移除任务，xx：任务 id                                            | drop scaling 1234                               |
| reset scaling xx                                    | 重置任务进度，xx：任务 id                                         | reset scaling 1234                              |
| check scaling xx                                    | 数据一致性校验，使用 `server.yaml` 里的校验算法，xx：任务 id         | check scaling 1234                              |
| show scaling check algorithms                       | 展示可用的一致性校验算法                                          | show scaling check algorithms                   |
| check scaling {jobId} by type(name={algorithmType}) | 数据一致性校验，使用指定的校验算法                                  | check scaling 1234 by type(name=DEFAULT)        |
| stop scaling source writing xx                      | 旧的 ShardingSphere 数据源停写，xx：任务 id                        | stop scaling source writing 1234                |
| restore scaling source writing xx                   | 旧的 ShardingSphere 数据源恢复写，xx：任务 id                      | restore scaling source writing 1234             |
| apply scaling xx                                    | 切换至新的 ShardingSphere 元数据，xx：任务 id                      | apply scaling 1234                              |

## 熔断

| 语句                                                           | 说明                                                | 示例                                            |
|:------------------------------------------------------------- |:-------------------------------------------------- |:----------------------------------------------  |
| [enable / disable] readwrite_splitting read xxx [from schema] | 启用 / 禁用读库                                      | enable readwrite_splitting read resource_0      |
| [enable / disable] instance [IP=xxx, PORT=xxx / instanceId]   | 启用 / 禁用 proxy 实例                               | disable instance 127.0.0.1@3307            |
| show instance list                                            | 查询 proxy 实例信息                                  | show instance list                              |
| show readwrite_splitting read resources [from schema]         | 查询所有读库的状态                                    | show readwrite_splitting read resources         |

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
| show instance mode                                                          | 查询当前 proxy 的 mode 配置                                    | show instance mode                                |
| count schema rules [from schema]                                            | 查询 schema 中的规则数量                                      | count schema rules                               |
| set variable proxy_property_name = xx                                       | proxy_property_name 为 proxy 的[属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/) ，需使用下划线命名 | set variable sql_show = true            |
| set variable transaction_type = xx                                          | 修改当前连接的事务类型, 支持 LOCAL，XA，BASE                     | set variable transaction_type = XA               |
| set variable agent_plugins_enabled = [true / false]                         | 设置 agent 插件的启用状态，默认值 false                         | set variable agent_plugins_enabled = true        |
| show all variables                                                          | 查询 proxy 所有的属性配置                                      | show all variables                               |
| show variable variable_name                                                 | 查询 proxy 属性，需使用下划线命名                            | show variable sql_show                           |
| preview SQL                                                                 | 预览实际 SQL                                                  | preview select * from t_order                    |
| parse SQL                                                                   | 解析 SQL 并输出抽象语法树                                        parse select * from t_order                      |
| refresh table metadata                                                      | 刷新所有表的元数据                                              | refresh table metadata                          |
| refresh table metadata [tableName / tableName from resource resourceName]   | 刷新指定表的元数据                                              | refresh table metadata t_order from resource ds_1 |
| show table metadata tableName [, tableName] ...                             | 查询表的元数据                                                 | show table metadata t_order                       |
| export schema config [from schema_name] [, file="file_path"]                | 查询 / 导出 schema 中的资源和规则配置                            | export schema config from readwrite_splitting_db  |
| show rules used resource resourceName [from schema]                         | 查询 schema 中使用指定资源的规则                                 | show rules used resource ds_0 from schemaName     |

## 注意事项

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `properties` 的属性 `proxy-hint-enabled` 设置为 true。
