+++
title = "读写分离"
weight = 2
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/read-write-splitting/read-write-splitting-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/read-write-splitting/read-write-splitting-5.0.0.xsd)

\<read-write-splitting:rule />

| *名称*                | *类型* | *说明*           |
| -------------------- | ------ | --------------- |
| id                   | 属性   | Spring Bean Id   |
| data-source-rule (+) | 标签   | 读写分离数据源规则配置 |

\<read-write-splitting:data-source-rule />

| *名称*                     | *类型* | *说明*                          |
| -------------------------- | ----- | ------------------------------- |
| id                         | 属性  | 读写分离数据源规则名称             |
| write-data-source-name     | 属性  | 写数据源名称                      |
| read-data-source-names     | 属性  | 读数据源名称，多个读数据源用逗号分隔 |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称                   |


\<read-write-splitting:load-balance-algorithm />

| *名称*    | *类型* | *说明*            |
| --------- | ----- | ----------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。
