+++
title = "属性配置"
weight = 3
chapter = true
+++

## 简介

Apache ShardingSphere 提供属性配置的方式配置系统级配置。

## 配置项说明

| *名称*                             | *数据类型*   | *说明*                                                                                                                                                                                  | *默认值*  |
| ---------------------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| sql-show (?)                       | boolean     | 是否在日志中打印 SQL。 <br /> 打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。<br /> 如果开启配置，日志将使用 Topic `ShardingSphere-SQL`，日志级别是 INFO。 | false    |
| sql-simple (?)                     | boolean     | 是否在日志中打印简单风格的 SQL。                                                                                                                                                           | false    |
| acceptor-size (?)                  | int         | 用于设置接收 TCP 请求线程池的大小。                                                                                                                                                        | CPU * 2  |
| executor-size (?)                  | int         | 用于设置任务处理线程池的大小。每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池。                                                                           | infinite |
| max-connections-size-per-query (?) | int         | 一次查询请求在每个数据库实例中所能使用的最大连接数。                                                                                                                                          | 1        |
| check-table-metadata-enabled (?)   | boolean     | 是否在程序启动和更新时检查分片元数据的结构一致性。                                                                                                                                            | false    |
| query-with-cipher-column (?)       | boolean     | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询。                                                                                                                            | true     |
| proxy-frontend-flush-threshold (?) | int         | 在 ShardingSphere-Proxy 中设置传输数据条数的 IO 刷新阈值。                                                                                                                                  | 128      |
| proxy-transaction-type (?)         | String      | ShardingSphere-Proxy 中使用的默认事务类型。包括：LOCAL、XA 和 BASE。                                                                                                                         | LOCAL    |
| proxy-opentracing-enabled (?)      | boolean     | 是否允许在 ShardingSphere-Proxy 中使用 OpenTracing。                                                                                                                                       | false    |
| proxy-hint-enabled (?)             | boolean     | 是否允许在 ShardingSphere-Proxy 中使用 Hint。使用 Hint 会将 Proxy 的线程处理模型由 IO 多路复用变更为每个请求一个独立的线程，会降低 Proxy 的吞吐量。                                                  | false    |
| transaction-manager-type (?)       | String      | 事务管理器类型。列如：Atomikos，Narayana。                                                                                                                                                  | atomikos   |
