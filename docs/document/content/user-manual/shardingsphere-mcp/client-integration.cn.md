+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把这条受控数据库访问通路接入桌面客户端、IDE 插件、Agent 平台或自研 LLM 应用。
接入后，大模型不是直接连接数据库，而是通过 MCP 客户端调用 ShardingSphere-MCP 暴露的资源、工具、提示和补全能力。
它不是快速开始中 curl 手工验证流程的替代说明，也不是让用户手工拼 JSON-RPC 的操作手册。
客户端负责连接配置、会话头、补全和调用编排。
用户只需要表达要完成的元数据查询、只读 SQL 查询或数据库治理任务。

适合使用客户端集成的场景：

- 在支持 MCP 的模型客户端、IDE 插件或 Agent 平台中接入 ShardingSphere。
- 让模型基于 ShardingSphere 元数据完成查询辅助、结构理解、问题诊断或治理规划。
- 为团队提供受控数据库访问通路，而不是把数据库连接信息直接交给模型。
- 为自研 Agent 平台集成 ShardingSphere 元数据、安全 SQL 和治理插件能力。

完整的资源、工具、提示和补全说明见[能力清单](../capabilities/)。

## 选择传输方式

- HTTP 适合 MCP Server 独立启动，客户端通过固定端点访问的场景。客户端需要完成会话初始化，并在后续请求中携带会话响应头。
- STDIO 适合本地 MCP 客户端拉起 ShardingSphere-MCP 子进程的场景。客户端负责进程生命周期，stdout 只传输 MCP 协议帧。

## HTTP 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
`url` 指向已经启动的 HTTP MCP Server。

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

HTTP 客户端需要在正常 MCP 调用前完成会话生命周期：

1. 调用 `initialize`。
2. 保存 `MCP-Session-Id` 和 `MCP-Protocol-Version` 响应头。
3. 携带这两个响应头发送 `notifications/initialized`，并预期 HTTP 状态码为 `202`。
4. 后续 MCP 请求继续携带这两个响应头。

关闭会话后，该会话 ID 不能继续复用。

## STDIO 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
`command` 指向发行包内的启动脚本，`args` 指向 STDIO 配置文件。

```json
{
  "mcpServers": {
    "shardingsphere": {
      "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
      "args": ["/path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml"]
    }
  }
}
```

STDIO 模式适合由本地 MCP 客户端拉起 ShardingSphere-MCP 子进程。
它不是面向人工手输请求的交互式 Shell。
将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

STDIO 模式下：

- stdout 只用于 MCP 协议帧。
- 诊断日志写到 stderr 或 `logs/mcp.log`。
- 客户端配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## 集成后的使用方式

客户端完成 MCP Server 配置后，用户在模型对话中直接描述任务。
客户端负责会话初始化、能力发现、补全和工具调用；模型根据任务选择读取资源或调用工具。

示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段和索引。
- 执行一条只读查询，并限制返回 100 行。
- 规划一个数据加密或数据脱敏规则，先预览不要执行。

如果客户端提供工具调用审批界面，应重点审查 SQL 执行、规则变更、插件工作流执行等有副作用的调用。
自研客户端或协议调试场景，可结合[能力清单](../capabilities/)确认资源、工具、提示和补全目标。
