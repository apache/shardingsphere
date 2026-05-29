+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP 是 Apache ShardingSphere 的 MCP Server，可以独立启动，并向 MCP 客户端暴露 ShardingSphere 逻辑库的元数据、安全 SQL 访问能力和插件工作流。
本文将 MCP 协议中的 tools、resources、prompts 和 completions 分别称为工具、资源、提示和补全；协议方法名和 JSON 字段名仍保留英文，例如 `tools/list` 和 `resources/read`。

ShardingSphere-MCP 的配置以数据库为核心：先配置 MCP Server 可以连接的 ShardingSphere 逻辑库，再通过 MCP 客户端读取元数据或调用 SQL 工具。

## 文档结构

- 快速开始：构建发行包，配置一个可连接的逻辑库，启动 HTTP MCP Server，并验证元数据读取和只读 SQL 查询。
- 配置说明：说明传输方式、`runtimeDatabases`、插件目录和启动参数。
- 客户端集成：说明 HTTP、STDIO、会话响应头和能力发现调用方式。
- 工作流基础：说明插件工作流共享的规划、执行和校验机制。
- 功能插件：说明官方 MCP 功能插件能力。
- 部署说明：说明发行包、OCI 镜像和安全部署建议。
- 常见问题：排查 MCP Server、传输方式、会话和 SQL 工具的通用问题。
