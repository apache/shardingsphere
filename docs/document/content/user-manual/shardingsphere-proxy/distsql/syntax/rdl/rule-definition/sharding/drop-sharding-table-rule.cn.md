+++
title = "DROP SHARDING TABLE RULE"
weight = 3
+++

## 描述

`DROP SHARDING TABLE RULE` 语法用于删除指定逻辑库的指定分片规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingTableRule ::=
  'DROP' 'SHARDING' 'TABLE' 'RULE' ifExists? ruleName (',' ruleName)*  ('FROM' databaseName)?

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
- `ifExists` 子句用于避免 `Sharding rule not exists` 错误。

### 示例

- 为指定逻辑库删除多个指定分片规则
 
```sql
DROP SHARDING TABLE RULE t_order, t_order_item FROM sharding_db;
```

- 为当前逻辑库删除单个指定分片规则

```sql
DROP SHARDING TABLE RULE t_order;
```

- 使用 `ifExists` 子句删除分片规则

```sql
DROP SHARDING TABLE RULE IF EXISTS t_order;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)