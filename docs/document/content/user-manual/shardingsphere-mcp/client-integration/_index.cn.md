+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把 ShardingSphere-MCP 接入支持 MCP 的 AI 应用、IDE 插件或 Agent 平台。
配置完成后，用户可以在应用中通过自然语言查看元数据、执行受控 SQL 查询，或发起数据库治理任务。

适合使用客户端集成的场景：

- 在 AI 应用、IDE 插件或 Agent 平台中接入 ShardingSphere。
- 基于 ShardingSphere 元数据完成查询辅助、结构理解、问题诊断或治理规划。
- 为团队提供统一的受控数据库访问通路。
- 为 AI 应用集成 ShardingSphere 元数据、受控 SQL 和规则变更能力。

可完成的任务和使用边界见[能力清单](../capabilities/)。

## 典型客户端

- [Codex](./codex/)：适合在 Codex CLI 或 IDE 扩展中使用 ShardingSphere-MCP。
- [Claude Code](./claude-code/)：适合在 Claude Code 项目或用户配置中使用 ShardingSphere-MCP。

## 选择接入方式

- 如果 ShardingSphere-MCP 已经独立启动，或者需要多个应用访问同一个服务，选择 HTTP 接入方式。
- 如果只在本地开发环境使用，并希望 AI 应用在需要时启动 ShardingSphere-MCP，选择 STDIO 接入方式。
- 如果使用 Codex 或 Claude Code，优先参考本章对应子页面。
- 如果使用其他客户端，请按客户端自身文档配置 ShardingSphere-MCP 的地址或启动脚本。

## 集成后的使用方式

AI 应用完成 MCP Server 配置后，用户在对话中直接描述任务。

示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段和索引。
- 执行一条只读查询，并限制返回 100 行。
- 规划一个数据加密或数据脱敏规则，先预览不要执行。

涉及 SQL 执行、规则变更或规则变更计划执行时，应先审查预览内容，再确认执行。
自研集成或协议调试场景见[自研集成附录](../developer-appendix/)。
