+++
title = "START MIGRATION CHECK"
weight = 12
+++

### Description

The `START MIGRATION CHECK` syntax is used to stop migration check process.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
StartMigrationCheck ::=
  'START' 'MIGRATION' 'CHECK' migrationJobId 

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

- Stop migration check process

```sql
START MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### Reserved word

`START`, `MIGRATION`, `CHECK`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
