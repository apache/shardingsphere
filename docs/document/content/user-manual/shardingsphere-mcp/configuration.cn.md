+++
title = "配置说明"
weight = 3
+++

ShardingSphere-MCP 使用 YAML 文件配置传输方式和 MCP Server 可以连接的数据库。
发行包默认读取 `conf/mcp-http.yaml`，也内置 `conf/mcp-stdio.yaml` 和 `conf/mcp-http-docker.yaml`。

## 传输方式

每个 MCP Server 进程必须且只能选择一种传输方式。

HTTP 示例：

```yaml
transport:
  type: HTTP
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

| 配置项                           | 说明                                                                                              |
|-------------------------------|-------------------------------------------------------------------------------------------------|
| `transport.type`              | 传输方式，支持 `HTTP` 和 `STDIO`。                                                                       |
| `transport.http`              | HTTP 传输配置，只在 `transport.type` 为 `HTTP` 时生效。                                                     |
| `transport.http.bindHost`     | HTTP 监听地址，默认值为 `127.0.0.1`。`127.0.0.1`、`localhost`、`::1` 只允许本机访问；`0.0.0.0` 或指定内网 IP 允许对应网络接口访问。 |
| `transport.http.port`         | HTTP 监听端口，默认值为 `18088`。                                                                         |
| `transport.http.endpointPath` | HTTP 端点路径，默认值为 `/mcp`。                                                                          |

### HTTP 会话归属（可选）

如果 ShardingSphere-MCP 部署在受信网关或反向代理后面，可以让网关注入可信请求头，用于把 MCP 会话和外部用户或调用来源关联起来。
该配置不提供认证或授权；认证、授权和请求头注入仍应由外层网关完成。

```yaml
transport:
  type: HTTP
  http:
    sessionAttributionSource:
      subjectHeader: X-ShardingSphere-MCP-Subject
      sourceHeader: X-ShardingSphere-MCP-Source
      attributeHeaderPrefix: X-ShardingSphere-MCP-Attribute-
```

| 配置项                                                             | 说明                       |
|-----------------------------------------------------------------|--------------------------|
| `transport.http.sessionAttributionSource`                       | HTTP 会话归属来源。未配置时不绑定会话归属。 |
| `transport.http.sessionAttributionSource.subjectHeader`         | 表示外部用户、租户或调用主体的请求头名称。    |
| `transport.http.sessionAttributionSource.sourceHeader`          | 表示调用来源的请求头名称。            |
| `transport.http.sessionAttributionSource.attributeHeaderPrefix` | 自定义归属属性的请求头前缀。           |

只有确认客户端不能直接伪造这些请求头时，才应启用该配置。
同一个 MCP 会话的后续 HTTP 请求必须提供一致的 subject、source 和 attributes。

## 数据库配置

`runtimeDatabases` 定义 MCP Server 可以连接并对外暴露的数据库。Server 启动时可以省略或为空，此时不提供依赖数据库的能力。
每个条目的 key 是用户在自然语言任务中引用的数据库名称，通常对应 ShardingSphere-Proxy 暴露的逻辑库。
MCP Server 会从 `jdbcUrl` 解析数据库类型；请使用与该 JDBC URL 匹配的驱动类。

```yaml
runtimeDatabases:
  "logic_db":
    jdbcUrl: "jdbc:mysql://127.0.0.1:3307/logic_db"
    username: "root"
    password: ""
    driverClassName: "com.mysql.cj.jdbc.Driver"
```

| *名称*                  | *说明*                                                                                   |
|-----------------------|----------------------------------------------------------------------------------------|
| `jdbcUrl` (+)         | MCP Server 连接运行时数据库并解析数据库类型的 JDBC URL；使用 ShardingSphere 规则能力时应指向 Proxy 逻辑库。 |
| `username` (+)        | 连接运行时数据库的用户名，通常是 ShardingSphere-Proxy 逻辑库用户名。                                      |
| `password` (?)        | 连接运行时数据库的密码。                                                                       |
| `driverClassName` (+) | JDBC 驱动类名，例如 MySQL 驱动使用 `com.mysql.cj.jdbc.Driver`。                                |

说明：

- (+) 表示必填项。
- (?) 表示可选项。

注意事项：

- 连接 ShardingSphere-Proxy 时，用户看到的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- 数据库直连时，用户看到的是目标数据库自身的元数据，不代表 ShardingSphere 规则状态。
- 模式、表、视图、索引和序列等元数据依赖连接目标的 JDBC 元数据；Proxy 和数据库直连的可见结果可能不同。
- 如果目标 JDBC 驱动没有随发行包提供，请把驱动 jar 放入 `plugins/`。
- `logic_db` 和 `127.0.0.1:3307` 等示例值只用于说明。运行时 YAML 文件会拒绝未替换的尖括号占位符语法。

## 敏感值占位符

规则变更工具可能需要算法密钥、令牌或替换字符等敏感参数。
不要把真实敏感值写入模型可见的工具入参、普通文档、聊天记录或日志。
可以在工具入参的算法属性中传递敏感值占位符对象；MCP Server 只负责规划、预览和生成安全的人工执行包，不会读取或解析真实值。
规划、预览和人工执行包只返回中性占位符或 `******`，不会返回 `secret_ref` 或真实敏感值。

算法属性中的引用对象示例：

```json
{
  "primary_algorithm_properties": {
    "aes-key-value": {
      "secret_ref": "placeholder://secret-value-1"
    }
  }
}
```

注意事项：

- MCP Server 只记录需要人工替换的敏感值槽位，不会从外部系统获取真实敏感值。
- 带敏感值占位符的自动执行会在产生副作用前停止，并返回 `secret_reference_manual_execution_required`。
- 使用人工执行包时，执行人员应在 MCP 和 AI 应用之外的受控环境中把中性占位符替换为真实值，再执行 DistSQL 或 YAML。
- 文档和示例只使用中性占位符，避免在模型上下文中暴露真实密钥、路径或内部系统信息。

## 连接目标选择

`runtimeDatabases` 可以配置任意可连接的 JDBC URL。用户能看到的数据库对象和可执行的治理任务取决于连接目标。

### 连接 ShardingSphere-Proxy 逻辑库

如果需要使用 ShardingSphere 规则状态、数据加密、数据脱敏或规则变更能力，应连接 ShardingSphere-Proxy 逻辑库。

此时用户看到的是 Proxy 暴露的逻辑库、逻辑表和逻辑列。
Proxy 可见元数据可能不同于底层物理库的完整结构；涉及元数据解释或规则变更的计划应先审查再执行。

### 数据库直连

数据库直连指 ShardingSphere-MCP 不经过 ShardingSphere-Proxy，直接连接用户提供的 MySQL、PostgreSQL 等数据库服务。
如果只需要查看已有数据库的元数据、搜索对象或执行受控查询，可以使用数据库直连。

此时用户看到的是目标数据库自身元数据，不代表 ShardingSphere 规则状态。
数据加密、数据脱敏等依赖 ShardingSphere 规则的任务不适用于数据库直连。

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
