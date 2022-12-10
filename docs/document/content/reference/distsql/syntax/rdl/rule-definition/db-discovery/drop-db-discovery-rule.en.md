+++
title = "DROP DB_DISCOVERY RULE"
weight = 4
+++

## Description

The `DROP DB_DISCOVERY RULE` syntax is used to drop database discovery rule for specified database

### Syntax

```sql
DropDatabaseDiscoveryRule ::=
  'DROP' 'DB_DISCOVERY' 'RULE'  dbDiscoveryRuleName (',' dbDiscoveryRuleName)*  ('FROM' databaseName)?

dbDiscoveryRuleName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Example

- Drop mutiple database discovery rule for specified database

```sql
DROP DB_DISCOVERY RULE group_0, group_1 FROM test1;
```

- Drop single database discovery rule for current database

```sql
DROP DB_DISCOVERY RULE group_0;
```

### Reserved word

`DROP`, `DB_DISCOVERY`, `RULE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
