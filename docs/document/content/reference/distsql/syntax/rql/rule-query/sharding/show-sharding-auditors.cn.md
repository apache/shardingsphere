+++
title = "SHOW SHARDING AUDITORS"
weight = 8
+++

### 描述

`SHOW SHARDING AUDITORS` 语法用于查询指定逻辑库中的分片审计生成器。

### 语法

```
ShowShardingAuditors::=
  'SHOW' 'SHARDING' 'AUDITOR'('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                      | 说明            |
| -----------------------|-----------------|
| name                   | 分片审计算法名称  |
| type                   | 分片审计算法类型  |
| props                  | 分片审计算法参数  |

### 示例

- 查询指定逻辑库中的分片审计生成器

```sql
SHOW SHARDING AUDITORS FROM test1;
```

```sql
mysql> SHOW SHARDING AUDITORS FROM test1;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions | {}    |
+-------------------------------+-------------------------+-------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的分片审计生成器

```sql
SHOW SHARDING AUDITORS;
```

```sql
mysql> SHOW SHARDING AUDITORS;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions | {}    |
+-------------------------------+-------------------------+-------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`AUDITORS`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

