+++
title = "DROP SHARDING TABLE RULE"
weight = 3
+++

## 描述

`DROP SHARDING TABLE RULE` 语法用于从当前逻辑库中删除分片表规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingTableRule ::=
  'DROP' 'SHARDING' 'TABLE' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` 子句用于避免 `Sharding rule not exists` 错误。

### 示例

- 删除多个分片表规则
 
```sql
DROP SHARDING TABLE RULE t_order, t_order_item;
```

- 删除单个分片表规则

```sql
DROP SHARDING TABLE RULE t_order;
```

- 使用 `ifExists` 子句删除分片规则

```sql
DROP SHARDING TABLE RULE IF EXISTS t_order;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`RULE`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
