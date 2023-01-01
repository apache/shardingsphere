+++
title = "SHOW DB_DISCOVERY HEARTBEATS"
weight = 4
+++

### Description

The `SHOW DB_DISCOVERY HEARTBEATS` syntax is used to query database discovery heartbeats for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowDatabaseDiscoveryType::=
  'SHOW' 'DB_DISCOVERY' 'HEARTBEATS' ('FROM' databaseName)?

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

### Return value description

| Column                   | Description                             |
| ------------------------ | ----------------------------------------|
| name                     | Database discovery heartbeat name       |
| props                    | Database discovery heartbeat properties |




### Example

- Query database discovery heartbeats for specified database.

```sql
SHOW DB_DISCOVERY HEARTBEATS FROM discovery_db;
```

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS FROM discovery_db;
+-------------------+---------------------------------+
| name              | props                           |
+-------------------+---------------------------------+
| group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+-------------------+---------------------------------+
1 row in set (0.00 sec)
```

- Query database discovery heartbeats for current database.

```sql
SHOW DB_DISCOVERY HEARTBEATS;
```

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS;
+-------------------+---------------------------------+
| name              | props                           |
+-------------------+---------------------------------+
| group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+-------------------+---------------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `DB_DISCOVERY`, `HEARTBEATS`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
