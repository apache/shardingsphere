+++
title = "COUNT ENCRYPT RULE"
weight = 4
+++

### 描述

`COUNT ENCRYPT RULE` 语法用于查询指定逻辑库中的加密规则数量。

### 语法

```sql
CountEncryptRule::=
  'COUNT' 'ENCRYPT' 'RULE' ('FROM' databaseName)?

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

- 查询指定逻辑库中的加密规则数量

```sql
COUNT ENCRYPT RULE FROM test1;
```

```sql
mysql> COUNT ENCRYPT RULE FROM test1;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| encrypt   | test1    | 2     |
+-----------+----------+-------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的加密规则数量

```sql
COUNT ENCRYPT RULE;
```

```sql
mysql> COUNT ENCRYPT RULE;
+-----------+----------+-------+
| rule_name | database | count |
+-----------+----------+-------+
| encrypt   | test1    | 2     |
+-----------+----------+-------+
1 row in set (0.01 sec)
```

### 保留字

`COUNT`、`ENCRYPT`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

