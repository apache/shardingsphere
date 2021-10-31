+++
title = "Shadow DB"
weight = 4
+++

## Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd)

\<shadow:rule />

| *Name* | *Type*  | *Description* |
| ------ | ------- | ------------- |
| id     | Attribute | Spring Bean Id |
| enable | Attribute | Shadow DB switch. Optional values: true/false, the default is false |
| data-source(?)  | Tag | Shadow data source configuration |
| default-shadow-algorithm-name(?)  | Tag  | Default shadow algorithm configuration |
| shadow-table(?) | Tag | Shadow table configuration |

\<shadow:data-source />

| *Name* | *Type*  | *Description* |
| ------ | ------- | ------------- |
| id | Attribute | Spring Bean Id |
| source-data-source-name | Attribute | Production data source name |
| shadow-data-source-name | Attribute | Shadow data source name |

\<shadow:default-shadow-algorithm-name />
| *Name* | *Type*  | *Description* |
| ----- | ------ | ------ |
| name | Attribute | Default shadow algorithm name |

\<shadow:shadow-table />

| *Name* | *Type*  | *Description* |
| ------ | ------- | ------------- |
| name | Attribute | Shadow table name |
| data-sources | Attribute | Shadow table location shadow data source names (multiple values are separated by ",") |
| algorithm (?) | Tag | Shadow table location shadow algorithm configuration |

\<shadow:algorithm />

| *Name* | *Type*  | *Description* |
| ------ | ------- | ------------- |
| shadow-algorithm-ref | Attribute | Shadow table location shadow algorithm name |

\<shadow:shadow-algorithm />

| *Name*    | *Type* | *Description*        |
| --------- | ----- | ------------- |
| id        | Attribute  | Shadow algorithm name |
| type      | Attribute  | Shadow algorithm type |
| props (?) | Attribute  | Shadow algorithm property configuration |
