+++
title = "SHOW SHARDING TABLE REFERENCE RULES"
weight = 14
+++

### Description

`SHOW SHARDING TABLE REFERENCE RULES` syntax is used to query sharding tables with reference relationships in the specified logical database.

### Syntax

```sql
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'TABLE' 'REFERENCE' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, No database selected will be prompted.

### Return value description

| Columns                 | Descriptions                       |
| ------------------------|------------------------------------|
| name                    | Sharding table reference rule name |
| sharding_table_reference| sharding table reference           |

### Example

- Query sharding table reference rules for the specified logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES FROM test1;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES FROM test1;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

- Query sharding table reference rules for the current logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `REFERENCE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
