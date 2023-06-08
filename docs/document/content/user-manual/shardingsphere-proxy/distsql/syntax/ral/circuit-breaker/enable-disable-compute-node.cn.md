+++
title = "ENABLE/DISABLE COMPUTE NODE"
weight = 4
+++

## 描述

`ENABLE/DISABLE COMPUTE NODE` 语法用于启用/禁用指定 proxy 实例。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
EnableDisableComputeNode ::=
  ('ENABLE' | 'DISABLE') 'COMPUTE' 'NODE' instanceId

instanceId ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `instanceId` 需要通过 [SHOW COMPUTE NODES](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/) 语法查询得到

- 不可禁用当前正在使用的 proxy 实例

### 示例

- 禁用指定 proxy 实例

```sql
DISABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

- 启用指定 proxy 实例

```sql
ENABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

### 保留字

`ENABLE`、`DISABLE`、`COMPUTE`、`NODE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW COMPUTE NODES](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/circuit-breaker/show-compute-nodes/)
