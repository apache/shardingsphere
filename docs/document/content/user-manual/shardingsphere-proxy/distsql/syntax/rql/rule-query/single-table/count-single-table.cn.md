+++
title = "COUNT SINGLE TABLE"
weight = 3
+++

### 描述

`COUNT SINGLE TABLE` 语法用于查询指定逻辑库中的单表个数。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CountSingleTable::=
  'COUNT' 'SINGLE' 'TABLE' ('FROM' databaseName)?
  
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

| 列         | 说明          |
|-----------|---------------|
| rule_name | 单表规则名称    |
| database  | 单表所在的数据库名称 |
| count     | 单表个数        |

### 示例

- 查询指定逻辑库中的单表规则个数

```sql
COUNT SINGLE TABLE FROM sharding_db;
```

```sql
mysql> COUNT SINGLE TABLE FROM sharding_db;
+-----------+-------------+-------+
| rule_name | database    | count |
+-----------+-------------+-------+
| single    | sharding_db | 2     |
+-----------+-------------+-------+
1 row in set (0.02 sec)
```

### 保留字

`COUNT`、`SINGLE`、`TABLE`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
