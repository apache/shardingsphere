+++
title = "数据分片"
weight = 1
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd)

\<sharding:rule />

| *名称*                                | *类型* | *说明*              |
| ------------------------------------- | ------ | ------------------ |
| id                                    | 属性   | Spring Bean Id     |
| table-rules (?)                       | 标签   | 分片表规则配置       |
| auto-table-rules (?)                  | 标签   | 自动化分片表规则配置  |
| binding-table-rules (?)               | 标签   | 绑定表规则配置        |
| broadcast-table-rules (?)             | 标签   | 广播表规则配置        |
| default-database-strategy-ref (?)     | 属性   | 默认分库策略名称      |
| default-table-strategy-ref (?)        | 属性   | 默认分表策略名称      |
| default-key-generate-strategy-ref (?) | 属性   | 默认分布式序列策略名称 |

\<sharding:table-rule />

| *名称*                     | *类型* | *说明*          |
| ------------------------- | ----- | --------------- |
| logic-table               | 属性  | 逻辑表名称        |
| actual-data-nodes         | 属性  | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| database-strategy-ref     | 属性  | 分库策略名称      |
| table-strategy-ref        | 属性  | 分表策略名称      |
| key-generate-strategy-ref | 属性  | 分布式序列策略名称 |

\<sharding:binding-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| binding-table-rule (+) | 标签   | 绑定表规则配置 |

\<sharding:binding-table-rule />

| *名称*       | *类型*  | *说明*                   |
| ------------ | ------ | ------------------------ |
| logic-tables | 属性   | 绑定表名称，多个表以逗号分隔 |

\<sharding:broadcast-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| broadcast-table-rule (+) | 标签   | 广播表规则配置 |

\<sharding:broadcast-table-rule />

| *名称* | *类型* | *说明*   |
| ------ | ----- | -------- |
| table  | 属性  | 广播表名称 |

\<sharding:standard-strategy />

| *名称*          | *类型* | *说明*          |
| --------------- | ----- | -------------- |
| id              | 属性   | 标准分片策略名称 |
| sharding-column | 属性   | 分片列名称      |
| algorithm-ref   | 属性   | 分片算法名称    |

\<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                    |
| ---------------- | ----- | ------------------------- |
| id               | 属性   | 复合分片策略名称            |
| sharding-columns | 属性   | 分片列名称，多个列以逗号分隔 |
| algorithm-ref    | 属性   | 分片算法名称               |

\<sharding:hint-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | Hint 分片策略名称 |
| algorithm-ref | 属性   | 分片算法名称      |

\<sharding:none-strategy />

| *名称* | *类型* | *说明*      |
| ------ | ----- | ----------- |
| id     | 属性   | 分片策略名称 |

\<sharding:key-generate-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | 分布式序列策略名称 |
| column        | 属性   | 分布式序列列名称   |
| algorithm-ref | 属性   | 分布式序列算法名称 |

\<sharding:sharding-algorithm />

| *名称*    | *类型* | *说明*        |
| --------- | ----- | ------------- |
| id        | 属性  | 分片算法名称    |
| type      | 属性  | 分片算法类型    |
| props (?) | 标签  | 分片算法属性配置 |

\<sharding:key-generate-algorithm />

| *名称*    | *类型* | *说明*              |
| --------- | ----- | ------------------ |
| id        | 属性  | 分布式序列算法名称    |
| type      | 属性  | 分布式序列算法类型    |
| props (?) | 标签  | 分布式序列算法属性配置 |

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding)和[内置分布式序列算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen)。

## 注意事项

行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。
