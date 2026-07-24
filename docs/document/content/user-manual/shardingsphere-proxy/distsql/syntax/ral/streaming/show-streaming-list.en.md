+++
title = "SHOW STREAMING LIST"
weight = 3
+++

### Description

The `SHOW STREAMING LIST` syntax is used to query the CDC streaming job list.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowStreamingList ::=
  'SHOW' 'STREAMING' 'LIST'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column         | Description                 |
|----------------|-----------------------------|
| id             | CDC streaming job id        |
| database       | database name               |
| tables         | CDC streaming tables        |
| job_item_count | CDC streaming job item count |
| active         | whether the job is active   |
| create_time    | job create time             |
| stop_time      | job stop time               |

### Example

- Query CDC streaming job list

```sql
SHOW STREAMING LIST;
```

```sql
sharding_db=> SHOW STREAMING LIST;
                     id                     |  database   | tables  | job_item_count | active |     create_time     | stop_time
--------------------------------------------+-------------+---------+----------------+--------+---------------------+-----------
 j0302p0000702a83116fcee83f70419ca5e2993791 | sharding_db | t_order | 1              | true   | 2023-10-27 22:01:27 |
(1 row)
```

### Reserved word

`SHOW`, `STREAMING`, `LIST`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
