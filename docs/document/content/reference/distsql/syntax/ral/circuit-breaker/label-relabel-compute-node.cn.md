+++
title = "LABEL|RELABEL COMPUTE NODE"
weight = 6
+++

### 描述

`LABEL|RELABEL COMPUTE NODE` 语法用于为 `PROXY` 实例添加标签

### 语法

```sql
LableRelabelComputeNodes ::=
  ('LABEL'|'RELABEL') 'COMPUTE' 'NODE' instance_id 'WITH' labelName

instance_id ::=
  string

labelName ::=
  identifier
```
### 补充说明

- `instance_id` 需要通过 [SHOW COMPUTE NODES](/cn/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) 语法查询获得

- `RELABEL` 用于为 `PROXY` 实例修改标签

### 示例

- 为 `PROXY` 实例添加标签

```sql
LABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_1;
```

- 为 `PROXY` 实例修改标签
```sql
RELABEL COMPUTE NODE "0699e636-ade9-4681-b37a-65240c584bb3" WITH label_2;
```

### 保留字

`LABEL`、`RELABEL`、`COMPUTE`、`NODE` 、`WITH`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/cn/reference/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)