+++
title = "START MIGRATION CHECK"
weight = 14
+++

### 描述

`START MIGRATION CHECK` 语法用于开始指定数据迁移作业的数据校验

### 语法

```sql
StartMigrationCheck ::=
  'START' 'MIGRATION' 'CHECK' migrationJobId 

migrationJobId ::=
  string
```

### 补充说明

- `migrationJobId` 需要通过 `SHOW MIGRATION LIST` 语法查询获得

### 示例

- 开始指定数据迁移作业的数据校验

```sql
START MIGRATION CHECK 'j010180026753ef0e25d3932d94d1673ba551';
```

### 保留字

`START`、`MIGRATION`、`CHECK`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)