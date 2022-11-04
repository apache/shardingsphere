+++
title = "SHOW TRANSACTION RULE"
weight = 3
+++

### Description

The `SHOW TRANSACTION RULE` syntax is used to query transaction rule configuration.

### Syntax

```sql
ShowTransactionRule ::=
  'SHOW' 'TRANSACTION' 'RULE'
```

### Return Value Description

| Colume      | Description             |
|-------------|-------------------------|
| users       | users                   |
| provider    | privilege provider type |
| props       | privilege properties    |

### Example

- Query transaction rule configuration

```sql
SHOW TRANSACTION RULE;
```

```sql
mysql> SHOW TRANSACTION RULE;
+--------------+---------------+-------+
| default_type | provider_type | props |
+--------------+---------------+-------+
| LOCAL        |               |       |
+--------------+---------------+-------+
1 row in set (0.05 sec)
```

### Reserved word

`SHOW`, `TRANSACTION`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
