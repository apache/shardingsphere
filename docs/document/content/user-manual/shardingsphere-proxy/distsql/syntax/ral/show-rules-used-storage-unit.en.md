+++
title = "SHOW RULES USED STORAGE UNIT"
weight = 14
+++

### Description

The `SHOW RULES USED STORAGE UNIT` syntax is used to query the rules for using the specified storage unit in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowRulesUsedStorageUnit ::=
  'SHOW' 'RULES' 'USED' 'STORAGE' 'UNIT' storageUnitName ('FROM' databaseName)?

storageUnitName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns     | Description   |
|-------------|---------------|
| type        | rule type     |
| name        | rule name     |

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Query the rules for using the specified storage unit in specified database

```sql
SHOW RULES USED STORAGE UNIT ds_1 FROM sharding_db;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT ds_1 FROM sharding_db;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

- Query the rules for using the specified storage unit in current database

```sql
SHOW RULES USED STORAGE UNIT ds_1;
```

```sql
mysql> SHOW RULES USED STORAGE UNIT ds_1;
+---------------------+------------+
| type                | name       |
+---------------------+------------+
| readwrite_splitting | ms_group_0 |
| readwrite_splitting | ms_group_0 |
+---------------------+------------+
2 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `RULES`, `USED`, `STORAGE`, `UNIT`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
