+++
title = "属性配置"
weight = 2
chapter = true
+++

## 背景信息

Apache ShardingSphere 提供了丰富的系统配置属性，用户可通过 `global.yaml` 进行配置。

## 参数解释

| *名称*                                                | *数据类型*  | *说明*                                                                                                                         | *默认值*           | *动态生效* |
|-----------------------------------------------------|---------|------------------------------------------------------------------------------------------------------------------------------|-----------------|--------|
| sql-show (?)                                        | boolean | 是否在日志中打印 SQL。SQL 日志内容包含：逻辑 SQL、真实 SQL 和 SQL 解析结果。如果开启配置，日志将使用 Topic `org.apache.shardingsphere.sql`，日志级别为 INFO。 | false           | 是      |
| sql-simple (?)                                      | boolean | 是否在日志中打印简单风格的 SQL。                                                                                                          | false           | 是      |
| kernel-executor-size (?)                            | int     | SQL 执行工作线程池大小。`0` 表示不限制。                                                                                                  | 0               | 否      |
| max-connections-size-per-query (?)                  | int     | 一次查询请求在每个数据库实例中所能使用的最大连接数。                                                                                                  | 1               | 是      |
| max-union-size-per-datasource (?)                   | int     | 每个数据源允许合并的最大 UNION ALL 数量。当路由到同一数据源的路由单元数量超过此值时，将分批合并以恢复并行执行能力。                                      | Integer.MAX_VALUE | 是    |
| check-table-metadata-enabled (?)                    | boolean | 应用启动或元数据更新时，是否校验表元数据一致性。                                                                                                    | false           | 是      |
| load-table-metadata-batch-size (?)                  | int     | 应用启动或刷新表元数据时，单个批次加载表元数据的数量。                                                                                                 | 1000            | 是      |
| proxy-frontend-database-protocol-type (?)           | String  | ShardingSphere-Proxy 前端数据库协议类型。支持 `MySQL`、`PostgreSQL`、`openGauss` 和 `Firebird`。                                      | ""              | 是      |
| proxy-frontend-flush-threshold (?)                  | int     | ShardingSphere-Proxy 前端传输记录的 I/O 刷新阈值。                                                                                       | 128             | 是      |
| proxy-backend-query-fetch-size (?)                  | int     | Proxy 后端与数据库使用游标交互时每次获取的数据行数。数值增大可能会增加 ShardingSphere-Proxy 的内存使用。`-1` 表示使用不同 JDBC 驱动的最小值。                  | -1              | 是      |
| proxy-frontend-executor-size (?)                    | int     | ShardingSphere-Proxy 前端 Netty 线程池线程数量。`0` 表示使用 Netty 默认值。                                                                 | 0               | 否      |
| proxy-frontend-max-connections (?)                  | int     | 允许连接 ShardingSphere-Proxy 的最大客户端数量。`0` 表示不限制。                                                                            | 0               | 是      |
| proxy-frontend-connection-idle-timeout (?)          | long    | 前端连接空闲超时时间，单位为秒。                                                                                                          | 28800           | 是      |
| proxy-default-port (?)                              | int     | ShardingSphere-Proxy 默认启动端口。                                                                                                  | 3307            | 否      |
| proxy-netty-backlog (?)                             | int     | ShardingSphere-Proxy 的 Netty backlog 大小。                                                                                       | 1024            | 是      |
| cdc-server-port (?)                                 | int     | CDC Server 端口。                                                                                                                | 33071           | 否      |
| proxy-frontend-ssl-enabled (?)                      | boolean | ShardingSphere-Proxy 前端是否启用 SSL/TLS。                                                                                         | false           | 否      |
| proxy-frontend-ssl-version (?)                      | String  | 要启用的 SSL/TLS 协议。空白表示使用默认协议。                                                                                               | TLSv1.2,TLSv1.3 | 否      |
| proxy-frontend-ssl-cipher (?)                       | String  | 按偏好顺序启用的密码套件。多个密码套件用逗号分隔。空白表示使用默认密码套件。                                                                                | ""              | 否      |
| agent-plugins-enabled (?)                           | boolean | 是否启用 agent 插件。                                                                                                             | true            | 是      |
| metadata-identifier-case-sensitivity (?)            | String  | 元数据标识符大小写敏感策略。可选值为 `AUTO`、`SENSITIVE` 和 `INSENSITIVE`。                                                                      | AUTO            | 否      |
| groovy-inline-expression-parsing-cache-max-size (?) | long    | Groovy 行表达式解析缓存的最大容量。                                                                                                      | 1000            | 是      |

属性配置可以通过 [DistSQL#RAL](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/) 在线修改。
其中支持动态修改的属性立即生效，不支持动态修改的属性在重启后生效。

## 配置示例

完整配置示例请参考 ShardingSphere 仓库内的 `global.yaml`：<https://github.com/apache/shardingsphere/blob/master/proxy/bootstrap/src/main/resources/conf/global.yaml>
