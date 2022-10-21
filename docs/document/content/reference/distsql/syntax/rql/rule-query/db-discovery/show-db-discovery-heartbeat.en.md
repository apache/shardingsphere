+++
title = "SHOW DB_DISCOVERY HEARTBEATS"
weight = 4
+++

### Description

The `SHOW DB_DISCOVERY HEARTBEATS` syntax is used to query database discovery heartbeats for specified database.

### Syntax

```
ShowDatabaseDiscoveryType::=
  'SHOW' 'DB_DISCOVERY' 'HEARTBEATS' ('FROM' databaseName)?

databaseName ::=
  identifier
```

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
SHOW DB_DISCOVERY HEARTBEATS FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS FROM test1;
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

`SHOW`、`DB_DISCOVERY`、`HEARTBEATS`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
