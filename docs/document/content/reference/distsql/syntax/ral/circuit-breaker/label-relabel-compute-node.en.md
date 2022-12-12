+++
title = "LABEL|RELABEL COMPUTE NODES"
weight = 6
+++

### Description

The `LABEL|RELABEL COMPUTE NODES` syntax is used to label `PROXY` instance.

### Syntax

```sql
LableRelabelComputeNodes ::=
  ('LABEL'|'RELABEL') 'COMPUTE' 'NODE' instance_id 'WITH' labelName

instance_id ::=
  string

labelName ::=
  identifier
```

### Supplement

- needs to be obtained through [SHOW COMPUTE NODES](/en/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) syntax query

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

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/en/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
