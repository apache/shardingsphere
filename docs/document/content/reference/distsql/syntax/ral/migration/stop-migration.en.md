+++
title = "STOP MIGRATION"
weight = 15
+++

### Description

The `STOP MIGRATION` syntax is used to stop migration process.

### Syntax

```sql
StopMigration ::=
  'STOP' 'MIGRATION' migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

### Example

- Stop migration process

```sql
STOP MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`STOP`, `MIGRATION`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
