+++
title = "高可用"
weight = 3
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/database-discovery/database-discovery-5.0.0.xsd)

\<database-discovery:rule />

| *名称*                  | *类型* | *说明*               |
| ----------------------- | ------ | ------------------ |
| id                      | 属性   | Spring Bean Id      |
| data-source-rule (+)    | 标签   | 数据源规则配置        |
| discovery-heartbeat (+) | 标签   | 检测心跳规则配置       |

\<database-discovery:data-source-rule />

| *名称*                       | *类型* | *说明*                                      |
| --------------------------- | ----- | ------------------------------------------ |
| id                          | 属性  | 数据源规则名称                                |
| data-source-names           | 属性  | 数据源名称，多个数据源用逗号分隔 如：ds_0, ds_1  |
| discovery-heartbeat-name    | 属性  | 检测心跳名称                                 |
| discovery-type-name         | 属性  | 高可用类型名称                               |

\<database-discovery:discovery-heartbeat />

| *名称*                       | *类型* | *说明*                                      |
| --------------------------- | ----- | ------------------------------------------  |
| id                          | 属性  | 监听心跳名称                                  |
| props                       | 标签  | 监听心跳属性配置，keep-alive-cron 属性配置 cron 表达式，如：'0/5 * * * * ?'  |

\<database-discovery:discovery-type />

| *名称*     | *类型* | *说明*                                    |
| --------- | ----- | ----------------------------------------- |
| id        | 属性  | 高可用类型名称                               |
| type      | 属性  | 高可用类型，如： MGR、openGauss               |
| props (?) | 标签  | 高可用类型配置，如 MGR 的 group-name 属性配置   |
