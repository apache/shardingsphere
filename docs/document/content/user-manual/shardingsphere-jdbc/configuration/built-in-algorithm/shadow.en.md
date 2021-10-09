+++
title = "Shadow Algorithm"
weight = 5
+++

## Column Shadow Algorithm

### Column Regex Match Shadow Algorithm

Type: COLUMN_REGEX_MATCH

Attributes:

| *Name*      | *DataType* | *Description*  |
| -------------- | --------- | ------- |
| column         | String    | Shadow column |
| operation      | String    | SQL operation type (insert, update, delete, select) |
| regex          | String    | Regular expression |

## Note Shadow Algorithm

### Simple SQL Note Shadow Algorithm

Type: SIMPLE_NOTE

Attributes:

> Configure at least a set of arbitrary key-value pairs. For example: foo:bar

| *Name*          | *DataType* | *Description*    |
| --------------  | ---------  | --------- |
| foo             | String     | bar       |
