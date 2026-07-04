+++
title = "SHOW STREAMING STATUS"
weight = 4

+++

### 描述

`SHOW STREAMING STATUS` 语法用于查询指定 CDC Streaming 作业状态。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowStreamingStatus ::=
  'SHOW' 'STREAMING' 'STATUS' jobId

jobId ::=
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `jobId` 需要通过 [SHOW STREAMING LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/) 语法查询获得。

### 返回值说明

| 列                             | 说明                  |
|-------------------------------|---------------------|
| item                          | CDC Streaming 作业项   |
| data_source                   | 数据源                 |
| status                        | CDC Streaming 作业状态  |
| active                        | 作业项是否活跃             |
| processed_records_count       | 已处理记录数              |
| inventory_finished_percentage | 存量任务完成百分比           |
| incremental_idle_seconds      | 增量空闲时间，单位为秒         |
| confirmed_position            | 已确认的增量位置            |
| current_position              | 当前增量位置              |
| error_message                 | 错误信息                |

### 示例

- 查询 CDC Streaming 作业状态

```sql
SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;
```

```sql
sharding_db=> SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;
 item | data_source |          status          | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | confirmed_position | current_position | error_message
------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+--------------------+------------------+---------------
 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | false  | 2                       | 100                           | 115                      | 5/597E43D0         | 5/597E4810       |
 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | false  | 3                       | 100                           | 115                      | 5/597E4450         | 5/597E4810       |
(2 rows)
```

### 保留字

`SHOW`、`STREAMING`、`STATUS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW STREAMING LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/)
