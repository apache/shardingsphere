+++
title = "OpenAI Responses API"
weight = 3
+++

本页说明如何在 OpenAI Responses API 中接入已经启动的 ShardingSphere-MCP HTTP Server。ShardingSphere-MCP 是 Apache ShardingSphere 提供的 MCP Server，用于把数据库元数据访问、受控 SQL 查询和数据库治理能力暴露给支持 MCP 的 AI 客户端和平台。

## 适用场景

- 适合在后端服务、Agent 平台或自研应用中通过 OpenAI API 调用 ShardingSphere-MCP。
- 适合在模型调用中按需导入 ShardingSphere-MCP 工具，并通过 `allowed_tools`、`require_approval` 或 OAuth 参数约束使用范围。
- 接入完成后，可以让模型在 API 调用中查看逻辑库中的表、查看表结构、执行受控只读查询，或调用 `database_gateway_validate_runtime_database` 对已经配置的 runtime database 进行接入前校验。

## 前置条件

- 已按[快速开始](../../quick-start/)启动 HTTP MCP Server。
- 只暴露可被 OpenAI API 访问且已受保护的远程 endpoint。ShardingSphere-MCP 内置 HTTP Server 不提供认证或授权。
- 远程平台接入时，应将 ShardingSphere-MCP 放在受信网关或反向代理后面，由外层组件提供 TLS 终止、身份认证、
  授权策略、网络访问控制和审计日志。
  安全边界见[部署说明](../../deployment/)和[配置说明](../../configuration/)。
- 该受保护的远程 endpoint 需要支持 `Streamable HTTP` 或 `HTTP/SSE`。
- 已准备 OpenAI API Key，并选择支持 remote MCP 的模型。
- 如果受保护的远程 endpoint 或网关使用 OAuth 或 Bearer 认证，还需要准备可传给 MCP tool 的访问令牌。

## 接入步骤

### 配置接入

在 Responses API 请求中，把 ShardingSphere-MCP 作为 `tools` 中的一个 `mcp` 工具传入。最小示例如下：

```python
from openai import OpenAI

client = OpenAI()

response = client.responses.create(
    model="gpt-5",
    tools=[
        {
            "type": "mcp",
            "server_label": "shardingsphere",
            "server_url": "https://example.com/mcp",
            "allowed_tools": [
                "database_gateway_search_metadata",
                "database_gateway_validate_runtime_database",
            ],
        }
    ],
    input="Use ShardingSphere-MCP to inspect the tables in the logic database.",
)
```

配置时建议关注这些字段：

- `server_label`：当前 MCP Server 的标识名称，后续工具调用和审批事件会引用这个名称。
- `server_url`：位于 ShardingSphere-MCP 前方、已受保护的远程 HTTP 地址。
- `allowed_tools`：可选。用于只导入 ShardingSphere-MCP 暴露工具中的一个子集，适合先从只读工具和预检工具开始。
- `require_approval`：可选。默认会触发审批请求。只有在你已经审查过工具范围并接受自动调用时，才应关闭审批。
- `authorization`：可选。仅当受保护的远程 endpoint 或网关需要 OAuth 或 Bearer 访问令牌时，在这里传入认证信息。
  该字段不会为 ShardingSphere-MCP 内置 HTTP Server 启用认证能力。

### 验证接入成功

识别成功：

- 首次请求成功导入工具后，Responses API 会返回 `mcp_list_tools` 输出项。
- 如果请求包含需要审批的 MCP tool call，Responses API 会返回 `mcp_approval_request` 输出项，此时需要按 OpenAI 审批流程补发 `mcp_approval_response`。

调用成功：

- 可以先用一条最小验证请求确认导入与调用链路，例如：
  - 查看 `<logic-database>` 中有哪些表。
  - 查看 `orders` 表的列和索引。
  - 对已经配置的 runtime database 调用 `database_gateway_validate_runtime_database`。
- 当 `mcp_list_tools`、审批流程或最终查询结果按预期返回时，说明接入已经生效。

如果接入失败，优先检查：

- `server_url` 是否为 OpenAI 平台可访问且已受保护的远程地址，而不是本地 `127.0.0.1` 地址或直接暴露的未认证内置 HTTP Server。
- 工具导入阶段是否返回了 `mcp_list_tools`；如果没有，先排查远程地址和服务可用性。
- 是否收到了 `mcp_approval_request`，以及后续是否正确补发了 `mcp_approval_response`。

## 注意事项

- OpenAI Responses API 的 remote MCP 只适用于远程 HTTP MCP Server，不适用于本地 `STDIO` 进程。
- 远程 MCP Server 默认会触发审批流程。对于可能产生写操作或副作用的工具，应保留审批或显式限制 `allowed_tools`。
- 只通过你信任的环境暴露 ShardingSphere-MCP，并优先使用受你控制的远程地址。
- 本页只说明 OpenAI API 集成方式。若希望在 ChatGPT 产品界面中直接接入，请参考 [ChatGPT Developer Mode](../chatgpt-developer-mode/)。
- 具体可用任务和使用边界见[能力清单](../../capabilities/)。

## 参考资料

### 相关文档

- [快速开始](../../quick-start/)
- [能力清单](../../capabilities/)
- [配置说明](../../configuration/)
- [Codex](../codex/)
- [ChatGPT Developer Mode](../chatgpt-developer-mode/)

### 官方参考

- [Connectors and MCP servers](https://platform.openai.com/docs/guides/tools-remote-mcp)
- [Responses API remote MCP reference](https://platform.openai.com/docs/api-reference/responses/remote-mcp)
