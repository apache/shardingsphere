+++
title = "SHOW DIST VARIABLE"
weight = 4
+++

### Description

The `SHOW DIST VARIABLE` syntax is used to query `PROXY` system variables configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowDistVariable ::=
  'SHOW' ('VARIABLES' | 'VARIABLE' 'WHERE' 'NAME' '=' variableName)

variableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns        | Description           |
|----------------|-----------------------|
| variable_name  | system variable name  |
| variable_value | system variable value |

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
| system_log_level                      | INFO           |
| kernel_executor_size                  | 0              |
| max_connections_size_per_query        | 1              |
| check_table_meta_data_enabled         | false          |
| sql_federation_type                   | NONE           |
| proxy_frontend_database_protocol_type |                |
| proxy_frontend_flush_threshold        | 128            |
| proxy_backend_query_fetch_size        | -1             |
| proxy_frontend_executor_size          | 0              |
| proxy_backend_executor_suitable       | OLAP           |
| proxy_frontend_max_connections        | 0              |
| proxy_mysql_default_version           | 5.7.22         |
| proxy_default_port                    | 3307           |
| proxy_netty_backlog                   | 1024           |
| proxy_instance_type                   | Proxy          |
| cdc_server_port                       | 33071          |
| proxy_meta_data_collector_enabled     | true           |
| agent_plugins_enabled                 | true           |
| cached_connections                    | 0              |
| transaction_type                      | LOCAL          |
| sql_show                              | false          |
| sql_simple                            | false          |
+---------------------------------------+----------------+
23 rows in set (0.01 sec)
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

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
