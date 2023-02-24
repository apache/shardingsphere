+++
title = "ROLLBACK MIGRATION"
weight = 17
+++

### Description

The `ROLLBACK MIGRATION` syntax is used to rollback migration process.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RollbackMigration ::=
  'ROLLBACK' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}


### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/) syntax query

- After the statement is executed, the target will be cleaned up

### Example

- Rollback migration process

```sql
ROLLBACK MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`ROLLBACK`, `MIGRATION`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
