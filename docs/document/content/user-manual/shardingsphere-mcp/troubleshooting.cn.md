+++
title = "常见问题"
weight = 6
+++

这里汇总 MCP Server、传输方式、会话、SQL 工具和工作流机制的常见问题。
插件业务问题请查看对应功能插件文档。

## 启动失败

优先检查：

- JDK 版本是否为 JDK 21。
- 配置文件路径是否正确。
- `conf/mcp-http.yaml` 或 `conf/mcp-stdio.yaml` 是否存在。
- YAML 是否包含不受支持字段。
- `username`、`password`、`driverClassName` 是否显式写出；不需要值时写空字符串 `""`。

启动失败时，先查看终端错误和 `logs/mcp.log`。

## HTTP 连接失败

检查：

- 默认端点是否为 `http://127.0.0.1:18088/mcp`。
- 端口是否被占用。
- `transport.type` 是否为 `STREAMABLE_HTTP`。
- `transport.http.endpointPath` 是否和客户端 URL 一致。
- 绑定 `127.0.0.1` 时，远程机器无法直接访问。
- 绑定 `0.0.0.0` 时，应由外层网关处理鉴权和网络访问控制。

## HTTP 返回 403

Origin 校验规则：

- 回环地址绑定下，如果请求带 `Origin`，该 Origin 也必须是回环地址。
- 非回环地址绑定下，缺失 `Origin` 的非浏览器请求会被接受。
- 非回环地址绑定下，任何显式 `Origin` 都会被拒绝。

可选处理方式：

- 调整客户端的 Origin 策略。
- 使用本机回环地址访问。
- 通过受控网关转发请求。

## 会话或协议头问题

HTTP 客户端调用 `initialize` 后，必须保存：

- `MCP-Session-Id`
- `MCP-Protocol-Version`

后续请求都要携带这两个响应头。
会话关闭后不能继续复用。
工作流的 `plan_id` 也只在当前会话内有效。

## STDIO 模式没有响应

STDIO 是给 MCP 客户端拉起子进程使用的，不是人工交互 shell。

检查：

- 客户端配置里的 `command` 是否指向 `bin/start.sh` 或 `bin\start.bat`。
- `args` 是否包含 `conf/mcp-stdio.yaml`。
- stdout 是否被日志污染。
- 诊断信息应查看 stderr 或 `logs/mcp.log`。

## 找不到逻辑库或元数据为空

检查：

- `runtimeDatabases` 是否配置了目标逻辑库。
- MCP 暴露的是 ShardingSphere 逻辑库，不是底层物理存储单元。
- `databaseType` 和 `jdbcUrl` 是否匹配目标逻辑库。
- 目标 JDBC 驱动 jar 是否位于 `plugins/`。
- 连接用户是否有读取 JDBC 元数据的权限。

## JDBC 驱动错误

发行包只内置有限的 JDBC 驱动。
如果目标数据库驱动缺失，请把对应 jar 放入 `plugins/`。

如果以嵌入方式使用 `shardingsphere-mcp-bootstrap`，需要把驱动加到运行时类路径。

`driverClassName` 字段必须显式存在。
当驱动可自动注册且不需要显式覆盖时，写 `""`。

## SQL 工具调用失败

`database_gateway_execute_query` 只用于：

- `SELECT`
- `EXPLAIN ANALYZE`

DML、DDL、DCL、事务控制、savepoint 和其他有副作用 SQL 应使用 `database_gateway_execute_update`。

有副作用 SQL 建议先使用：

```json
{"execution_mode":"preview"}
```

再在确认后使用：

```json
{"execution_mode":"execute"}
```

其他限制：

- 多语句会被拒绝。
- `max_rows` 范围是 `0..5000`。
- `timeout_ms` 范围是 `0..300000`。

## 工作流通用问题

检查：

- 是否丢失 `plan_id`。
- 是否换了 `MCP-Session-Id` 后继续执行或校验。
- `database_gateway_apply_workflow` 是否传入 `execution_mode`。
- 使用 `manual-only` 后，是否已经人工执行返回的 SQL 或 DistSQL。
- `approved_steps` 是否来自预览结果返回的 `preview_artifacts[].approval_step`。

## 收集诊断信息

报告问题时建议提供：

- 启动命令。
- MCP 配置文件，注意移除密码和密钥。
- 传输方式和端点。
- `MCP-Session-Id` 是否已初始化，注意不要公开真实敏感响应头。
- 工具或资源请求体。
- JSON-RPC 错误负载。
- `logs/mcp.log` 中相关错误。
