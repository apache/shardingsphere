+++
title = "SHOW DIST VARIABLE"
weight = 5
+++

### Description

The `SHOW DIST VARIABLE` syntax is used to query `PROXY` system variables configuration.

### Syntax

```sql
ShowDistVariable ::=
  'SHOW' ('VARIABLES'|'VARIABLE' 'WHERE' 'NAME' '=' variableName)

variableName ::=
  identifier
```

### Return Value Description

| Columns       | Description            |
|---------------|------------------------|
| variable_name | system variable name   |
| variable_value| systen variable value  |

### Supplement

- When `variableName` is not specified, the default is query all `PROXY` variables configuration.

### Example

- Query all system variables configuration of `PROXY`

```sql
SHOW DIST VARIABLES;
```

```sql
mysql> SHOW DIST VARIABLES;
+---------------------------------------+----------------+
| variable_name                         | variable_value |
+---------------------------------------+----------------+
| sql_show                              | false          |
| sql_simple                            | false          |
| kernel_executor_size                  | 0              |
| max_connections_size_per_query        | 1              |
| check_table_metadata_enabled          | false          |
| sql_federation_type                   | NONE           |
| proxy_frontend_database_protocol_type |                |
| proxy_frontend_flush_threshold        | 128            |
| proxy_hint_enabled                    | false          |
| proxy_backend_query_fetch_size        | -1             |
| proxy_frontend_executor_size          | 0              |
| proxy_backend_executor_suitable       | OLAP           |
| proxy_frontend_max_connections        | 0              |
| proxy_backend_driver_type             | JDBC           |
| proxy_mysql_default_version           | 5.7.22         |
| proxy_default_port                    | 3307           |
| proxy_netty_backlog                   | 1024           |
| proxy_instance_type                   | Proxy          |
| proxy_metadata_collector_enabled      | false          |
| agent_plugins_enabled                 | true           |
| cached_connections                    | 0              |
| transaction_type                      | LOCAL          |
+---------------------------------------+----------------+
22 rows in set (0.01 sec)
```
- Query specified system variable configuration of `PROXY`

```sql
SHOW DIST VARIABLE WHERE NAME = sql_show;
```

```sql
mysql> SHOW DIST VARIABLE WHERE NAME = sql_show;
+---------------+----------------+
| variable_name | variable_value |
+---------------+----------------+
| sql_show      | false          |
+---------------+----------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `DIST`, `VARIABLE`, `VARIABLES`, `NAME`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
