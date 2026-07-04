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
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `migrationJobId` 需要通过 [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/) 语法查询获得

### 返回值说明

| 列                             | 说明        |
|-------------------------------|-----------|
| tables                        | 校验表       |
| result                        | 校验结果      |
| check_failed_tables           | 校验失败的表    |
| ignored_tables                | 忽略的表      |
| active                        | 校验作业是否活跃  |
| inventory_finished_percentage | 存量校验完成度   |
| inventory_remaining_seconds   | 存量校验剩余时间  |
| incremental_idle_seconds      | 增量闲置时间    |
| check_begin_time              | 校验开始时间    |
| check_end_time                | 校验结束时间    |
| duration_seconds              | 校验持续时间，单位秒 |
| algorithm_type                | 校验算法类型    |
| algorithm_props               | 校验算法属性    |
| error_message                 | 错误信息提示    |

### 示例

- 查询指定数据迁移作业的数据校验情况

```sql
SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
```

```sql
mysql> SHOW MIGRATION CHECK STATUS 'j010180026753ef0e25d3932d94d1673ba551';
+---------+--------+---------------------+----------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| tables  | result | check_failed_tables | ignored_tables | active | inventory_finished_percentage | inventory_remaining_seconds | incremental_idle_seconds | check_begin_time        | check_end_time          | duration_seconds | algorithm_type | algorithm_props | error_message |
+---------+--------+---------------------+----------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| t_order | true   |                     |                | false  | 100                           | 0                           | 0                        | 2022-11-01 17:57:39.940 | 2022-11-01 17:57:40.587 | 0                | DATA_MATCH     |                 |               |
+---------+--------+---------------------+----------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`CHECK`、`STATUS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW MIGRATION LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/migration/show-migration-list/)
