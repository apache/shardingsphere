+++
title = "数据加密"
weight = 3
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd)

\<encrypt:rule />

| *名称*    | *类型* | *说明*         |
| --------- | ----- | ------------- |
| id        | 属性  | Spring Bean Id |
| table (+) | 标签  | 加密表配置      |

\<encrypt:table />

| *名称*     | *类型* | *说明*    |
| ---------- | ----- | -------- |
| name       | 属性  | 加密表名称 |
| column (+) | 标签  | 加密列配置 |

\<encrypt:column />

| *名称*                    | *类型* | *说明*       |
| ------------------------- | ----- | ------------ |
| logic-column              | 属性  | 加密列逻辑名称 |
| cipher-column             | 属性  | 加密列名称    |
| assisted-query-column (?) | 属性  | 查询辅助列名称 |
| plain-column (?)          | 属性  | 原文列名称     |
| encrypt-algorithm-ref     | 属性  | 加密算法名称   |

\<encrypt:encrypt-algorithm />

| *名称*    | *类型* | *说明*         |
| --------- | ----- | ------------- |
| id        | 属性  | 加密算法名称    |
| type      | 属性  | 加密算法类型    |
| props (?) | 标签  | 加密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。
