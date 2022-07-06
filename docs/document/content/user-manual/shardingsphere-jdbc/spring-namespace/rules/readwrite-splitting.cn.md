+++
title = "读写分离"
weight = 2
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.1.2.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.1.2.xsd)

\<readwrite-splitting:rule />

| *名称*                | *类型* | *说明*           |
| -------------------- | ------ | --------------- |
| id                   | 属性   | Spring Bean Id   |
| data-source-rule (+) | 标签   | 读写分离数据源规则配置 |

\<readwrite-splitting:data-source-rule />

| *名称*                     | *类型* | *说明*                 |
| -------------------------- | ----- | --------------------- |
| id                         | 属性  | 读写分离数据源规则名称    |
| static-strategy            | 标签  | 静态读写分离类型         |
| dynamic-strategy           | 标签  | 动态读写分离类型         |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称         |

\<readwrite-splitting:static-strategy />

| *名称*                     | *类型* | *说明*                             |
| -------------------------- | ----- | --------------------------------- |
| id                         | 属性  | 静态读写分离名称                     |
| write-data-source-name     | 属性  | 写库数据源名称                       |
| read-data-source-names     | 属性  | 读库数据源列表，多个从数据源用逗号分隔  |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称                     |

\<readwrite-splitting:dynamic-strategy />

| *名称*                            | *类型* | *说明*                            |
| -------------------------------- | ----- | --------------------------------- |
| id                               | 属性  | 动态读写分离名称                     |
| auto-aware-data-source-name      | 属性  | 数据库发现逻辑数据源名称              |
| write-data-source-query-enabled  | 属性  | 读库全部下线，主库是否承担读流量       |
| load-balance-algorithm-ref       | 属性  | 负载均衡算法名称                     |


\<readwrite-splitting:load-balance-algorithm />

| *名称*     | *类型* | *说明*           |
| --------- | ----- | ---------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。
查询一致性路由的详情，请参见[使用规范](/cn/features/readwrite-splitting/use-norms)。
