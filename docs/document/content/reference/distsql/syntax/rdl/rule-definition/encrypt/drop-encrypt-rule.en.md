+++
title = "DROP ENCRYPT RULE"
weight = 2
+++

## Description

The `DROP ENCRYPT RULE` syntax is used to drop an existing encryption rule.

### Syntax

```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' tableName ( ',' tableName )*
    
tableName ::=
  identifier
```

### Example

#### Drop an encrypt rule

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

### Reserved words

`DROP`, `ENCRYPT`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
