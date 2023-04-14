+++
title = "SHOW MIGRATION LIST"
weight = 7
+++

### Description

The `SHOW MIGRATION LIST` syntax is used to query migration job list.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowMigrationList ::=
  'SHOW' 'MIGRATION' 'LIST'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Values Description

| Columns        | Description                    |
|----------------|--------------------------------|
| id             | migration job id               |
| tables         | migration tables               |
| job_item_count | migration job sharding number  |
| active         | migration job states           |
| create_time    | migration job create time      |
| stop_time      | migration job stop time        |

### Example

- Query migration job list

```sql
SHOW MIGRATION LIST;
```

```sql
mysql> SHOW MIGRATION LIST;
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
| id                                    | tables  | job_item_count | active | create_time         | stop_time           |
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
| j01013a38b0184e07c864627b5bb05da09ee0 | t_order | 1              | false  | 2022-10-31 18:18:24 | 2022-10-31 18:18:31 |
+---------------------------------------+---------+----------------+--------+---------------------+---------------------+
1 row in set (0.28 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `LIST`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
