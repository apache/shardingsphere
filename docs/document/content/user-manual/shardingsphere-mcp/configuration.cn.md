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
| `databaseType` | 是 | 连接端点的数据库协议或方言类型，例如 `MySQL` 或 `PostgreSQL`。它用于选择 JDBC 元数据和能力判断逻辑，不表示连接目标一定是真实数据库或 ShardingSphere-Proxy。 |
| `jdbcUrl` | 是 | MCP Server 连接运行时数据库的 JDBC URL；使用 ShardingSphere 规则能力时应指向 Proxy 逻辑库。 |
| `username` | 是 | 连接运行时数据库的用户名，通常是 ShardingSphere-Proxy 逻辑库用户名。 |
| `password` | 否 | 连接运行时数据库的密码；无密码账号可以省略或写空字符串 `""`。 |
| `driverClassName` | 是 | JDBC 驱动类名，例如 MySQL 驱动使用 `com.mysql.cj.jdbc.Driver`。 |

注意事项：

- 连接 ShardingSphere-Proxy 时，MCP 资源暴露的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- 连接真实数据库时，MCP 资源反映该 JDBC 目标的元数据，不代表 ShardingSphere 规则状态。
- Schema、table、view、index 和 sequence 等元数据依赖目标数据库的 JDBC 元数据；Proxy 和真实数据库的可见结果可能不同。
- 如果目标 JDBC 驱动没有随发行包提供，请把驱动 jar 放入 `plugins/`。

## 连接目标与能力边界

`runtimeDatabases` 当前可以配置任意可用的 JDBC URL。不同连接目标的语义不同，能力边界也不同。

### 连接 ShardingSphere-Proxy 逻辑库

这是使用 ShardingSphere 规则能力时的推荐连接方式。该模式面向 Proxy 暴露的逻辑库和逻辑 SQL 视图，适合使用以下能力：

- 读取 ShardingSphere 逻辑库、逻辑表和逻辑列元数据。
- 查询 Proxy 可见的加密、脱敏算法插件。
- 查询、规划、应用和校验加密或脱敏规则。
- 通过 Proxy 执行逻辑 SQL 和工作流生成的 DistSQL。

该模式受 Proxy 能力限制：

- JDBC 元数据、`information_schema`、索引、sequence 和列类型信息以 Proxy 暴露结果为准，不等同于完整底层物理库元数据。
- 物理列、物理索引和多存储节点一致性不作为 MCP 自动确认的稳定契约。
- 可用 DistSQL、规则类型和算法插件取决于 Proxy 版本、已安装插件和当前账号权限。
- 物理 DDL 产物应先审查；只有 Proxy 能安全路由并执行时才适合自动应用。

### 连接真实数据库

该模式只适合把 MCP 作为通用 JDBC 元数据和 SQL 入口使用，适合以下能力：

- 浏览 database、schema、table、view、column、index 和 sequence 等 JDBC 元数据。
- 搜索元数据。
- 执行通用只读查询，或在明确授权后执行普通 DML、DDL、DCL。

该模式不提供 ShardingSphere 规则能力：

- 不能发现 Proxy 中可见的加密或脱敏算法插件。
- 不能查询、规划、应用或校验 ShardingSphere 加密、脱敏规则。
- 不能使用依赖 DistSQL 的工作流能力；真实数据库通常不识别 ShardingSphere DistSQL。

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
