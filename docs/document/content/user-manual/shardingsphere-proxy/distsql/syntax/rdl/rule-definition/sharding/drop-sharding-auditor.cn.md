+++
title = "DROP SHARDING AUDITOR"
weight = 17
+++

## 描述

`DROP SHARDING AUDITOR` 语法用于从当前逻辑库中删除分片审计算法。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingAuditor ::=
  'DROP' 'SHARDING' 'AUDITOR' ifExists? auditorName (',' auditorName)*

ifExists ::=
  'IF' 'EXISTS'

auditorName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Sharding auditor not exists` 错误。
- 分片表规则或默认审计策略正在引用的分片审计算法不能被删除。

### 示例

- 删除分片审计算法

```sql
DROP SHARDING AUDITOR sharding_key_required_auditor;
```

- 使用 `ifExists` 子句删除多个分片审计算法

```sql
DROP SHARDING AUDITOR IF EXISTS sharding_key_required_auditor, custom_auditor;
```

### 保留字

`DROP`、`SHARDING`、`AUDITOR`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
