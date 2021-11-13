+++
title = "Shadow Algorithm"
weight = 6
+++

## Column Shadow Algorithm

### Column Value Match Shadow Algorithm

Type：VALUE_MATCH

Attributes:

| *Name*      | *DataType* | *Description*  |
| -------------- | --------- | ------- |
| column         | String    | Shadow column |
| operation      | String    | SQL operation type（INSERT, UPDATE, DELETE, SELECT） |
| value          | String    | Shadow column matching value |

### Column Regex Match Shadow Algorithm

Type: REGEX_MATCH

Attributes:

| *Name*      | *DataType* | *Description*  |
| -------------- | --------- | ------- |
| column         | String    | Shadow column |
| operation      | String    | SQL operation type (insert, update, delete, select) |
| regex          | String    | Shadow column matching regular expression |

## Note Shadow Algorithm

### Simple SQL Note Shadow Algorithm

Type: SIMPLE_HINT

Attributes:

> Configure at least a set of arbitrary key-value pairs. For example: foo:bar

| *Name*          | *DataType* | *Description*    |
| --------------  | ---------  | --------- |
| foo             | String     | bar       |
