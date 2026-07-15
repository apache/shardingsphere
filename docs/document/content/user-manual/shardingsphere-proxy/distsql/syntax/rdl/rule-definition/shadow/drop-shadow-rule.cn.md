+++
title = "DROP SHADOW RULE"
weight = 3
+++

## 描述

`DROP SHADOW RULE` 语法用于从当前逻辑库中删除影子库压测规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'RULE' ifExists? ruleName (',' ruleName)*

ifExists ::=
  'IF' 'EXISTS'

ruleName ::=
  identifier

```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Shadow rule not exists` 错误。

### 示例

- 删除影子库压测规则
 
```sql
DROP SHADOW RULE shadow_rule;
```

- 删除多个影子库压测规则

```sql
DROP SHADOW RULE shadow_rule, shadow_rule_1;
```

- 使用 `ifExists` 子句删除影子库压测规则

```sql
DROP SHADOW RULE IF EXISTS shadow_rule;
```

### 保留字

`DROP`、`SHADOW`、`RULE`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
