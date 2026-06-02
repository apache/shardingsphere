+++
title = "常见问题"
weight = 7
+++

本页按现象整理 MCP Server、传输方式、会话、SQL 工具和工作流机制的排查方法。
功能插件的业务规则问题请查看对应功能插件文档。

## 排查索引

| 现象 | 可能原因 | 处理方式 | 是否需要代码改进 |
| --- | --- | --- | --- |
| 启动失败 | JDK、配置路径、YAML 字段或必填字段不正确。 | 查看终端错误和 `logs/mcp.log`。 | 通常不需要。 |
| HTTP 无法连接 | 端口、端点路径、传输方式或绑定地址不正确。 | 检查 `port`、`endpointPath`、`bindHost` 和客户端 URL。 | 通常不需要。 |
| HTTP 返回 403 | 请求 `Origin` 与绑定地址安全策略不匹配。 | 本机调试用回环地址；远程访问走受控网关；详细原因看服务端日志。 | 通常不需要。 |
| 会话请求失败 | 未初始化、缺少会话头，或复用已关闭会话。 | 先调用 `initialize`，后续请求持续携带响应头。 | 通常不需要。 |
| STDIO 没有响应 | 被当成人工交互 Shell，或客户端未按 MCP stdio 协议发送 JSON-RPC。 | 由 MCP 客户端拉起进程；诊断信息看 stderr 或日志。 | 通常不需要。 |
| 逻辑库或元数据为空 | 未配置逻辑库、逻辑库名称不正确、连接失败、权限不足，或目标范围确实为空。 | 先读 `shardingsphere://runtime`，再看资源返回的 `empty_state` 和 `recovery`。 | 通常不需要。 |
| JDBC 驱动错误 | 驱动不在类路径，或 `driverClassName` 不正确。 | 把驱动 jar 放入 `plugins/`，并确认 `driverClassName` 非空且类名正确。 | 通常不需要。 |
| SQL 工具调用失败 | 工具选错、多语句被拒绝或参数超限。 | 查询用 `execute_query`；有副作用 SQL 用 `execute_update` 并先预览。 | 通常不需要；错误消息可增强。 |
| 工作流失败 | `plan_id`、会话、执行模式或人工执行步骤不正确。 | 同一会话内复用 `plan_id`；先预览；人工执行后再校验。 | 通常不需要。 |
| 敏感输入无法传递 | 补问要求密钥或凭证。 | 由客户端或运维侧取值，再通过受保护 MCP 调用传入。 | 如需服务端解析密钥引用，需要改代码。 |

补充说明：

- `username` 和 `driverClassName` 必须显式写出且不能为空；无密码账号可以省略 `password` 或写 `""`。
- `MCP-Session-Id` 和 `MCP-Protocol-Version` 来自 `initialize` 响应头，关闭会话后不能复用。
- 使用 `manual-only` 后，应先人工执行返回的 SQL 或 DistSQL，再调用校验工具。
- 人工执行包中的密钥占位符应由执行人员在受控环境替换。

## 连接错误分类

当运行时数据库或 ShardingSphere-Proxy 连接失败时，MCP 响应会返回连接错误分类，用于帮助定位问题。分类只描述失败原因，不暴露 JDBC URL、密码、环境变量或堆栈信息。

| 分类 | 含义 |
| --- | --- |
| `missing_jdbc_driver` | 未找到配置的 JDBC 驱动。 |
| `authentication_failed` | 用户名或密码认证失败。 |
| `authorization_failed` | 当前账号没有访问目标数据库或元数据的权限。 |
| `connection_timeout` | 连接超时，通常需要检查地址、端口、网络或超时设置。 |
| `invalid_configuration` | 运行时数据库配置不完整或不一致。 |
| `database_unavailable` | 目标数据库或 ShardingSphere-Proxy 当前不可用。 |
| `connection_failed` | 连接失败，但无法归类为更具体的原因。 |
| `database_not_visible` | 指定逻辑库对当前连接不可见。 |

## SQL 工具选择

| SQL 类型 | 工具 | 建议 |
| --- | --- | --- |
| `SELECT` | `database_gateway_execute_query` | 用于只读查询。 |
| `EXPLAIN ANALYZE` | `database_gateway_execute_query` | 仅在目标逻辑库能力允许时使用。 |
| DML、DDL、DCL、事务控制、savepoint | `database_gateway_execute_update` | 先用 `execution_mode=preview` 查看副作用，再决定是否执行。 |

`database_gateway_execute_update` 的预览参数：

```json
{"execution_mode":"preview"}
```

确认后执行：

```json
{"execution_mode":"execute"}
```

## 诊断信息

报告问题时建议提供：

- 启动命令。
- MCP 配置文件，注意移除密码、密钥和令牌。
- 传输方式和端点。
- 是否已完成 `initialize`，不要公开真实 `MCP-Session-Id`。
- 工具或资源请求体。
- JSON-RPC 错误负载。
- `logs/mcp.log` 中相关错误。
