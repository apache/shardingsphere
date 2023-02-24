+++
title = "ENABLE/DISABLE COMPUTE NODE"
weight = 4
+++

## Description

The `ENABLE/DISABLE COMPUTE NODE` syntax is used enable/disable a specified proxy instance

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
EnableDisableComputeNode ::=
  ('ENABLE' | 'DISABLE') 'COMPUTE' 'NODE' instanceId

instanceId ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `instanceId` needs to be obtained through [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

- The currently in-use proxy instance cannot be disabled

### Example

- Disable a specified proxy instance
```sql
DISABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

- Enable a specified proxy instance

```sql
ENABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

### Reserved word

`ENABLE`, `DISABLE`, `COMPUTE`, `NODE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
