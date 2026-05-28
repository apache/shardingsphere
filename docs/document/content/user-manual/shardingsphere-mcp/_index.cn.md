+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP 是 Apache ShardingSphere 面向 Model Context Protocol 的独立运行时。
它通过 MCP tools、resources、prompts 和 completions 暴露逻辑库元数据、安全 SQL 访问能力，以及由插件提供的治理工作流。

本章面向使用者，说明如何启动 MCP runtime、连接 MCP client、配置运行时逻辑库、部署官方发行包，并排查通用问题。

## 文档结构

- 快速开始：使用发行包内置 demo runtime 验证 metadata discovery 和 query。
- 配置说明：说明 transport、runtimeDatabases、插件目录和启动参数。
- Client 集成：说明 HTTP、STDIO、session header 和 discovery 调用方式。
- Workflow 基础：说明插件工作流共享的 plan、apply、validate 机制。
- Feature Plugins：说明官方 MCP 插件能力。
- 部署说明：说明发行包、OCI 镜像和安全部署建议。
- 常见问题：排查 MCP runtime、transport、session 和 SQL tool 的通用问题。
