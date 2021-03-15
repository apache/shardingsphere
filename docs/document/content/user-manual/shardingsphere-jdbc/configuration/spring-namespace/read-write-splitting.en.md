+++
title = "Read write splitting"
weight = 2
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/read-write-splitting/read-write-splitting-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/read-write-splitting/read-write-splitting-5.0.0.xsd)

\<read-write-splitting:rule />

| *Name*               | *Type*    | *Description*                                |
| -------------------- | --------- | -------------------------------------------- |
| id                   | Attribute | Spring Bean Id                               |
| data-source-rule (+) | Tag       | Read write splitting data source rule configuration |

\<read-write-splitting:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                              |
| -------------------------- | ---------- | -------------------------------------------------------------------------- |
| id                         | Attribute  | Read write splitting data source rule name                                      |
| write-data-source-name     | Attribute  | Write data source name                                                   |
| read-data-source-names     | Attribute  | Read data source names, multiple data source names separated with comma |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                                |

\<read-write-splitting:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
