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

| Columns            | Description                   |
|--------------------|-------------------------------|
| id                 | migration job id              |
| tables             | migration tables              |
| job_item_count     | migration job sharding number |
| active             | migration job states          |
| create_time        | migration job create time     |
| stop_time          | migration job stop time       |
| job_sharding_nodes | migration job sharding nodes  |

### Example

- Query migration job list

```sql
SHOW MIGRATION LIST;
```

```sql
mysql> SHOW MIGRATION LIST;
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
| id                                         | tables              | active | create_time         | stop_time | job_item_count | job_sharding_nodes |
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
| j0102p00001d029afca1fd960d567fed6cddc9b4a2 | source_ds.t_order   | true   | 2022-10-31 18:18:24 |           | 1              | 10.7.5.76@-@27808  |
+--------------------------------------------+---------------------+--------+---------------------+-----------+----------------+--------------------+
4 rows in set (0.06 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `LIST`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
