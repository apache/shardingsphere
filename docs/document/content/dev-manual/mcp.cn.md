+++
pre = "<b>5.10. </b>"
title = "MCP"
weight = 10
chapter = true
+++

本章面向希望扩展 ShardingSphere-MCP 的开发者。
用户安装和使用请查看[用户手册](../../user-manual/shardingsphere-mcp/)，协议表面请查看[技术参考](../../reference/mcp/)。

## 模块结构

MCP 子链路按 `api + support + features + core + bootstrap` 分层组织：

- `mcp/api`：public tool/resource handler 契约、descriptor 类型、协议 response 和 MCP 协议异常。
- `mcp/support`：database metadata、execution、capability、workflow context、模型、facade、SPI 和复用 helper。
- `mcp/features/encrypt`：Encrypt MCP feature。
- `mcp/features/mask`：Mask MCP feature。
- `mcp/core`：handler 发现、registry、request scope、session、SQL execution trace、metadata discovery 和 runtime context。
- `mcp/bootstrap`：基于 MCP Java SDK 的 bootstrap、HTTP/STDIO transport、配置加载和生命周期管理。
- `distribution/mcp`：独立打包、启动脚本、配置和 Dockerfile。
- `test/e2e/mcp`：端到端契约验证。

`mcp/bootstrap` 只负责发布聚合后的协议表面，不应硬编码具体 feature 业务。

## 新增 Feature Plugin

新增 feature 的推荐路径：

1. 在 `mcp/features/<feature>` 下创建模块。
2. 依赖 `mcp/api`。
3. 如果需要 database metadata、SQL execution 或 workflow 支持，依赖 `mcp/support`。
4. 不依赖 `mcp/core` 或 `mcp/bootstrap`；runtime 实现不是 feature 扩展契约。
5. 实现 `MCPHandlerProvider`。
6. 通过 `getToolHandlers()` 和 `getResourceHandlers()` 返回 feature 自己暴露的 handlers。
7. 如果 feature 拥有 workflow definitions，在同一个 provider 上实现 `MCPWorkflowDefinitionProvider`。
8. 在 `src/main/resources/META-INF/services/` 注册 `org.apache.shardingsphere.mcp.api.MCPHandlerProvider`。
9. 在 `META-INF/shardingsphere-mcp/mcp-descriptors` 下添加 descriptor。

如果 feature 要作为官方默认能力随发行包提供，还需要：

- 加入 `mcp/features/pom.xml`。
- 加入 `distribution/mcp/pom.xml`。

如果 feature 是可选插件，构建后把 jar 放入发行包 `plugins/` 目录。

## Feature Workflow 模板

需要规划、预览、执行和校验规则变更的 feature，应以数据加密 MCP feature 的规则 workflow 作为模板。
模板实现应满足：

- Feature 业务逻辑保留在 `mcp/features/<feature>`；`mcp/support` 和 `mcp/core` 只承载通用 workflow、执行、redaction、descriptor 和 runtime 契约。
- Handler 声明 canonical tool name、context type 和 workflow definition；descriptor 和 prompt 维护模型可见契约，handler 不重复维护描述字段。
- 规划前先读取 feature 自有 resources，例如算法、规则或现有配置资源，再生成可审查的 DistSQL artifact。
- 输出 schema 只暴露当前 feature 支持的 artifact；不要保留不支持的字段作为占位，也不要让模型依赖真实物理表结构。
- 有副作用的执行必须先 preview，再根据用户明确批准的 `approved_steps` 执行。
- 敏感参数在模型可见的计划、预览、执行、校验、恢复和错误输出中必须掩码；执行路径使用受控上下文中的原始值。
- 校验逻辑应读取 Proxy 可见的规则状态或 feature 状态，避免把不属于当前 feature 的物理操作作为验收条件。
- Descriptor 启动期校验应覆盖 tool/resource/prompt 名称唯一性、schema 字段、side-effect annotations、related resources、follow-up tools、
  completion target 和 workflow recovery path。

## Handler 与 Descriptor

对外新增 tool：

- 实现 `MCPToolHandler<T extends MCPRequestContext>`。
- 声明 context type。
- 声明 canonical tool name。
- `handle(...)` 只返回成功的 `MCPSuccessPayload`；参数非法、资源不存在、查询失败、超时、不支持等受控失败应抛出对应的 `ShardingSphereMCPException` 子类，由 runtime 转换为 MCP tool 错误结果。未预期的运行时失败会被脱敏并转换为 JSON-RPC internal error。
- 在 descriptor 中维护 input schema、output schema、annotations、相关 resources、follow-up tools 和副作用说明。

对外新增 resource：

- 实现 `MCPResourceHandler<T extends MCPRequestContext>`。
- 声明 context type。
- 声明 canonical resource URI template。固定 URI 也是没有变量的 URI template。
- `handle(...)` 只返回成功的 `MCPSuccessPayload`；不要在 handler 中手工构造错误 payload，受控失败应抛出对应的 `ShardingSphereMCPException` 子类，由 runtime 转换为 MCP resource 读取错误。未预期的运行时失败会被脱敏并转换为 JSON-RPC internal error。
- 在 descriptor 中维护 URI 参数含义、对象范围、MIME type、title、description、annotations 和关系元数据。

运行时代码需要 descriptor 时，应使用 canonical tool name 或 resource URI template，通过 `MCPDescriptorCatalogIndex` 从 catalog 解析。
不要在 handler 内重复维护 descriptor 字段。

## Context 选择

- 只需要 session ID、当前 transport 或 session identity 的 handler 使用 `MCPRequestContext`。
- database metadata 或 execution handler 使用 `MCPDatabaseRequestContext`。
- workflow handler 使用 `MCPWorkflowRequestContext`。

`MCPRequestScope` 是 runtime 管理的单次请求实现，名称表达的是生命周期；handler 只依赖自身所需的最小 context 接口。

Completion 请求按 session 使用 60 秒固定窗口限流，默认每分钟 600 次，可通过 Java 系统属性
`shardingsphere.mcp.maxCompletionRequestsPerMinute` 调整。

## 命名与唯一性

- Tool name 和 resource URI pattern 必须全局唯一。
- 重复 handler 或重复 descriptor 会在启动期校验时被拒绝。
- Feature URI 使用 `shardingsphere://features/<feature>/...` 命名空间。
- 不要和公共 metadata path 混用。

## Descriptor 维护原则

Descriptor 应说明模型如何使用协议表面，而不是只重复 tool 名或 URI。

维护时应包含：

- 清晰的字段描述。
- JSON schema 约束。
- 输出结构和示例。
- 安全 annotations。
- 副作用范围。
- 相关 resources。
- 下一步 actions。
- completion target。
- workflow 恢复路径。

Tool annotations 只是客户端提示，不能替代运行时校验、SQL 安全检查、用户审批或服务端授权。
