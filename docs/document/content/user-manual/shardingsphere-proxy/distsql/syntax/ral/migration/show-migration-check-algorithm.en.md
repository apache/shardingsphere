+++
title = "SHOW MIGRATION CHECK ALGORITHM"
weight = 9
+++

### Description

The `SHOW MIGRATION RULE` syntax is used to query migration check algorithm.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowMigrationCheckAlgorithm ::=
  'SHOW' 'MIGRATION' 'CHECK' 'ALGORITHMS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column                   | Description                                |
|--------------------------|--------------------------------------------|
| type                     | migration check algorithm type             |
| supported_database_types | supported database type                    |
| description              | Description of migration check algorithm   |

### Example

- Query migration check algorithm

```sql
SHOW MIGRATION CHECK ALGORITHMS;
```

```sql
mysql> SHOW MIGRATION CHECK ALGORITHMS;
+-------------+--------------------------------------------------------------+----------------------------+
| type        | supported_database_types                                     | description                |
+-------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH | MySQL                                                        | Match CRC32 of records.    |
| DATA_MATCH  | SQL92,MySQL,MariaDB,PostgreSQL,openGauss,Oracle,SQLServer,H2 | Match raw data of records. |
+-------------+--------------------------------------------------------------+----------------------------+
2 rows in set (0.03 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `CHECK`, `ALGORITHMS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
