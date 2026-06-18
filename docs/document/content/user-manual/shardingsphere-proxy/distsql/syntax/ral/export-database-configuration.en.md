+++
title = "EXPORT DATABASE CONFIGURATION"
weight = 13
+++

### Description

The `EXPORT DATABASE CONFIGURATION` syntax is used to export storage units and rule configurations to `YAML` format.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ExportDatabaseConfiguration ::=
  'EXPORT' 'DATABASE' 'CONFIGURATION' ('FROM' databaseName)? ('TO' 'FILE' filePath)?

databaseName ::=
  identifier

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the currently used logical database will be exported; if no database is used, `No database selected` will be prompted;
- When `filePath` is not specified, the exported information will be output through the result set;
- When `filePath` is specified, the file will be automatically created. If the file already exists, it will be overwritten.

### Example

- Export currently used logical database

```sql
mysql> EXPORT DATABASE CONFIGURATION;
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| result                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| databaseName: sharding_db
dataSources:
  ds_1:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/db0
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
  ds_2:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/db1
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 50
rules:
 |
+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.01 sec)
```

- Export the specified logical database and output it to file

```sql
mysql> EXPORT DATABASE CONFIGURATION FROM sharding_db TO FILE '/xxx/config_sharding_db.yaml';
+-------------------------------------------------------------------------+
| result                                                                  |
+-------------------------------------------------------------------------+
| Successfully exported to: '/xxx/config_sharding_db.yaml'  |
+-------------------------------------------------------------------------+
1 row in set (0.02 sec)
```

### Reserved word

`EXPORT`, `DATABASE`, `CONFIGURATION`, `FROM`, `TO`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
