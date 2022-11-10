+++
title = "UNREGISTER MIGRATION SOURCE STORAGE UNIT"
weight = 5
+++

### 描述

`UNREGISTER MIGRATION SOURCE STORAGE UNIT` 语法用于从当前逻辑库中移除存储单元。

### 语法

```sql
UnregisterMigrationSourceStorageUnit ::=
  'UNREGISTER' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNIT' storageUnitName  ( ',' storageUnitName )* 

storageUnitName ::=
  identifier
```

### 补充说明

- `UNREGISTER MIGRATION SOURCE STORAGE UNIT` 只会移除 Proxy 中的存储单元，不会删除与存储单元对应的真实数据源；

### 示例

- 移除数据迁移源存储单元

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT su_0;
```

- 移除多个数据迁移源存储单元
```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT su_0, su_1;
```

### 保留字

`UNREGISTER`、`MIGRATION`、`SOURCE`、`STORAGE`、`UNIT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)