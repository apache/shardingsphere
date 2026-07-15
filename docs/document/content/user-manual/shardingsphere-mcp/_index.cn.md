+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP 是 Apache ShardingSphere 的 MCP Server，可以独立启动。
MCP 是连接 AI 应用与外部数据源和工具的开放协议，协议说明参见 [MCP 官方文档](https://modelcontextprotocol.io/docs/learn/architecture)。

AI 应用开发者可以将 ShardingSphere-MCP 作为受控数据库访问能力接入应用。
接入后，用户可以通过自然语言查看数据库结构、执行受控查询，并规划需要审查的 ShardingSphere 规则变更。

使用依赖数据库的能力前，需要准备可连接的数据库，并在 `runtimeDatabases` 中配置连接信息；如果需要使用数据加密、数据脱敏等规则变更能力，连接目标应为 ShardingSphere-Proxy 逻辑库。

## 面向 AI 应用的数据库访问

ShardingSphere-MCP 面向支持 MCP 的 AI 应用、IDE 插件和 Agent 平台使用。
完成 MCP 集成后，用户可以在 AI 应用中通过自然语言描述数据库任务。

常见任务示例：

- 查看 `logic_db` 中有哪些表。
- 查询 `orders` 的字段、索引和表结构。
- 检查 `orders` 当前是否已有加密或脱敏规则。
- 为 `orders.phone` 规划脱敏规则，先预览不要执行。
- 确认刚才的治理变更计划，并校验执行结果。

有副作用的任务应先生成或预览计划，由用户审查变更内容后再执行。

## 文档结构

- 快速开始：构建发行包，配置一个可连接的逻辑库，启动 MCP Server，并在 AI 应用中验证自然语言任务。
- 能力清单：说明用户可以通过自然语言完成的数据库任务和使用边界。
- 配置说明：说明传输方式、`runtimeDatabases`、插件目录和启动参数。
- 客户端集成：说明如何通过 HTTP 或 STDIO 把 MCP Server 接入 AI 应用，并提供 Codex 和 Claude Code 示例。
- 部署说明：说明发行包、OCI 镜像、安全部署建议、健康检查和基础运行诊断入口。
- 常见问题：排查 MCP Server、连接、配置、元数据、查询和变更的通用问题。
- 功能插件：说明官方 MCP 功能插件能力，以及插件变更的审查、执行和校验流程。
  - 规则变更流程：说明规则变更任务的确认、预览、执行和校验流程。
  - 数据加密：说明如何通过 MCP 功能插件规划、执行和校验数据加密规则变更。
  - 数据脱敏：说明如何通过 MCP 功能插件规划、执行和校验数据脱敏规则变更。

自研集成或协议调试场景可参考[自研集成附录](developer-appendix/)。
