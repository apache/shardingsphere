+++
title = "SHOW SHARDING TABLE RULES USED AUDITOR"
weight = 13

+++

### Description

`SHOW SHARDING TABLE RULES USED ALGORITHM` syntax is used to query sharding rules used specified sharding auditor in specified logical database

### Syntax

```
ShowShardingTableRulesUsedAuditor::=
  'SHOW' 'SHARDING' 'TABLE' 'RULES' 'USED' 'AUDITOR' AuditortorName ('FROM' databaseName)?

AuditortorName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns     | Descriptions       |
| ------------| -------------------|
| type        | Sharding rule type |
| name        | Sharding rule name |

### Example

- Query sharding table rules for the specified sharding auditor in spicified logical database

```sql
SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor FROM sharding_db;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.00 sec)
```

- Query sharding table rules for specified sharding auditor in the current logical database

```sql
SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`、`SHARDING`、`TABLE`、`RULES`、`USED`、`AUDITOR`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
