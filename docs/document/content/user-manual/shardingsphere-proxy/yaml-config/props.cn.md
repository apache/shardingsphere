+++
title = "属性配置"
weight = 2
chapter = true
+++

## 背景信息

Apache ShardingSphere 提供属性配置的方式配置系统级配置。本节介绍 `server.yaml` 中的配置项。

## 参数解释

| *名称*                                | *数据类型*   | *说明*                                                                                                                                   | *默认值*    | *动态生效* |             
|-------------------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------|----------|--------| 
| sql-show (?)                        | boolean  | 是否在日志中打印 SQL。 <br /> 打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。<br /> 如果开启配置，日志将使用 Topic `ShardingSphere-SQL`，日志级别是 INFO。 | false    | 是      |
| sql-simple (?)                      | boolean  | 是否在日志中打印简单风格的 SQL。                                                                                                                     | false    | 是      |
| kernel-executor-size (?)            | int      | 用于设置任务处理线程池的大小。每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池。                                                            | infinite | 否      |
| max-connections-size-per-query (?)  | int      | 一次查询请求在每个数据库实例中所能使用的最大连接数。                                                                                                             | 1        | 是      |
| check-table-metadata-enabled (?)    | boolean  | 在程序启动和更新时，是否检查分片元数据的结构一致性。                                                                                                             | false    | 是      |
| proxy-frontend-flush-threshold (?)  | int      | 在 ShardingSphere-Proxy 中设置传输数据条数的 IO 刷新阈值。                                                                                             | 128      | 是      |
| proxy-hint-enabled (?)              | boolean  | 是否允许在 ShardingSphere-Proxy 中使用 Hint。使用 Hint 会将 Proxy 的线程处理模型由 IO 多路复用变更为每个请求一个独立的线程，会降低 Proxy 的吞吐量。                                    | false    | 是      |
| proxy-backend-query-fetch-size (?)  | int      | Proxy 后端与数据库交互的每次获取数据行数（使用游标的情况下）。数值增大可能会增加 ShardingSphere Proxy 的内存使用。默认值为 -1，代表设置为 JDBC 驱动的最小值。                                      | -1       | 是      |
| proxy-frontend-executor-size (?)    | int      | Proxy 前端 Netty 线程池线程数量，默认值 0 代表使用 Netty 默认值。                                                                                           | 0        | 否      |
| proxy-backend-executor-suitable (?) | String   | 可选选项：OLAP、OLTP。OLTP 选项可能会减少向客户端写入数据包的时间开销，但如果客户端连接数超过 `proxy-frontend-executor-size`，尤其是执行慢 SQL 时，它可能会增加 SQL 执行的延迟甚至阻塞其他客户端的连接。        | OLAP     | 是      |
| proxy-frontend-max-connections (?)  | int      | 允许连接 Proxy 的最大客户端数量，默认值 0 代表不限制。                                                                                                       | 0        | 是      |
| sql-federation-type (?)             | String   | 联邦查询执行器类型，包括：NONE，ORIGINAL，ADVANCED。                                                                                                   | NONE    | 是      |
| proxy-mysql-default-version (?)     | String   | Proxy 通过配置文件指定 MySQL 的版本号,默认版本：5.7.22。                                                                                                 | 5.7.22   | 否      |
| proxy-default-port (?)              | String   | Proxy 通过配置文件指定默认端口。                                                                                                                    | 3307     | 否      |
| proxy-netty-backlog (?)             | int      | Proxy 通过配置文件指定默认netty back_log参数。                                                                                                      | 1024     | 否      |
|proxy-frontend-database-protocol-type| String   | Proxy 前端协议类型，支持 MySQL, PostgreSQL, openGauss        |   ""    |    否    |

属性配置可以通过 [DistSQL#RAL](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/) 修改。
支持动态修改的属性可以立即生效，不支持动态修改的属性需要重启后生效。

## 配置示例

完整配置示例请参考 ShardingSphere 仓库内的 `server.yaml`：<https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf/server.yaml#L71-L93>
