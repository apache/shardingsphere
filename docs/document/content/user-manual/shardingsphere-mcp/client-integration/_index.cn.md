+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把 ShardingSphere-MCP 接入支持 MCP 的 AI 客户端、产品入口或平台 API。配置完成后，用户可以在应用中通过自然语言查看元数据、执行受控 SQL 查询，或发起数据库治理任务。

可完成的任务和使用边界见[能力清单](../capabilities/)。

## 客户端接入

- [Codex](./codex/)：适合在 Codex CLI 或 IDE 扩展中接入已经启动的 HTTP MCP Server。
- [Claude Code](./claude-code/)：适合在 Claude Code CLI 中接入已经启动的 HTTP MCP Server，或由 Claude Code 拉起本地 STDIO MCP Server。

## 平台与 API 接入

- [OpenAI Responses API](./openai-responses-api/)：适合在后端应用中通过 OpenAI API 调用远程 MCP Server。
- [ChatGPT Developer Mode](./chatgpt-developer-mode/)：适合在 ChatGPT Web 产品中直接接入远程 MCP Server。
- [Anthropic MCP Connector](./anthropic-mcp-connector/)：适合在 Anthropic Messages API 中直接接入远程 MCP Server。

平台与 API 接入要求使用已受保护且远程可访问的 MCP endpoint。
不要把 ShardingSphere-MCP 内置 HTTP Server 直接暴露给远程平台，因为它不提供认证或授权。
远程平台接入时，应将其放在受信网关或反向代理后面，由外层组件提供 TLS 终止、身份认证、授权策略、网络访问控制和审计日志。
安全边界见[部署说明](../deployment/)和[配置说明](../configuration/)。
`http://127.0.0.1:18088/mcp` 这类本地地址示例只适用于本地客户端集成文档，不能直接复用于 OpenAI 或 Anthropic 平台入口。

## 选择接入入口

- 如果主要在本地开发或编码助手中使用，优先选择客户端接入。
- 如果需要在后端服务中通过 API 调模型并调用 MCP，优先选择平台与 API 接入。
- 如果需要多个客户端共享同一个 ShardingSphere-MCP 服务，优先准备独立启动的 HTTP MCP Server。
- 如果只在本地开发环境使用，并希望客户端在需要时拉起 MCP 进程，可选择支持 STDIO 的客户端。
- 如果目标入口位于 OpenAI 或 Anthropic 平台侧，优先确认已受保护的远程 MCP 地址是否可从平台访问，再继续后续接入步骤。

## 集成后的使用方式

完成配置后，用户可以在对话中直接描述任务，例如：

- 查看 `logic_db` 中有哪些表。
- 查询 `orders` 的字段和索引。
- 执行一条只读查询，并限制返回 100 行。
- 调用 `database_gateway_validate_runtime_database` 对已经配置的 runtime database 进行接入前校验。
- 规划一个数据加密或数据脱敏规则，先预览不要执行。

涉及 SQL 执行、规则变更或规则变更计划执行时，应先审查预览内容，再确认执行。自研集成或协议调试场景见[自研集成附录](../developer-appendix/)。
