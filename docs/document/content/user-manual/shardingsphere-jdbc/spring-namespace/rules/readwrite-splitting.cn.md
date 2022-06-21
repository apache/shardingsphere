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

| *名称*                     | *类型* | *说明*                                        |
| -------------------------- | ----- | -------------------------------------------- |
| id                         | 属性  | 读写分离数据源规则名称                           |
| type                       | 属性  | 读写分离类型，分为静态和动态。如 Static、Dynamic  |
| props                      | 标签  | 读写分离所需属性，如静态：write-data-source-name、read-data-source-names，动态：auto-aware-data-source-name、write-data-source-query-enabled  |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称                               |


\<readwrite-splitting:load-balance-algorithm />

| *名称*     | *类型* | *说明*           |
| --------- | ----- | ---------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。
查询一致性路由的详情，请参见[使用规范](/cn/features/readwrite-splitting/use-norms)。
