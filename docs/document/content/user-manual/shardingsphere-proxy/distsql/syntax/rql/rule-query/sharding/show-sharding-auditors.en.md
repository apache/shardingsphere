+++
title = "SHOW SHARDING AUDITORS"
weight = 7

+++

### Description

`SHOW SHARDING AUDITORS` syntax is used to query sharding auditors in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingAuditors::=
  'SHOW' 'SHARDING' 'AUDITORS' ('FROM' databaseName)?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| column | Description                           |
|--------|---------------------------------------|
| name   | Sharding auditor name                 |
| type   | Sharding auditor algorithm type       |
| props  | Sharding auditor algorithm properties |

### Example

- Query sharding auditors for the specified logical database

```sql
SHOW SHARDING AUDITORS FROM sharding_db;
```

```sql
mysql> SHOW SHARDING AUDITORS FROM sharding_db;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions |       |
+-------------------------------+-------------------------+-------+
1 row in set (0.01 sec)
```

- Query sharding auditors for the current logical database

```sql
SHOW SHARDING AUDITORS;
```

```sql
mysql> SHOW SHARDING AUDITORS;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions |       |
+-------------------------------+-------------------------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `AUDITORS`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

