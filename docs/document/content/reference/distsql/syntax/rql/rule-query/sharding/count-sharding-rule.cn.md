+++
title = "COUNT SHARDING RULE"
weight = 16
+++

### 描述

`COUNT SHARDING RULE` 语法用于查询指定逻辑库中的分片规则数量。

### 语法

```sql
CountShardingRule::=
  'COUNT' 'SHARDING' 'RULE' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列        | 说明            |
| --------- | ---------------|
| rule_name | 规则类型        |
| database  | 规则所属逻辑库   |
| count     | 规则数量        |


### 示例

- 查询指定逻辑库中的分片规则数量

```sql
COUNT SHARDING RULE FROM test1;
```

```sql
mysql> COUNT SHARDING RULE FROM test1;
+--------------------------+----------+-------+
| rule_name                | database | count |
+--------------------------+----------+-------+
| sharding_table           | test1    | 2     |
| sharding_table_reference | test1    | 2     |
| broadcast_table          | test1    | 0     |
+--------------------------+----------+-------+
3 rows in set (0.00 sec)
```

- 查询当前逻辑库中的分片规则数量

```sql
COUNT SHARDING RULE;
```

```sql
mysql> COUNT SHARDING RULE;
+--------------------------+----------+-------+
| rule_name                | database | count |
+--------------------------+----------+-------+
| sharding_table           | test1    | 2     |
| sharding_table_reference | test1    | 2     |
| broadcast_table          | test1    | 0     |
+--------------------------+----------+-------+
3 rows in set (0.00 sec)
```

### 保留字

`COUNT`、`SHARDING`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

