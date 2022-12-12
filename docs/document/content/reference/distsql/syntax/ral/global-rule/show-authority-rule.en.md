+++
title = "SHOW AUTHORITY RULE"
weight = 2
+++

### Description

The `SHOW AUTHORITY RULE` syntax is used to query authority rule configuration.

### Syntax

```sql
ShowAuthorityRule ::=
  'SHOW' 'AUTHORITY' 'RULE'
```

### Return Value Description

| Colume      | Description             |
|-------------|-------------------------|
| users       | users                   |
| provider    | privilege provider type |
| props       | privilege properties    |

### Example

- Query authority rule configuration

```sql
SHOW AUTHORITY RULE;
```

```sql
mysql> SHOW AUTHORITY RULE;
+--------------------+---------------+-------+
| users              | provider      | props |
+--------------------+---------------+-------+
| root@%; sharding@% | ALL_PERMITTED |       |
+--------------------+---------------+-------+
1 row in set (0.07 sec)
```

### Reserved word

`SHOW`, `AUTHORITY`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
