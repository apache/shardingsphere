+++
title = "DROP SHARDING AUDITOR"
weight = 17
+++

## Description

The `DROP SHARDING AUDITOR` syntax is used to drop sharding auditors from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropShardingAuditor ::=
  'DROP' 'SHARDING' 'AUDITOR' ifExists? auditorName (',' auditorName)*

ifExists ::=
  'IF' 'EXISTS'

auditorName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `ifExists` clause is used to avoid a `Sharding auditor not exists` error.
- Sharding auditors referenced by sharding table rules or the default audit strategy cannot be dropped.

### Example

- Drop a sharding auditor

```sql
DROP SHARDING AUDITOR sharding_key_required_auditor;
```

- Drop multiple sharding auditors with `ifExists` clause

```sql
DROP SHARDING AUDITOR IF EXISTS sharding_key_required_auditor, custom_auditor;
```

### Reserved word

`DROP`, `SHARDING`, `AUDITOR`, `IF`, `EXISTS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
