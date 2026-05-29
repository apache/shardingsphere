+++
title = "功能插件"
weight = 7
chapter = true
+++

ShardingSphere-MCP 通过功能插件扩展领域能力。
MCP Server 负责传输方式、会话、描述符发现、元数据和工作流基础设施；功能插件负责提供具体工具、资源和业务语义。

发行包默认包含以下官方 MCP 功能插件：

- Encrypt：规划、执行和校验数据加密规则。
- Mask：规划、执行和校验数据脱敏规则。

新增或第三方功能插件可以通过 `plugins/` 目录加入运行时类路径。
如果功能插件未随发行包提供，启动前需要同时准备它依赖的 ShardingSphere 模块和第三方 jar。
