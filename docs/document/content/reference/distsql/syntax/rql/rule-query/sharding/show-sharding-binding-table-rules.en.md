+++
title = "SHOW SHARDING BINDING TABLE RULES"
weight = 14

+++

### Description

`SHOW SHARDING TABLE RULES USED ALGORITHM` syntax is used to query sharding binding table rules for specified logical database

### Syntax

```
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'BINDING' 'TABLE' 'RULES'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns                | Descriptions                |
| -----------------------| ----------------------------|
| sharding_binding_tables| sharding Binding Table list |

### Example

- Query sharding binding table rules for the spicified logical database

```sql
SHOW SHARDING BINDING TABLE RULES FROM test1;
```

```sql
mysql> SHOW SHARDING BINDING TABLE RULES FROM test1;
+-------------------------+
| sharding_binding_tables |
+-------------------------+
| t_order,t_order_item    |
+-------------------------+
1 row in set (0.00 sec)
```

- Query sharding binding table rules for the current logical database

```sql
SHOW SHARDING BINDING TABLE RULES;
```

```sql
mysql> SHOW SHARDING BINDING TABLE RULES;
+-------------------------+
| sharding_binding_tables |
+-------------------------+
| t_order,t_order_item    |
+-------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`、`SHARDING`、`BINDING`、`TABLE`、`RULES`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
