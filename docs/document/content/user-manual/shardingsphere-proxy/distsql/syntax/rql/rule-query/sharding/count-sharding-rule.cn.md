+++
title = "COUNT SHARDING RULE"
weight = 15
+++

### 描述

`COUNT SHARDING RULE` 语法用于查询指定逻辑库中的分片规则数量。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CountShardingRule::=
  'COUNT' 'SHARDING' 'RULE' ('FROM' databaseName)?

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

- 查询指定逻辑库中的分片规则数量

```sql
COUNT SHARDING RULE FROM sharding_db;
```

```sql
mysql> COUNT SHARDING RULE FROM sharding_db;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| sharding_table           | sharding_db    | 2     |
| sharding_table_reference | sharding_db    | 2     |
+--------------------------+----------------+-------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中的分片规则数量

```sql
COUNT SHARDING RULE;
```

```sql
mysql> COUNT SHARDING RULE;
+--------------------------+----------------+-------+
| rule_name                | database       | count |
+--------------------------+----------------+-------+
| sharding_table           | sharding_db    | 2     |
| sharding_table_reference | sharding_db    | 2     |
+--------------------------+----------------+-------+
2 rows in set (0.00 sec)
```

### 保留字

`COUNT`、`SHARDING`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

