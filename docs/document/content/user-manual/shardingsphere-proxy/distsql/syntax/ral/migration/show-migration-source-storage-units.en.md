+++
title = "SHOW MIGRATION SOURCE STORAGE UNITS"
weight = 5
+++

### Description

The `SHOW MIGRATION SOURCE STORAGE UNITS` syntax is used to query the registered migration source storage units

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowStorageUnit ::=
  'SHOW' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNITS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column    | Description            |
|-----------|------------------------|
| name      | Storage unit name      |
| type      | Storage unit type      |
| host      | Storage unit host      |
| port      | Storage unit port      |
| db        | Database name          |
| attribute | Storage unit attribute |

 ### Example

- Query registered migration source storage units

```sql
SHOW MIGRATION SOURCE STORAGE UNITS;
```

```sql
mysql> SHOW MIGRATION SOURCE STORAGE UNITS;
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
| name | type  | host      | port | db             | connection_timeout_milliseconds | idle_timeout_milliseconds | max_lifetime_milliseconds | max_pool_size | min_pool_size | read_only | other_attributes |
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
| ds_1 | MySQL | 127.0.0.1 | 3306 | migration_ds_0 |                                 |                           |                           |               |               |           |                  |
+------+-------+-----------+------+----------------+---------------------------------+---------------------------+---------------------------+---------------+---------------+-----------+------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `SOURCE`, `STORAGE`, `UNITS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)