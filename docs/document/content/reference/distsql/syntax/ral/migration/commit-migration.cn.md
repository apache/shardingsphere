+++
title = "COMMIT MIGRATION"
weight = 18
+++

### 描述

`COMMIT MIGRATION` 语法用于完成指定的数据迁移作业

### 语法

```sql
CommitMigration ::=
  'COMMIT' 'MIGRATION'  migrationJobId 

migrationJobId ::=
  string
```

### 补充说明

- `migrationJobId` 需要通过 [SHOW MIGRATION LIST](/cn/reference/distsql/syntax/ral/migration/show-migration-list/) 语法查询获得

### 示例

- 完成指定的数据迁移作业

```sql
COMMIT MIGRATION 'j010180026753ef0e25d3932d94d1673ba551';
```

### 保留字

`COMMIT`、`MIGRATION`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/cn/reference/distsql/syntax/ral/migration/show-migration-list/)