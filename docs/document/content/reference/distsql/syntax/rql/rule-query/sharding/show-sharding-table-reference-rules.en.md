+++
title = "SHOW SHARDING TABLE REFERENCE RULES"
weight = 14
+++

### Description

`SHOW SHARDING TABLE REFERENCE RULES` syntax is used to query sharding tables with reference relationships in the specified logical database.

### Syntax

```sql
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'TABLE' 'REFERENCE' 'RULES'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, No database selected will be prompted.

### Return value description

| Columns                 | Descriptions                  |
| ------------------------| ------------------------------|
| sharding_table_reference| sharding reference table list |

### Example

- Query sharding tables with reference relationships for the spicified logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES FROM test1;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES FROM test1;
+--------------------------+
| sharding_table_reference |
+--------------------------+
| t_order,t_order_item     |
+--------------------------+
1 row in set (0.00 sec)
```

- Query sharding tables with reference relationships for the current logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES;
+--------------------------+
| sharding_table_reference |
+--------------------------+
| t_order,t_order_item     |
+--------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `REFERENCE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
