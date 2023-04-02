+++
title = "COUNT SHADOW RULE"
weight = 5
+++

### 描述

`COUNT SHADOW RULE` 语法用于查询指定逻辑库中的影子库压测规则数量。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CountShadowRule::=
  'COUNT' 'SHADOW' 'RULE' ('FROM' databaseName)?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列         | 说明      |
|-----------|---------|
| rule_name | 规则类型    |
| database  | 规则所属逻辑库 |
| count     | 规则数量    |


### 示例

- 查询指定逻辑库中的影子库压测规则数量

```sql
COUNT SHADOW RULE FROM shadow_db;
```

```sql
mysql> COUNT SHADOW RULE FROM shadow_db;
+-----------+--------------+-------+
| rule_name | database     | count |
+-----------+--------------+-------+
| shadow    | shadow_db    | 1     |
+-----------+--------------+-------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的影子库压测规则数量

```sql
COUNT SHADOW RULE;
```

```sql
mysql> COUNT SHADOW RULE;
+-----------+--------------+-------+
| rule_name | database     | count |
+-----------+--------------+-------+
| shadow    | shadow_db    | 1     |
+-----------+--------------+-------+
1 row in set (0.01 sec)
```

### 保留字

`COUNT`、`SHADOW`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

