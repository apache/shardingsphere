+++
title = "COMMIT MIGRATION"
weight = 16
+++

### Description

The `COMMIT MIGRATION` syntax is used to commit migration process.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CommitMigration ::=
  'COMMIT' 'MIGRATION'  migrationJobId 

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

### Example

- Commit migration process

```sql
COMMIT MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`COMMIT`, `MIGRATION`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
