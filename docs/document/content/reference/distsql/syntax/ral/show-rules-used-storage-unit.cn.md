+++
title = "SHOW RULES USED STORAGE UNIT"
weight = 11
+++

### 描述

`SHOW RULES USED STORAGE UNIT` 语法用于查询指定逻辑库中使用指定存储单元的规则

### 语法

```sql
ShowRulesUsedStorageUnit ::=
  'SHOW' 'RULES' 'USED' 'STORAGE' 'UNIT' storageUnitName ('FROM' databaseName)?

storageUnitName ::=
  identifier

databaseName ::=
  identifier
```

### 返回值说明

| 列          | 说明       |
|-------------|-----------|
| type        | 规则类型   |
| name        | 规则名称   |

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE。` 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 查询指定逻辑库中使用指定存储单元的规则

```sql
SHOW RULES USED STORAGE UNIT su_1 FROM test1;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT su_1 FROM test1;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

- 查询当前逻辑库中使用指定存储单元的规则

```sql
SHOW RULES USED STORAGE UNIT su_1;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT su_1;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

### 保留字

`SHOW`、`RULES`、`USED`、`STORAGE`、`UNIT`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)