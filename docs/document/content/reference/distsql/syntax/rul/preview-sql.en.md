+++
title = "PREVIEW SQL"
weight = 3
+++

### Description

The `PREVIEW SQL` syntax is used to preview `SQL` execution plan.

### Syntax

```sql
PreviewSql ::=
  'PREVIEW' sqlStatement  
```

### Return Value Description

| Column                   | Description                 |
|--------------------------|-----------------------------|
| data_source_name         | storage unit name           |
| actual_sql               | actual excute SQL statement |

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

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
