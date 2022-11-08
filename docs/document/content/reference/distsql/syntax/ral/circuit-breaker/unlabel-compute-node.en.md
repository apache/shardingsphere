+++
title = "UNLABEL COMPUTE NODES"
weight = 6
+++

### Description

The `UNLABEL COMPUTE NODES` syntax is used to remove specified label from `PROXY` instance.

### Syntax

```sql
UnlabelComputeNode ::=
  'UNLABEL' 'COMPUTE' 'NODE' instance_id 'WITH' labelName

instance_id ::=
  string

labelName ::=
  identifier
```

### Supplement

- needs to be obtained through [SHOW COMPUTE NODES](/en/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

### Example

- Remove specified label from `PROXY` instance

```sql
UNLABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_1;
```

### Reserved word

`UNLABEL`, `COMPUTE`, `NODES`, `WITH`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
