+++
title = "Readwrite-splitting"
weight = 2
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.1.2.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.1.2.xsd)

\<readwrite-splitting:rule />

| *Name*               | *Type*    | *Description*                                |
| -------------------- | --------- | -------------------------------------------- |
| id                   | Attribute | Spring Bean Id                               |
| data-source-rule (+) | Tag       | Readwrite-splitting data source rule configuration |

\<readwrite-splitting:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                           |
| -------------------------- | ---------- | ----------------------------------------------------------------------- |
| id                         | Attribute  | Readwrite-splitting data source rule name                               |
| static-strategy            | Tag        | Static Readwrite-splitting type                                         |
| dynamic-strategy           | Tag        | Dynamic Readwrite-splitting type                                        |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                             |


\<readwrite-splitting:static-strategy />

| *Name*                     | *Type* | *Description*                                                          |
| -------------------------- | ----- | ----------------------------------------------------------------------- |
| id                         | Attribute  | Static readwrite-splitting name                                          |
| write-data-source-name     | Attribute  | Write data source name                                                   |
| read-data-source-names     | Attribute  | Read data source names, multiple data source names separated with comma  |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                              |

\<readwrite-splitting:dynamic-strategy />

| *Name*                           | *Type*     | *Description*                                                                                               |
| -------------------------------- | ---------- | ----------------------------------------------------------------------------------------------------------- |
| id                               | Attribute  | Dynamic readwrite-splitting name                                                                            |
| auto-aware-data-source-name      | Attribute  | Database discovery logic data source name                                                                   |
| write-data-source-query-enabled  | Attribute  | All read data source are offline, write data source whether the data source is responsible for read traffic |
| load-balance-algorithm-ref       | Attribute  | Load balance algorithm name                                                                                 |

\<readwrite-splitting:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Use Norms](/en/features/readwrite-splitting/use-norms) for more details about query consistent routing.
