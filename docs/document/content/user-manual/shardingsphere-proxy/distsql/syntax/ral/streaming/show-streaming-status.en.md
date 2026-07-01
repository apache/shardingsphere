+++
title = "SHOW STREAMING STATUS"
weight = 4
+++

### Description

The `SHOW STREAMING STATUS` syntax is used to query the status of a specified CDC streaming job.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowStreamingStatus ::=
  'SHOW' 'STREAMING' 'STATUS' jobId

jobId ::=
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `jobId` needs to be obtained through [SHOW STREAMING LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/).

### Return Value Description

| Column                        | Description                       |
|-------------------------------|-----------------------------------|
| item                          | CDC streaming job item            |
| data_source                   | data source                       |
| status                        | CDC streaming job status          |
| active                        | whether the job item is active    |
| processed_records_count       | number of processed records       |
| inventory_finished_percentage | inventory finished percentage     |
| incremental_idle_seconds      | incremental idle time in seconds  |
| confirmed_position            | confirmed incremental position    |
| current_position              | current incremental position      |
| error_message                 | error message                     |

### Example

- Query CDC streaming job status

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

### Reserved word

`SHOW`, `STREAMING`, `STATUS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW STREAMING LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/)
