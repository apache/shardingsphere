+++
title = "配置说明"
weight = 3
+++

ShardingSphere-MCP 使用 YAML 文件配置传输方式和 MCP Server 可以连接的数据库。
发行包默认读取 `conf/mcp-http.yaml`，也内置 `conf/mcp-stdio.yaml`。

## 传输方式

每个 MCP Server 进程必须且只能选择一种传输方式。

HTTP 示例：

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
```

STDIO 示例：

```yaml
transport:
  type: STDIO
```

| 配置项 | 说明 |
| --- | --- |
| `transport.type` | 传输方式，支持 `STREAMABLE_HTTP` 和 `STDIO`。 |
| `transport.http` | HTTP 传输配置，只在 `transport.type` 为 `STREAMABLE_HTTP` 时生效。 |
| `transport.http.bindHost` | HTTP 监听地址，默认值为 `127.0.0.1`。 |
| `transport.http.port` | HTTP 监听端口，默认值为 `18088`。 |
| `transport.http.endpointPath` | HTTP 端点路径，默认值为 `/mcp`。 |

| 监听地址 | 说明 |
| --- | --- |
| `127.0.0.1`、`localhost`、`::1` | 只允许本机访问。 |
| `0.0.0.0` 或指定内网 IP | 允许对应网络接口访问。 |

## 数据库配置

`runtimeDatabases` 定义 MCP Server 可以连接并对外暴露的数据库。
每个条目的 key 是 MCP 调用中使用的数据库名称，通常对应 ShardingSphere-Proxy 暴露的逻辑库。

```yaml
runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `databaseType` | 是 | 数据库类型，例如 `MySQL` 或 `PostgreSQL`。 |
| `jdbcUrl` | 是 | MCP Server 连接逻辑库的 JDBC URL。 |
| `username` | 是 | 连接 ShardingSphere-Proxy 逻辑库的用户名。 |
| `password` | 否 | 连接 ShardingSphere-Proxy 逻辑库的密码；无密码账号可以省略或写空字符串 `""`。 |
| `driverClassName` | 是 | JDBC 驱动类名，例如 MySQL 驱动使用 `com.mysql.cj.jdbc.Driver`。 |

注意事项：

- MCP 资源暴露的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- Schema、table、view、index 和 sequence 等元数据依赖目标数据库的 JDBC 元数据。
- 如果目标 JDBC 驱动没有随发行包提供，请把驱动 jar 放入 `plugins/`。

## 插件目录

发行包默认把 MCP Server 依赖和内置 MCP 功能插件 jar 放入 `lib/`。
如果目标数据库驱动或额外 MCP 功能插件 jar 没有随发行包提供，请放入发行包根目录下的 `plugins/`，再启动 MCP Server。

## 自定义配置文件

Unix-like 系统：

```bash
bin/start.sh /path/to/mcp-http.yaml
```

Windows：

```bat
bin\start.bat path\to\mcp-http.yaml
```

Docker 中可以通过 `SHARDINGSPHERE_MCP_CONFIG` 指定容器内的配置文件路径。
