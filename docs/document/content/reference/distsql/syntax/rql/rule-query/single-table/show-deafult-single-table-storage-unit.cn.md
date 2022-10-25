+++
title = "SHOW DEFAULT SINGLE TABLE STORAGE UNIT"
weight = 2
+++

### 描述

`SHOW DEFAULT SINGLE TABLE STORAGE UNIT` 语法用于查询指定逻辑库中的存储单元信息。

### 语法

```
ShowDefaultSingleTableStorageUnit::=
  'SHOW' 'DEFAULT' 'SINGLE' 'TABLE' 'STORAGE' 'UNIT' ('FROM' databaseName)?
  
databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                  | 说明      |
|--------------------|---------|
| storage_unit_name  | 存储单元名称  |

### 示例

- 查询当前逻辑库中的存储单元信息

```sql
SHOW DEFAULT SINGLE TABLE STORAGE UNIT
```

```sql
sql> SHOW DEFAULT SINGLE TABLE STORAGE UNIT;
+-------------------+
| storage_unit_name |
+-------------------+
|  ds_0             |
+-------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`DEFAULT`、`SINGLE`、`TABLE`、`STORAGE`、`UNIT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

