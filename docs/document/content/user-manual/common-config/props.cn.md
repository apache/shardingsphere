+++
title = "属性配置"
weight = 1
chapter = true
+++

## 背景信息

Apache ShardingSphere 提供属性配置的方式配置系统级配置。

## 参数解释

| *名称*                               | *数据类型*  | *说明*                                                                                                                                | *默认值*  |
|------------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------| -------- |
| sql-show (?)                       | boolean | 是否在日志中打印 SQL<br /> 打印 SQL 可以帮助开发者快速定位系统问题。日志内容包含：逻辑 SQL，真实 SQL 和 SQL 解析结果。<br /> 如果开启配置，日志将使用 Topic `ShardingSphere-SQL`，日志级别是 INFO | false    |
| sql-simple (?)                     | boolean | 是否在日志中打印简单风格的 SQL                                                                                                                   | false    |
| kernel-executor-size (?)           | int     | 用于设置任务处理线程池的大小<br />每个 ShardingSphereDataSource 使用一个独立的线程池，同一个 JVM 的不同数据源不共享线程池                                                     | infinite |
| max-connections-size-per-query (?) | int     | 一次查询请求在每个数据库实例中所能使用的最大连接数                                                                                                           | 1        |
| check-table-metadata-enabled (?)   | boolean | 在程序启动和更新时，是否检查分片元数据的结构一致性                                                                                                           | false    |
| sql-federation-type (?)            | String  | 联邦查询执行器类型，包括：NONE，ORIGINAL，ADVANCED                                                                                                 | NONE    |

## 操作步骤

属性配置直接配置在 ShardingSphere-JDBC 所使用的配置文件中，格式如下：

```yaml
props:
    sql-show: true
```

## 配置示例

ShardingSphere 仓库的示例中包含了多种不同场景的属性配置，请参考：<https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example>
