+++
title = "Shadow DB"
weight = 4
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd)

\<shadow:rule />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Spring Bean Id                  |
| column      | Attribute  | Shadow column name              |
| mappings(?) | Tag        | Mapping relationship between production database and shadow database |

\<shadow:mapping />

| *Name*                   | *Type*    | *Description*               |
| ------------------------ | --------- | --------------------------- |
| product-data-source-name | Attribute  | Production database name   |
| shadow-data-source-name  | Attribute  | Shadow database name       |
