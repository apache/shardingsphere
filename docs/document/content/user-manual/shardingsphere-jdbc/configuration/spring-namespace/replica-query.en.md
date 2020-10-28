+++
title = "Replica Query"
weight = 2
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/replica-query/replica-query-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/replica-query/replica-query-5.0.0.xsd)

\<replica-query:rule />

| *Name*               | *Type*    | *Description*                                |
| -------------------- | --------- | -------------------------------------------- |
| id                   | Attribute | Spring Bean Id                               |
| data-source-rule (+) | Tag       | Replica query data source rule configuration |

\<replica-query:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                              |
| -------------------------- | ---------- | -------------------------------------------------------------------------- |
| id                         | Attribute  | Primary-replica data source rule name                                      |
| primary-data-source-name   | Attribute  | Primary data source name                                                   |
| replica-data-source-names  | Attribute  | Replica data source names, multiple data source names separated with comma |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                                |

\<replica-query:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
