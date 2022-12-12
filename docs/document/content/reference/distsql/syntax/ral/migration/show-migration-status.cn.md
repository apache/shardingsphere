+++
title = "SHOW MIGRATION STATUS"
weight = 9
+++

### 描述

`SHOW MIGRATION STATUS` 语法用于查询指定数据迁移作业的详细情况

### 语法

```sql
ShowMigrationStatus ::=
  'SHOW' 'MIGRATION' 'STATUS' migrationJobId 

migrationJobId ::=
  string
```

### 补充说明

- `migrationJobId` 需要通过 `SHOW MIGRATION LIST` 语法查询获得

### 返回值说明

| 列                             | 说明               |
|--------------------------------|-------------------|
| item                           | 数据迁移作业分片编号 |
| data source                    | 数据迁移源          |
| status                         | 数据迁移作业状态     |
| processed_records_count        | 处理数据行数        |
| inventory_finished_percentage  | 数据迁移作业完成度   |
| incremental_idle_seconds       | 增量闲置时间        |
| error_message                  | 错误信息提示        |

### 示例

- 查询指定数据迁移作业的详细情况

```sql
SHOW MIGRATION STATUS 'j010180026753ef0e25d3932d94d1673ba551';
```

```sql
mysql> SHOW MIGRATION STATUS 'j010180026753ef0e25d3932d94d1673ba551';
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | su_1        | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           | 25                       |               |
+------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`STATUS`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)