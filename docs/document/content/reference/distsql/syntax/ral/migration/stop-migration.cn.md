+++
title = "STOP MIGRATION"
weight = 15
+++

### 描述

`STOP MIGRATION` 语法用于停止指定的数据迁移作业

### 语法

```sql
StopMigration ::=
  'STOP' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```

### 补充说明

- `migrationJobId` 需要通过 `SHOW MIGRATION LIST` 语法查询获得

### 示例

- 停止指定的数据迁移作业

```sql
STOP MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### 保留字

`STOP`、`MIGRATION`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)