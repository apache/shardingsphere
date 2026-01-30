+++
title = "Metrics"
weight = 3
+++

| 指标名称                                 | 指标类型    | 指标描述                                                                                       |
|:----------------------------------------|:----------|:----------------------------------------------------------------------------------------------|
| build_info                              | GAUGE     | 构建信息                                                                                       |
| parsed_sql_total                        | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT、DDL、DCL、DAL、TCL、RQL、RDL、RAL、RUL）分类的解析总数        |
| routed_sql_total                        | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT）分类的路由总数                                             |
| routed_result_total                     | COUNTER   | 路由结果总数(数据源路由结果、表路由结果)                                                            |
| jdbc_state                              | GAUGE     | ShardingSphere-JDBC 状态信息。0 表示正常状态；1 表示熔断状态；2 锁定状态                              |
| jdbc_meta_data_info                     | GAUGE     | ShardingSphere-JDBC 元数据信息                                                                  |
| jdbc_statement_execute_total            | COUNTER   | 语句执行总数                                                                                    |
| jdbc_statement_execute_errors_total     | COUNTER   | 语句执行错误总数                                                                                 |
| jdbc_statement_execute_latency_millis   | HISTOGRAM | 语句执行耗时                                                                                    |
| jdbc_transactions_total                 | COUNTER   | 事务总数，按 commit，rollback 分类                                                                |
