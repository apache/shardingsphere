+++
title = "ENABLE/DISABLE COMPUTE NODE"
weight = 4
aliases = ["/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/enable-disable-compute-node/"]
+++

## Description

The `ENABLE/DISABLE COMPUTE NODE` syntax is used to enable or disable a specified compute node in cluster mode.

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

- `instanceId` needs to be obtained through [SHOW COMPUTE NODES](/en/user-manual/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

- The compute node executing this statement cannot be disabled.

### Example

- Disable a specified compute node
```sql
DISABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

- Enable a specified compute node

```sql
ENABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

### Reserved word

`ENABLE`, `DISABLE`, `COMPUTE`, `NODE`

### Related links

- [Reserved word](/en/user-manual/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/user-manual/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
