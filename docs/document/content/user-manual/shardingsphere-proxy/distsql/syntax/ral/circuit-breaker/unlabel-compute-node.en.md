+++
title = "UNLABEL COMPUTE NODE"
weight = 6
+++

### Description

The `UNLABEL COMPUTE NODE` syntax is used to remove specified labels from `PROXY` instance.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
UnlabelComputeNode ::=
  'UNLABEL' 'COMPUTE' 'NODE' instance_id ('WITH' labelName (',' labelName)*)?

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

- `instance_id` needs to be obtained through [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

### Example

- Remove specified label from `PROXY` instance

```sql
UNLABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_1;
```

### Reserved word

`UNLABEL`, `COMPUTE`, `NODE`, `WITH`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
