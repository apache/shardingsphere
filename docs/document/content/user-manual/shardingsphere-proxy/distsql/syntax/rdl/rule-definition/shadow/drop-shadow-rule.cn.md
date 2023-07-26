+++
title = "DROP SHADOW RULE"
weight = 3
+++

## 描述

`DROP SHADOW RULE` 语法用于为指定逻辑库删除影子库压测规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShadowRule ::=
  'DROP' 'SHADOW' 'RULE' ifExists? shadowRuleName ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

shadowRuleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`；
- `ifExists` 子句用于避免 `Shadow rule not exists` 错误。

### 示例

- 为指定数据库删除影子库压测规则
 
```sql
DROP SHADOW RULE shadow_rule FROM shadow_db;
```

- 为当前数据库删除影子库压测规则

```sql
DROP SHADOW RULE shadow_rule;
```

- 使用 `ifExists` 子句删除影子库压测规则

```sql
DROP SHADOW RULE IF EXISTS shadow_rule;
```

### 保留字

`DROP`、`SHADOW`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)