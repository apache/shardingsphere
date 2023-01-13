+++
title = "COUNT SINGLE_TABLE RULE"
weight = 4
+++

### 描述

`COUNT SINGLE_TABLE RULE` 语法用于查询指定逻辑库中的单表规则个数。

### 语法

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CountSingleTableRule::=
  'COUNT' 'SINGLE_TABLE' 'RULE' ('FROM' databaseName)?
  
databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}


### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列        | 说明                 |
|-----------|---------------------|
| rule_name | 规则名称              |
| database  | 单表所在的数据库名称    |
| count     | 规则个数              |

### 示例

- 查询当前逻辑库中的单表规则个数

```sql
COUNT SINGLE_TABLE RULE
```

```sql
mysql> COUNT SINGLE_TABLE RULE;
+--------------+----------+-------+
| rule_name    | database | count |
+--------------+----------+-------+
| t_single_0   | ds       | 2     |
+--------------+----------+-------+
1 row in set (0.02 sec)
```

### 保留字

`COUNT`、`SINGLE_TABLE`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

