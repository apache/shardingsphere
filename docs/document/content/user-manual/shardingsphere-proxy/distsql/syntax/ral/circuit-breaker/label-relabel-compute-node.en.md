+++
title = "LABEL|RELABEL COMPUTE NODES"
weight = 5
+++

### Description

The `LABEL|RELABEL COMPUTE NODES` syntax is used to label `PROXY` instance.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
LableRelabelComputeNodes ::=
  ('LABEL' | 'RELABEL') 'COMPUTE' 'NODE' instance_id 'WITH' labelName

instance_id ::=
  string

labelName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- needs to be obtained through [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

- `RELABEL` is used to relabel `PROXY` instance

### Example

- Label `PROXY` instance

```sql
LABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_1;
```

- Relabel `PROXY` instance

```sql
RELABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_2;
```

### Reserved word

`LABEL`, `RELABEL`, `COMPUTE`, `NODES`, `WITH`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
