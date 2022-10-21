+++
title = "SHOW DB_DISCOVERY RULES"
weight = 2
+++

### Description

The `SHOW DB_DISCOVERY RULES` syntax is used to query database discovery rules for specified database.

### Syntax

```
ShowDatabaseDiscoveryRule::=
  'SHOW' 'DB_DISCOVERY' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                   | Description                            |
| ------------------------ | -------------------------------------- |
| group_name               | Database discovery Rule name           |
| data_source_names        | Data source name list                  |
| primary_data_source_name | Primary data source name               |
| discovery_type           | Database discovery service type        |
| discovery_heartbeat      | Database discovery service heartbeat   |



### Example

- Query database discovery rules for specified database.

```sql
SHOW DB_DISCOVERY RULES FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY RULES FROM test1;
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_name | data_source_names | primary_data_source_name | discovery_type                                                                                    | discovery_heartbeat                                             |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_0    | ds_0,ds_1,ds_2    | ds_0                     | {name=group_0_MySQL.MGR, type=MySQL.MGR, props={group-name=558edd3c-02ec-11ea-9bb3-080027e39bd2}} | {name=group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
1 row in set (0.01 sec)

```

- Query database discovery rules for current database.

```sql
SHOW DB_DISCOVERY RULES;
```

```sql
mysql> SHOW DB_DISCOVERY RULES;
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_name | data_source_names | primary_data_source_name | discovery_type                                                                                    | discovery_heartbeat                                             |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
| group_0    | ds_0,ds_1,ds_2    | ds_0                     | {name=group_0_MySQL.MGR, type=MySQL.MGR, props={group-name=558edd3c-02ec-11ea-9bb3-080027e39bd2}} | {name=group_0_heartbeat, props={keep-alive-cron=0/5 * * * * ?}} |
+------------+-------------------+--------------------------+---------------------------------------------------------------------------------------------------+-----------------------------------------------------------------+
1 row in set (0.03 sec)
```

### Reserved word

`SHOW`, `DB_DISCOVERY`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
