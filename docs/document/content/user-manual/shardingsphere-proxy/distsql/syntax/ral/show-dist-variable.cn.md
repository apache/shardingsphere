+++
title = "SHOW DIST VARIABLE"
weight = 8
+++

### 描述

`SHOW DIST VARIABLE` 语法用于查询 `PROXY` 系统变量配置。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowDistVariable ::=
  'SHOW' 'DIST' ('VARIABLES' ('LIKE' likePattern)?| 'VARIABLE' 'WHERE' 'NAME' '=' variableName)

likePattern ::=
  string

variableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列              | 说明     |
|----------------|--------|
| variable_name  | 系统变量名称 |
| variable_value | 系统变量值  |

### 补充说明

- 未指定 `vairableName` 时，默认查询所有 `PROXY` 系统变量配置

### 示例

- 查询所有 `PROXY` 系统变量配置

```sql
SHOW DIST VARIABLES;
```

```sql
mysql> SHOW DIST VARIABLES;
+-----------------------------------------+-----------------+
| variable_name                           | variable_value  |
+-----------------------------------------+-----------------+
| agent_plugins_enabled                   | true            |
| cached_connections                      | 0               |
| cdc_server_port                         | 33071           |
| check_table_metadata_enabled            | false           |
| kernel_executor_size                    | 0               |
| load_table_metadata_batch_size          | 1000            |
| max_connections_size_per_query          | 1               |
| proxy_backend_query_fetch_size          | -1              |
| proxy_default_port                      | 3307            |
| proxy_frontend_database_protocol_type   |                 |
| proxy_frontend_executor_size            | 0               |
| proxy_frontend_flush_threshold          | 128             |
| proxy_frontend_max_connections          | 0               |
| proxy_frontend_ssl_cipher               |                 |
| proxy_frontend_ssl_enabled              | false           |
| proxy_frontend_ssl_version              | TLSv1.2,TLSv1.3 |
| proxy_meta_data_collector_enabled       | false           |
| proxy_netty_backlog                     | 1024            |
| sql_show                                | false           |
| sql_simple                              | false           |
| system_schema_metadata_assembly_enabled | true            |
+-----------------------------------------+-----------------+
21 rows in set (0.01 sec)
```

- 查询指定 `PROXY` 系统变量配置

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

### 保留字

`SHOW`、`DIST`、`VARIABLE`、`VARIABLES`、`NAME`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
