+++
title = "SHOW DEFAULT SINGLE TABLE STORAGE UNIT"
weight = 2
+++

### Description

The `SHOW DEFAULT SINGLE TABLE STORAGE UNIT` syntax is used to query storage units for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowDefaultSingleTableStorageUnit::=
  'SHOW' 'DEFAULT' 'SINGLE' 'TABLE' 'STORAGE' 'UNIT' ('FROM' databaseName)?
  
databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return Value Description

| Column             | Description       |
|--------------------|-------------------|
| storage_unit_name  | Storage unit name |

### Example

- Query storage units for specified database.

```sql
SHOW DEFAULT SINGLE TABLE STORAGE UNIT
``` 

```sql
sql> SHOW DEFAULT SINGLE TABLE STORAGE UNIT;
+-------------------+
| storage_unit_name |
+-------------------+
|  ds_0             |
+-------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `DEFAULT`, `SINGLE`, `TABLE`, `STORAGE`, `UNIT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
