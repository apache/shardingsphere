+++
title = "ROLLBACK MIGRATION"
weight = 17
+++

### Description

The `ROLLBACK MIGRATION` syntax is used to rollback migration process.

### Syntax

```sql
RollbackMigration ::=
  'ROLLBACK' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

- After the statement is executed, the target will be cleaned up

### Example

- Rollback migration process

```sql
ROLLBACK MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`ROLLBACK`, `MIGRATION`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
