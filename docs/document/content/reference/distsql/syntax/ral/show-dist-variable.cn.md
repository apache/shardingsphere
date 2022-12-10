+++
title = "SHOW DIST VARIABLE"
weight = 5
+++

### 描述

`SHOW DIST VARIABLE` 语法用于查询 `PROXY` 系统变量配置

### 语法

```sql
ShowDistVariable ::=
  'SHOW' ('VARIABLES'|'VARIABLE' 'NAME' '=' variableName)

variableName ::=
  identifier
```

### 返回值说明

| 列            | 说明        |
|---------------|------------|
| variable_name | 系统变量名称|
| variable_value| 系统变量值  |

### 补充说明

- 未指定 `vairableName` 时，默认查询所有 `PROXY` 系统变量配置

### 示例

- 查询所有 `PROXY` 系统变量配置

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

- [保留字](/cn/reference/distsql/syntax/reserved-word/)