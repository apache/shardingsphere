+++
title = "DROP MIGRATION CHECK"
weight = 13
+++

### Description

The `DROP MIGRATION CHECK` syntax is used to drop migration check results for the specified migration job.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropMigrationCheck ::=
  'DROP' 'MIGRATION' 'CHECK' migrationJobId

migrationJobId ::=
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `migrationJobId` needs to be obtained through [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/).

### Example

- Drop migration check results

```sql
DROP MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`DROP`, `MIGRATION`, `CHECK`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
