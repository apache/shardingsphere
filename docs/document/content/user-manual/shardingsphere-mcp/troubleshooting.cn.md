+++
title = "常见问题"
weight = 7
+++

本页按现象整理 MCP Server、传输方式、会话、SQL 工具和工作流机制的排查方法。
功能插件的业务规则问题请查看对应功能插件文档。

## 排查索引

| 现象 | 可能原因 | 处理方式 | 是否需要代码改进 |
| --- | --- | --- | --- |
| 启动失败 | JDK、配置路径、YAML 字段或必填字段不正确。 | 查看终端错误和 `logs/mcp.log`。 | 部分需要：字段可省略能力待优化。 |
| HTTP 无法连接 | 端口、端点路径、传输方式或绑定地址不正确。 | 检查 `port`、`endpointPath`、`bindHost` 和客户端 URL。 | 通常不需要。 |
| HTTP 返回 403 | 请求 `Origin` 与绑定地址安全策略不匹配。 | 本机调试用回环地址；远程访问走受控网关。 | 可以改进：错误响应可给出更明确提示。 |
| 会话请求失败 | 未初始化、缺少会话头，或复用已关闭会话。 | 先调用 `initialize`，后续请求持续携带响应头。 | 通常不需要。 |
| STDIO 没有响应 | 被当成人工交互 Shell，或 stdout 被日志污染。 | 由 MCP 客户端拉起进程；诊断信息看 stderr 或日志。 | 可以改进：继续保护 stdout。 |
| 逻辑库或元数据为空 | 配置、驱动或权限不正确。 | 确认连接 Proxy 逻辑库，并检查驱动和权限。 | 可以改进：空结果可给出诊断。 |
| JDBC 驱动错误 | 驱动不在类路径，或 `driverClassName` 不正确。 | 把驱动 jar 放入 `plugins/`，或加入嵌入式运行时类路径。 | 部分需要：字段可省略能力待优化。 |
| SQL 工具调用失败 | 工具选错、多语句被拒绝或参数超限。 | 查询用 `execute_query`；有副作用 SQL 用 `execute_update` 并先预览。 | 通常不需要；错误消息可增强。 |
| 工作流失败 | `plan_id`、会话、执行模式或人工执行步骤不正确。 | 同一会话内复用 `plan_id`；先预览；人工执行后再校验。 | 通常不需要。 |
| 敏感输入无法传递 | 补问要求密钥或凭证。 | 由客户端或运维侧取值，再通过受保护 MCP 调用传入。 | 如需服务端解析密钥引用，需要改代码。 |

补充说明：

- `username`、`password` 和 `driverClassName` 目前必须显式写出；不需要值时写 `""`。
- `MCP-Session-Id` 和 `MCP-Protocol-Version` 来自 `initialize` 响应头，关闭会话后不能复用。
- 使用 `manual-only` 后，应先人工执行返回的 SQL 或 DistSQL，再调用校验工具。
- 人工执行包中的密钥占位符应由执行人员在受控环境替换。

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
