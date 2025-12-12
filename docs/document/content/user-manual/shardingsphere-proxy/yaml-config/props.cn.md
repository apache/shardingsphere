+++
title = "属性配置"
weight = 2
chapter = true
+++

## 背景信息

Apache ShardingSphere 提供了丰富的系统配置属性，用户可通过 `global.yaml` 进行配置。

## 参数解释

| *名称*                                      | *数据类型*  | *说明*                                                                                                                                   | *默认值*           | *动态生效* |
|-------------------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------|-----------------|--------|
| sql-show (?)                              | boolean | 是否在日志中打印 SQL。 <br /> 打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。<br /> 如果开启配置，日志将使用 Topic `org.apache.shardingsphere.sql`，日志级别是 INFO。 | false           | 是      |
| sql-simple (?)                            | boolean | 是否在日志中打印简单风格的 SQL。                                                                                                                     | false           | 是      |
| kernel-executor-size (?)                  | int     | 用于设置任务处理线程池的大小。每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池。                                                            | infinite        | 否      |
| max-connections-size-per-query (?)        | int     | 一次查询请求在每个数据库实例中所能使用的最大连接数。                                                                                                             | 1               | 是      |
| check-table-metadata-enabled (?)          | boolean | 在程序启动和更新时，是否检查分片元数据的结构一致性。                                                                                                             | false           | 是      |
| load-table-metadata-batch-size (?)        | int     | 在程序启动或刷新元数据时，单个批次加载表元数据的数量。                                                                                                            | 1000            | 是      |
| proxy-frontend-flush-threshold (?)        | int     | 在 ShardingSphere-Proxy 中设置传输数据条数的 IO 刷新阈值。                                                                                             | 128             | 是      |
| proxy-backend-query-fetch-size (?)        | int     | Proxy 后端与数据库交互的每次获取数据行数（使用游标的情况下）。数值增大可能会增加 ShardingSphere Proxy 的内存使用。默认值为 -1，代表设置为 JDBC 驱动的最小值。                                      | -1              | 是      |
| proxy-frontend-executor-size (?)          | int     | Proxy 前端 Netty 线程池线程数量，默认值 0 代表使用 Netty 默认值。                                                                                           | 0               | 否      |
| proxy-frontend-max-connections (?)        | int     | 允许连接 Proxy 的最大客户端数量，默认值 0 代表不限制。                                                                                                       | 0               | 是      |
| proxy-default-port (?)                    | String  | Proxy 通过配置文件指定默认端口。                                                                                                                    | 3307            | 否      |
| proxy-netty-backlog (?)                   | int     | Proxy 通过配置文件指定默认netty back_log参数。                                                                                                      | 1024            | 否      |
| proxy-frontend-database-protocol-type (?) | String  | Proxy 前端协议类型，支持 MySQL，PostgreSQL 和 openGauss                                                                                           | \"\"            | 否      |
| proxy-frontend-ssl-enabled (?)            | boolean | Proxy 前端启用 SSL/TLS。                                                                                                                    | false           | 否      |
| proxy-frontend-ssl-version (?)            | String  | 要启用的 SSL/TLS 协议。空白以使用默认值。                                                                                                              | TLSv1.2,TLSv1.3 | 否  |
| proxy-frontend-ssl-cipher (?)             | String  | 按偏好顺序启用的密码套件。用逗号分隔的多密码套件。空白以使用默认值。                                                                                                     | \"\"            | 否  |

属性配置可以通过 [DistSQL#RAL](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/) 在线修改。
其中支持动态修改的属性立即生效，不支持动态修改的属性在重启后生效。

## 配置示例

完整配置示例请参考 ShardingSphere 仓库内的 `global.yaml`：<https://github.com/apache/shardingsphere/blob/612cd5d8e802d0d712a3a4d89da8fdc048d23879/proxy/bootstrap/src/main/resources/conf/global.yaml#L71-L89>
