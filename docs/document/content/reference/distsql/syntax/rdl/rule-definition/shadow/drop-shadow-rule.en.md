+++
title = "DROP SHADOW RULE"
weight = 4
+++

## Description

The `DROP SHADOW RULE` syntax is used to drop shadow rule for specified database

### Syntax

```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'TABLE' 'RULE' shadowRuleName ('FROM' databaseName)?

shadowRuleName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Example

- Drop shadow rule for specified database

```sql
DROP SHADOW RULE shadow_rule FROM test1;
```

- Drop shadow rule for current database

```sql
DROP SHADOW RULE shadow_rule;
```

### Reserved word

`DROP`, `SHODOW`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
