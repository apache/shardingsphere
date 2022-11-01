+++
title = "START MIGRATION CHECK"
weight = 14
+++

### Description

The `START MIGRATION CHECK` syntax is used to stop migration check process.

### Syntax

```sql
StartMigrationCheck ::=
  'START' 'MIGRATION' 'CHECK' migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through `SHOW MIGRATION LIST` syntax query

### Example

- Stop migration check process

```sql
START MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`START`, `MIGRATION`, `CHECK`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
