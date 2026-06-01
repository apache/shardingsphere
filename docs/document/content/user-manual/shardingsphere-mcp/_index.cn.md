+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP 是 Apache ShardingSphere 的 MCP Server，可以独立启动，并向 MCP 客户端暴露 ShardingSphere 逻辑库的元数据、安全 SQL 访问能力和插件工作流。
ShardingSphere-MCP 为大模型和 Agent 提供一条受控访问 ShardingSphere 逻辑库的通路。
模型通过 MCP 客户端主动发现数据库结构、读取治理状态，并在权限边界内调用 SQL 工具或生成可审查的治理变更计划；数据库连接、会话和执行边界由 MCP Server 管理。

ShardingSphere-MCP 的配置以数据库为核心：先配置 MCP Server 可以连接的 ShardingSphere 逻辑库，再通过 MCP 客户端读取元数据或调用 SQL 工具。

## 通过自然语言使用 MCP

ShardingSphere-MCP 面向支持 MCP 的模型客户端、IDE 插件和 Agent 平台使用。
完成客户端集成后，用户在模型对话中描述数据库任务，模型会根据任务主动读取资源、调用工具、请求补全或生成插件工作流计划。
普通用户不需要手工拼接 JSON-RPC 请求、记住资源 URI，或直接选择底层工具。

常见任务示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段、索引和表结构。
- 检查 `<table-name>` 当前是否已有加密或脱敏规则。
- 为 `<table-name>.<column-name>` 规划脱敏规则，先预览不要执行。
- 确认刚才的治理变更计划，并校验执行结果。

有副作用的任务应先生成或预览计划，由用户审查变更内容后再执行。

## 文档结构

- 快速开始：构建发行包，配置一个可连接的逻辑库，启动 HTTP MCP Server，并验证元数据读取和只读 SQL 查询。
- 功能介绍：说明 MCP Server 对外提供的资源、工具、提示、补全和工作流能力。
- 配置说明：说明传输方式、`runtimeDatabases`、插件目录和启动参数。
- 客户端集成：说明 HTTP、STDIO、会话响应头和能力发现调用方式。
- 功能插件：说明官方 MCP 功能插件能力，以及插件工作流共享的规划、执行和校验阶段。
- 部署说明：说明发行包、OCI 镜像和安全部署建议。
- 常见问题：排查 MCP Server、传输方式、会话和 SQL 工具的通用问题。
