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
| `transport.http.bindHost` | HTTP 监听地址，默认值为 `127.0.0.1`。`127.0.0.1`、`localhost`、`::1` 只允许本机访问；`0.0.0.0` 或指定内网 IP 允许对应网络接口访问。 |
| `transport.http.port` | HTTP 监听端口，默认值为 `18088`。 |
| `transport.http.endpointPath` | HTTP 端点路径，默认值为 `/mcp`。 |

## 数据库配置

`runtimeDatabases` 定义 MCP Server 可以连接并对外暴露的数据库。
每个条目的 key 是用户在自然语言任务中引用的数据库名称，通常对应 ShardingSphere-Proxy 暴露的逻辑库。

```yaml
runtimeDatabases:
  "<logic-database>":
    databaseType: MySQL
    jdbcUrl: "jdbc:mysql://<proxy-host>:<proxy-port>/<logic-database>"
    username: "<proxy-username>"
    password: "<proxy-password>"
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| *名称* | *说明* |
| --- | --- |
| `databaseType` (+) | 连接端点的数据库协议或方言类型，例如 `MySQL` 或 `PostgreSQL`。它影响元数据识别和 SQL 能力判断，不表示连接目标一定是真实数据库或 ShardingSphere-Proxy。 |
| `jdbcUrl` (+) | MCP Server 连接运行时数据库的 JDBC URL；使用 ShardingSphere 规则能力时应指向 Proxy 逻辑库。 |
| `username` (+) | 连接运行时数据库的用户名，通常是 ShardingSphere-Proxy 逻辑库用户名。 |
| `password` (?) | 连接运行时数据库的密码。 |
| `driverClassName` (+) | JDBC 驱动类名，例如 MySQL 驱动使用 `com.mysql.cj.jdbc.Driver`。 |

说明：

- (+) 表示必填项。
- (?) 表示可选项。

注意事项：

- 连接 ShardingSphere-Proxy 时，用户看到的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- 连接真实数据库时，用户看到的是该 JDBC 目标的元数据，不代表 ShardingSphere 规则状态。
- 模式、表、视图、索引和序列等元数据依赖目标数据库的 JDBC 元数据；Proxy 和真实数据库的可见结果可能不同。
- 如果目标 JDBC 驱动没有随发行包提供，请把驱动 jar 放入 `plugins/`。

## 连接目标选择

`runtimeDatabases` 可以配置任意可连接的 JDBC URL。用户能看到的数据库对象和可执行的治理任务取决于连接目标。

### 连接 ShardingSphere-Proxy 逻辑库

如果需要使用 ShardingSphere 规则状态、数据加密、数据脱敏或规则变更能力，应连接 ShardingSphere-Proxy 逻辑库。

此时用户看到的是 Proxy 暴露的逻辑库、逻辑表和逻辑列。
Proxy 可见元数据可能不同于底层物理库的完整结构；涉及物理列、索引或规则变更的计划应先审查再执行。

### 连接真实数据库

如果只需要查看普通数据库元数据、搜索对象或执行受控查询，可以连接真实数据库。

此时用户看到的是目标数据库自身元数据，不代表 ShardingSphere 规则状态。
数据加密、数据脱敏等依赖 ShardingSphere 规则的任务不适用于真实数据库连接。

不同连接目标支持的自然语言任务见[能力清单](../capabilities/)。

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
