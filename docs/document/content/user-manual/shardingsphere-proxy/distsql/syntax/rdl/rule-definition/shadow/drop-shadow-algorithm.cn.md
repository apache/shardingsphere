+++
title = "DROP SHADOW ALGORITHM"
weight = 7
+++

## 描述

`DROP SHADOW ALGORITHM` 语法用于从当前逻辑库中删除影子库压测算法。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShadowAlgorithm ::=
  'DROP' 'SHADOW' 'ALGORITHM' ifExists? algorithmName (',' algorithmName)*

ifExists ::=
  'IF' 'EXISTS'

algorithmName ::=
  identifier

```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `shadow algorithm not exists` 错误。

### 示例

- 删除多个影子库压测算法
 
```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0, shadow_rule_t_order_item_sql_hint_0;
```

- 删除单个影子库压测算法

```sql
DROP SHADOW ALGORITHM shadow_rule_t_order_sql_hint_0;
```

- 使用 `ifExists` 子句删除影子库压测算法

```sql
DROP SHADOW ALGORITHM IF EXISTS shadow_rule_t_order_sql_hint_0;
```

### 保留字

`DROP`、`SHADOW`、`ALGORITHM`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
