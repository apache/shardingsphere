+++
title = "PREVIEW SQL"
weight = 3
+++

### Description

The `PREVIEW SQL` syntax is used to preview `SQL` execution plan.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
PreviewSql ::=
  'PREVIEW' sqlStatement  
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column           | Description                 |
|------------------|-----------------------------|
| data_source_name | storage unit name           |
| actual_sql       | actual excute SQL statement |

### Example

- Preview `SQL` execution plan

```sql
PREVIEW SELECT * FROM t_order;
```

```sql
mysql> PREVIEW SELECT * FROM t_order;
+------------------+-----------------------+
| data_source_name | actual_sql            |
+------------------+-----------------------+
| su_1             | SELECT * FROM t_order |
+------------------+-----------------------+
1 row in set (0.18 sec)
```

### Reserved word

`PREVIEW`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
