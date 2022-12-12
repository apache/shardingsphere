+++
title = "STOP MIGRATION CHECK"
weight = 13
+++

### Description

The `STOP MIGRATION CHECK` syntax is used to stop migration check process.

### Syntax

```sql
StopMigrationCheck ::=
  'STOP' 'MIGRATION' 'CHECK' migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

### Example

- Stop migration check process

```sql
STOP MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`STOP`, `MIGRATION`, `CHECK`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
