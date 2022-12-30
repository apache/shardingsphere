+++
title = "FORMAT SQL"
weight = 2
+++

### Description

The `FORMAT SQL` syntax is used to parse `SQL` and output formated `SQL` statement.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ParseSql ::=
  'FORMAT' sqlStatement  
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column                   | Description               |
|--------------------------|---------------------------|
| formatted_result         | formated SQL statement    |

### Example

- Parse `SQL` and output formated `SQL` statement

```sql
FORMAT SELECT * FROM t_order;
```

```sql
mysql> FORMAT SELECT * FROM t_order;
+-------------------------+
| formatted_result        |
+-------------------------+
| SELECT *
FROM t_order; |
+-------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`FORMAT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
