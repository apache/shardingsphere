+++
title = "START MIGRATION"
weight = 16
+++

### Description

The `START MIGRATION` syntax is used to start migration process.

### Syntax

```sql
StartMigration ::=
  'START' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

### Example

- Start migration process

```sql
START MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`START`, `MIGRATION`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
