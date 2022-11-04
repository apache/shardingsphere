+++
title = "REFRESH TABLE METADATA"
weight = 6
+++

### 描述

`REFRESH TABLE METADATA` 语法用于刷新表元数据

### 语法

```sql
RefreshTableMetadata ::=
  'REFRESH' 'TABLE' 'METADATA' ( (tableName)? | tableName 'FROM' 'STORAGE' 'UNIT' storageUnitName)?

tableName ::=
  identifier

storageUnitName ::=
  identifier
```

### 补充说明

- 未指定 `tableName` 和 `storageUnitName` 时，默认刷新所有表的元数据

- 刷新元数据需要使用 `DATABASE` 如果未使用 `DATABASE` 则会提示 `No database selected`

### 示例

- 刷新指定存储单于中指定表的元数据

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT su_1;
```

- 刷新指定表的元数据

```sql
REFRESH TABLE METADATA t_order;
```

- 刷新所有表的元数据

```sql
REFRESH TABLE METADATA;
```

### 保留字

`REFRESH`、`TABLE`、`METADATA`、`FROM`、`STORAGE`、`UNIT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)