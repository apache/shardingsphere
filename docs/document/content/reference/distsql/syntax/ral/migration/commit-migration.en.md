+++
title = "COMMIT MIGRATION"
weight = 18
+++

### Description

The `COMMIT MIGRATION` syntax is used to commit migration process.

### Syntax

```sql
CommitMigration ::=
  'COMMIT' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through `SHOW MIGRATION LIST` syntax query

### Example

- Commit migration process

```sql
COMMIT MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`COMMIT`, `MIGRATION`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
