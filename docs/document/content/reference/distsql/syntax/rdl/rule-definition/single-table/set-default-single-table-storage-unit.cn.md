+++
title = "SET DEFAULT SINGLE TABLE STORAGE UNIT"
weight = 2
+++

## 描述

`SET DEFAULT SINGLE TABLE STORAGE UNIT` 语法用于设置默认的单表存储单元

### 语法定义

```sql
SetDefaultSingleTableStorageUnit ::=
  'SET' 'DEFAULT' 'SINGLE' 'TABLE' 'STORAGE' 'UNIT' singleTableDefinition

singleTableDefinition ::=
  '=' (storageUnitName ｜ 'RANDOM')

storageUnitName ::=
  identifier
```

### 补充说明

- `STORAGE UNIT` 需使用 RDL 管理的存储单元。 `RANDOM` 代表随机储存

### 示例

- 设置默认的单表存储单元

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = su_0;
```

- 设置默认的单表存储单元为随机储存

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM;
```

### 保留字

`SET`、`DEFAULT`、`SINGLE`、`TABLE`、`STORAGE`、`UNIT`、`RANDOM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)