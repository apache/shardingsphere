+++
title = "ChatGPT Developer Mode"
weight = 4
+++

本页说明如何在 ChatGPT Developer Mode 中接入已经启动的 ShardingSphere-MCP HTTP Server。ShardingSphere-MCP 是 Apache ShardingSphere 提供的 MCP Server，用于把数据库元数据访问、受控 SQL 查询和数据库治理能力暴露给支持 MCP 的 AI 客户端和平台。

## 适用场景

- 适合在 ChatGPT Web 产品中直接接入远程 ShardingSphere-MCP，而不经过自定义后端代码。
- 适合在 ChatGPT 会话里选择一个远程 MCP App，并直接调用 ShardingSphere-MCP 提供的元数据查询、受控查询、规则规划或接入前预检能力。
- 接入完成后，可以在 ChatGPT 对话中查看逻辑库中的表、查看表结构，或调用 `database_gateway_validate_runtime_database` 对已经配置的 runtime database 进行接入前校验。

## 前置条件

- 已按[快速开始](../../quick-start/)启动 HTTP MCP Server。
- 只暴露可被 ChatGPT 访问且已受保护的远程 endpoint。ShardingSphere-MCP 内置 HTTP Server 不提供认证或授权。
- 远程平台接入时，应将 ShardingSphere-MCP 放在受信网关或反向代理后面，由外层组件提供 TLS 终止、身份认证、
  授权策略、网络访问控制和审计日志。
  安全边界见[部署说明](../../deployment/)和[配置说明](../../configuration/)。
- 该受保护的远程 endpoint 需要支持 `SSE` 或 `streaming HTTP`。
- 已准备可用的 ChatGPT Web 账号。当前 Developer Mode 在 Web 端 Beta 提供给 Pro、Plus、Business、Enterprise 和 Education 账号。
- 提前确定受保护的远程 endpoint 使用 `OAuth`、`No Authentication` 或 `Mixed Authentication` 中的哪一种模式。
  `No Authentication` 仅适用于受控私有测试，或外层网络边界已经限制访问的 endpoint。

## 接入步骤

### 配置接入

1. 在 ChatGPT Web 中进入 `Settings -> Apps -> Advanced settings -> Developer mode`，启用 Developer Mode。
2. 打开 App 设置页，使用 `Create app` 为 ShardingSphere-MCP 创建一个新的 app。
3. 在 app 配置中填写 ShardingSphere-MCP 的受保护远程地址，并选择与 endpoint 匹配的认证方式：
   - `OAuth`
   - `No Authentication`，仅适用于受控私有测试或已经由外层边界限制访问的 endpoint
   - `Mixed Authentication`
4. 保存 app 配置，并在 app 详情页刷新工具列表，使 ChatGPT 从 ShardingSphere-MCP 拉取最新工具和描述。

### 验证接入成功

识别成功：

- 在 app 设置页中确认新建的 ShardingSphere-MCP app 已经出现，并能看到导入的工具列表。
- 在 ChatGPT 会话中切换到 Developer Mode，并选中该 app。

调用成功：

- 先执行一条最小验证任务，例如：
  - 查看 `logic_db` 中有哪些表。
  - 查看 `orders` 表的列和索引。
  - 对已经配置的 runtime database 调用 `database_gateway_validate_runtime_database`。
- 如果 ChatGPT 能识别到对应 app，并能按预期调用导入工具，说明接入已经生效。

如果接入失败，优先检查：

- 远程 MCP 地址是否为 ChatGPT 可访问且已受保护的远程地址，而不是本地 `127.0.0.1` 地址或直接暴露的未认证内置 HTTP Server。
- app 保存后是否已刷新工具列表，并成功拉取到 ShardingSphere-MCP 暴露的工具。
- 认证模式是否与 MCP Server 的实际配置一致。

## 注意事项

- ChatGPT Developer Mode 支持读写工具。涉及 SQL 执行或规则变更时，应仔细审查模型发起的写操作和审批提示。
- 该入口只适用于远程 MCP Server，不适用于本地 `STDIO` 进程。
- 如果一个会话中同时启用了多个 app，建议在提示中显式指定应优先使用 ShardingSphere-MCP。
- 本页只说明 ChatGPT 产品界面接入，不覆盖 OpenAI API 代码集成。后者请参考 [OpenAI Responses API](../openai-responses-api/)。
- 具体可用任务和使用边界见[能力清单](../../capabilities/)。

## 参考资料

### 相关文档

- [快速开始](../../quick-start/)
- [能力清单](../../capabilities/)
- [配置说明](../../configuration/)
- [Codex](../codex/)
- [OpenAI Responses API](../openai-responses-api/)

### 官方参考

- [ChatGPT Developer mode](https://platform.openai.com/docs/guides/developer-mode)
