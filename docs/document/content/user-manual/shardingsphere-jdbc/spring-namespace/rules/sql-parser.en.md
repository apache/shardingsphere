+++
title = "SQL Parser"
weight = 6
+++

## Configuration Item Explanation

Namespace：[http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.1.2.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sql-parser/sql-parser-5.1.2.xsd)

\<sql-parser:rule />

| *Name*                   | *Type*    | *Description*           |
|--------------------------|-----------|----------------|
| id                       | Attribute | Spring Bean Id |
| sql-comment-parse-enable | Attribute | Whether to parse SQL comments    |
| parse-tree-cache-ref     | Attribute | Parse tree local cache name      |
| sql-statement-cache-ref  | Attribute | SQL statement local cache name   |

\<sql-parser:cache-option />

| *Name*                        | *Type* | *Description*               |
|-----------------------------| ----- |--------------------|
| id                          | Attribute  | Local cache configuration item name          |
| initial-capacity            | Attribute  | Initial capacity of local cache           |
| maximum-size                | Attribute  | Maximum capacity of local cache             |
