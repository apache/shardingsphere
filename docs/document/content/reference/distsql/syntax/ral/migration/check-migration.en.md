+++
title = "CHECK MIGRATION "
weight = 11
+++

### Description

The `CHECK MIGRATION LIST` syntax is used to check data consistancy in migration job.

### Syntax

```sql
ShowMigrationList ::=
  'CHECK' 'MIGRATION' migrationJobId 'BY' 'TYPE' '(' 'NAME' '=' migrationCheckAlgorithmType ')'

migrationJobId ::=
  string

migrationCheckAlgorithmType ::=
  string
```

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/) syntax query

- `migrationCheckAlgorithmType` needs to be obtained through [SHOW MIGRATION CHECK ALGORITHMS](/en/reference/distsql/syntax/ral/migration/show-migration-check-algorithm/) syntax query

### Example

- check data consistancy in migration job

```sql
CHECK MIGRATION 'j01016e501b498ed1bdb2c373a2e85e2529a6' BY TYPE (NAME='CRC32_MATCH');
```

### Reserved word

`CHECK`, `MIGRATION`, `BY`, `TYPE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/reference/distsql/syntax/ral/migration/show-migration-list/)
- [SHOW MIGRATION CHECK ALGORITHMS](/en/reference/distsql/syntax/ral/migration/show-migration-check-algorithm/)
