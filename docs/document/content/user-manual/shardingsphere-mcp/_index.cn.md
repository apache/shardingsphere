+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP 是 Apache ShardingSphere 的 MCP Server，可以独立启动。
它把 ShardingSphere 逻辑库接入支持 MCP 的客户端，使用户能够通过自然语言查看元数据、执行受控 SQL 查询，并规划可审查的治理变更。

ShardingSphere-MCP 的配置以数据库为核心：先配置 MCP Server 可以连接的 ShardingSphere 逻辑库，再在客户端中描述要完成的数据库任务。

## 通过自然语言使用 MCP

ShardingSphere-MCP 面向支持 MCP 的客户端、IDE 插件和 Agent 平台使用。
完成客户端集成后，用户可以在客户端对话中直接描述数据库任务。

常见任务示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段、索引和表结构。
- 检查 `<table-name>` 当前是否已有加密或脱敏规则。
- 为 `<table-name>.<column-name>` 规划脱敏规则，先预览不要执行。
- 确认刚才的治理变更计划，并校验执行结果。

有副作用的任务应先生成或预览计划，由用户审查变更内容后再执行。

## 文档结构

- 快速开始：构建发行包，配置一个可连接的逻辑库，启动 HTTP MCP Server，并验证基础任务。
- 功能介绍：说明 MCP Server 可以完成的数据库任务、可读取的信息和使用边界。
- 配置说明：说明传输方式、`runtimeDatabases`、插件目录和启动参数。
- 客户端集成：说明如何通过 HTTP 或 STDIO 把 MCP Server 接入客户端，以及集成后的使用方式。
- 功能插件：说明官方 MCP 功能插件能力，以及插件变更的审查、执行和校验流程。
- 部署说明：说明发行包、OCI 镜像和安全部署建议。
- 常见问题：排查 MCP Server、连接、配置、元数据和 SQL 执行的通用问题。
