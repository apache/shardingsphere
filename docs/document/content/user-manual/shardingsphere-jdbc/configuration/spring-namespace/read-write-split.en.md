+++
title = "Read-write Split"
weight = 2
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave-5.0.0.xsd)

\<master-slave:rule />

| *Name*               | *Type*    | *Description*                               |
| -------------------- | --------- | ------------------------------------------- |
| id                   | Attribute | Spring Bean Id                              |
| data-source-rule (+) | Tag       | Master-slave data source rule configuration |

\<master-slave:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                            |
| -------------------------- | ---------- | ------------------------------------------------------------------------ |
| id                         | Attribute  | Master-slave data source rule name                                       |
| master-data-source-name    | Attribute  | Master data source name                                                  |
| slave-data-source-names    | Attribute  | Slave data source names, multiple data source names separated with comma |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                              |

\<master-slave:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
