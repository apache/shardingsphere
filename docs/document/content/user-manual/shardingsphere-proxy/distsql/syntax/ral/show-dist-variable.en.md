+++
title = "SHOW DIST VARIABLE"
weight = 8
+++

### Description

The `SHOW DIST VARIABLE` syntax is used to query `PROXY` system variables configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowDistVariable ::=
  'SHOW' 'DIST' ('VARIABLES' ('LIKE' likePattern)?| 'VARIABLE' 'WHERE' 'NAME' '=' variableName)

likePattern ::=
  string

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

- When `variableName` is not specified, all persistent `PROXY` system variables and the cached connection count are queried by default.

### Example

- Query all system variables configuration of `PROXY`

```sql
SHOW DIST VARIABLES;
```

```sql
mysql> SHOW DIST VARIABLES;
+-------------------------------------------------------+-----------------+
| variable_name                                         | variable_value  |
+-------------------------------------------------------+-----------------+
| agent_plugins_enabled                                 | true            |
| cached_connections                                    | 0               |
| cdc_server_port                                       | 33071           |
| check_table_metadata_enabled                          | false           |
| groovy_inline_expression_parsing_cache_max_size       | 1000            |
| kernel_executor_size                                  | 0               |
| load_table_metadata_batch_size                        | 1000            |
| max_connections_size_per_query                        | 1               |
| max_union_size_per_datasource                         | 2147483647      |
| metadata_identifier_case_sensitivity                  | DATABASE        |
| proxy_backend_query_fetch_size                        | -1              |
| proxy_default_port                                    | 3307            |
| proxy_frontend_connection_idle_timeout                | 28800           |
| proxy_frontend_database_protocol_type                 |                 |
| proxy_frontend_executor_size                          | 0               |
| proxy_frontend_flush_threshold                        | 128             |
| proxy_frontend_max_connections                        | 0               |
| proxy_frontend_ssl_cipher                             |                 |
| proxy_frontend_ssl_enabled                            | false           |
| proxy_frontend_ssl_version                            | TLSv1.2,TLSv1.3 |
| proxy_netty_backlog                                   | 1024            |
| sql_show                                              | false           |
| sql_simple                                            | false           |
+-------------------------------------------------------+-----------------+
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
