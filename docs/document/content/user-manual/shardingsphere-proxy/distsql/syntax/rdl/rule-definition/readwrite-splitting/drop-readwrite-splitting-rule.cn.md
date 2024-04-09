+++
title = "DROP READWRITE_SPLITTING RULE"
weight = 3
+++

## 描述

`DROP READWRITE_SPLITTING RULE` 语法用于为指定逻辑库删除读写分离。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropReadwriteSplittingRule ::=
  'DROP' 'READWRITE_SPLITTING' 'RULE' ifExists? ruleName (',' ruleName)* ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

ruleName ::=
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
- `ifExists` 子句用于避免 `Readwrite-splitting rule not exists` 错误。

### 示例

- 为指定逻辑库删除读写分离规则
 
```sql
DROP READWRITE_SPLITTING RULE ms_group_1 FROM readwrite_splitting_db;
```

- 为当前逻辑库删除读写分离规则

```sql
DROP READWRITE_SPLITTING RULE ms_group_1;
```

- 使用 `ifExists` 子句删除读写分离规则

```sql
DROP READWRITE_SPLITTING RULE IF EXISTS ms_group_1;
```

### 保留字

`DROP`、`READWRITE_SPLITTING`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)