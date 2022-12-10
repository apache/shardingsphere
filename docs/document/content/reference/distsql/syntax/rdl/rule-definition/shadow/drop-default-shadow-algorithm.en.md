+++
title = "DROP DEFAULT SHADOW ALGORITHM"
weight = 7
+++

## Description

The `DROP DEFAULT SHADOW ALGORITHM` syntax is used to drop default shadow algorithm for specified database

### Syntax

```sql
DropDefaultShadowAlgorithm ::=
  'DROP' 'DEFAULT' 'SHADOW' 'ALGORITHM' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Example

- Drop default shadow algorithm for specified database

```sql
DROP DEFAULT SHADOW ALGORITHM FROM test1;
```

- Drop default shadow algorithm for current database

```sql
DROP DEFAULT SHADOW ALGORITHM;
```

### Reserved word

`DROP`, `DEFAULT`, `SHODOW`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
