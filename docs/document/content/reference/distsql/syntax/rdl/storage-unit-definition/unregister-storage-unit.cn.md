+++
title = "UNREGISTER STORAGE UNIT"
weight = 4
+++

### 描述

`UNREGISTER STORAGE UNIT` 语法用于从当前逻辑库中移除存储单元。

### 语法

```sql
UnregisterStorageUnit ::=
  'DROP' 'STORAGE' 'UNIT' ( 'IF' 'EXISTS' )? storageUnitName  ( ',' storageUnitName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?

storageUnitName ::=
  identifier
```

### 补充说明

- `UNREGISTER STORAGE UNIT` 只会移除 Proxy 中的存储单元，不会删除与存储单元对应的真实数据源；
- 无法移除已经被规则使用的存储单元。移除被规则使用的存储单元时会提示 `Storage unit are still in used`；
- 将要移除的存储单元中仅包含 `SINGLE TABLE RULE`，且用户确认可以忽略该限制时，可添加 `IGNORE SINGLE TABLES` 关键字移除存储单元。

### 示例

- 移除存储单元

```sql
UNREGISTER STORAGE UNIT su_0;
```

- 移除多个存储单元

```sql
UNREGISTER STORAGE UNIT su_1, su_2;
```

- 忽略单表移除存储单元

```sql
UNREGISTER STORAGE UNIT su_3 IGNORE SINGLE TABLES;
```

- 如果存储单元存在则移除

```sql
UNREGISTER STORAGE UNIT IF EXISTS su_4;
```

### 保留字

`DROP`、`STORAGE`、`UNIT`、`IF`、`EXISTS`、`IGNORE`、`SINGLE`、`TABLES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)