+++
title = "Feature Plugins"
weight = 7
chapter = true
+++

ShardingSphere-MCP 通过 feature plugin 扩展领域能力。
MCP runtime 负责 transport、session、descriptor discovery、metadata 和 workflow 基础设施；feature plugin 负责提供具体 tools、resources 和业务语义。

发行包默认包含以下官方 MCP feature plugin：

- Encrypt：规划、执行和校验数据加密规则。
- Mask：规划、执行和校验数据脱敏规则。

新增或第三方 feature plugin 可以通过 `plugins/` 目录加入运行时 classpath。
如果 feature plugin 未随发行包提供，启动前需要同时准备它依赖的 ShardingSphere 模块和第三方 jar。
