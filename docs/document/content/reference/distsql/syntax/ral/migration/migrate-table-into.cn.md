+++
title = "MIGRATE TABLE INTO"
weight = 7
+++

### 描述

`MIGRATE TABLE INTO` 语法用于将表从源端迁移到目标端。

### 语法

```sql
MigrateTableInto ::=
  'MIGRATE' 'TABLE' migrationSource '.' tableName 'INTO' (databaseName '.')?tableName

migrationSource ::=
  identifier

databaseName ::=
  identifier

tableName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 将表从源端迁移到当前逻辑库

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

- 将表从源端迁移到指定逻辑库
```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

### 保留字

`MIGRATE`、`TABLE`、`INTO`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)