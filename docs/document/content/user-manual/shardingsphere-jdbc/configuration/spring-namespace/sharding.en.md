+++
title = "Sharding"
weight = 1
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd)

\<sharding:rule />

| *Name*                                | *Type*    | *Description*                               |
| ------------------------------------- | --------- | ------------------------------------------- |
| id                                    | Attribute | Spring Bean Id                              |
| table-rules (?)                       | Tag       | Sharding table rule configuration           |
| auto-table-rules (?)                  | Tag       | Automatic sharding table rule configuration |
| binding-table-rules (?)               | Tag       | Binding table rule configuration            |
| broadcast-table-rules (?)             | Tag       | Broadcast table rule configuration          |
| default-database-strategy-ref (?)     | Attribute | Default database strategy name              |
| default-table-strategy-ref (?)        | Attribute | Default table strategy name                 |
| default-key-generate-strategy-ref (?) | Attribute | Default key generate strategy name          |

\<sharding:table-rule />

| *Name*                    | *Type*    | *Description*              |
| ------------------------- | --------- | -------------------------- |
| logic-table               | Attribute | Logic table name           |
| actual-data-nodes         | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. |
| actual-data-sources       | Attribute | Data source names for auto sharding table |
| database-strategy-ref     | Attribute | Database strategy name for standard sharding table     |
| table-strategy-ref        | Attribute | Table strategy name for standard sharding table        |
| sharding-strategy-ref     | Attribute | sharding strategy name for auto sharding table         |
| key-generate-strategy-ref | Attribute | Key generate strategy name |

\<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

\<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Binding table name, multiple tables separated with comma |

\<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*                      |
| ------------------------ | ------ | ---------------------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rule configuration |

\<sharding:broadcast-table-rule />

| *Name* | *Type*    | *Description*        |
| ------ | --------- | -------------------- |
| table  | Attribute | Broadcast table name |

\<sharding:standard-strategy />

| *Name*          | *Type*    | *Description*                   |
| --------------- | --------- | ------------------------------- |
| id              | Attribute | Standard sharding strategy name |
| sharding-column | Attribute | Sharding column name            |
| algorithm-ref   | Attribute | Sharding algorithm name         |

\<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Complex sharding strategy name                               |
| sharding-columns | Attribute | Sharding column names, multiple columns separated with comma |
| algorithm-ref    | Attribute | Sharding algorithm name                                      |

\<sharding:hint-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Hint sharding strategy name |
| algorithm-ref | Attribute | Sharding algorithm name     |

\<sharding:none-strategy />

| *Name* | *Type*    | *Description*          |
| ------ | --------- | ---------------------- |
| id     | Attribute | Sharding strategy name |

\<sharding:key-generate-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Key generate strategy name  |
| column        | Attribute | Key generate column name    |
| algorithm-ref | Attribute | Key generate algorithm name |

\<sharding:sharding-algorithm />

| *Name*    | *Type*    | *Description*                 |
| --------- | --------- | ----------------------------- |
| id        | Attribute | Sharding algorithm name       |
| type      | Attribute | Sharding algorithm type       |
| props (?) | Tag       | Sharding algorithm properties |

\<sharding:key-generate-algorithm />

| *Name*    | *Type*    | *Description*                     |
| --------- | --------- | --------------------------------- |
| id        | Attribute | Key generate algorithm name       |
| type      | Attribute | Key generate algorithm type       |
| props (?) | Tag       | Key generate algorithm properties |

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding) and [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen) for more details about type of algorithm.

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.
