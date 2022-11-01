+++
title = "STOP MIGRATION CHECK"
weight = 13
+++

### 描述

`STOP MIGRATION CHECK` 语法用于停止指定数据迁移作业的数据校验

### 语法

```sql
StopMigrationCheck ::=
  'STOP' 'MIGRATION' 'CHECK' migrationJobId 

migrationJobId ::=
  string
```

### 补充说明

- `migrationJobId` 需要通过 `SHOW MIGRATION LIST` 语法查询获得

### 示例

- 停止指定数据迁移作业的数据校验

```sql
STOP MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### 保留字

`STOP`、`MIGRATION`、`CHECK`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)