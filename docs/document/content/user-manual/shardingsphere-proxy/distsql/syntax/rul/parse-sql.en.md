+++
title = "PARSE SQL"
weight = 1
+++

### Description

The `PARSE SQL` syntax is used to parse `SQL` and output abstract syntax tree.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ParseSql ::=
  'PARSE' sqlStatement  
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column                  | Description                    |
|-------------------------|--------------------------------|
| parsed_statement        | parsed SQL statement type      |
| parsed_statement_detail | detail of the parsed statement |

### Example

- Parse `SQL` and output abstract syntax tree

```sql
PARSE SELECT * FROM t_order;
```

```sql
mysql> PARSE SELECT * FROM t_order;
+----------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| parsed_statement     | parsed_statement_detail                                                                                                                                                                                                                                                                               |
+----------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| SelectStatement      | {"projections":{"startIndex":7,"stopIndex":7,"projections":[{"startIndex":7,"stopIndex":7}],"distinctRow":false},"from":{"tableName":{"startIndex":14,"stopIndex":20,"identifier":{"value":"t_order","quoteCharacter":"NONE"}}},"parameterCount":0,"parameterMarkerSegments":[],"commentSegments":[]} |
+----------------------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.01 sec)
```

### Reserved word

`PARSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
