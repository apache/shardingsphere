+++
title = "SHOW MIGRATION CHECK STATUS"
weight = 11
+++

### 描述

`SHOW MIGRATION CHECK STATUS` 语法用于查询指定数据迁移作业的数据校验情况。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMigrationCheckStatus ::=
  'SHOW' 'MIGRATION' 'CHECK' 'STATUS' migrationJobId 

migrationJobId ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `migrationJobId` 需要通过 [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/) 语法查询获得

### 返回值说明

| 列                   | 说明     |
|---------------------|--------|
| tables              | 校验表    |
| result              | 校验结果   |
| finished_percentage | 校验完成度  |
| remaining_seconds   | 剩余时间   |
| check_begin_time    | 校验开始时间 |
| check_end_time      | 校验结束时间 |
| error_message       | 错误信息提示 |

### 示例

- 查询指定数据迁移作业的数据校验情况

```sql
SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
```

```sql
mysql> SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| t_order | true   | 100                 | 0                 | 2022-11-01 17:57:39.940 | 2022-11-01 17:57:40.587 | 0                |               |
+---------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`CHECK`、`STATUS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)