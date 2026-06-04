+++
title = "Anthropic MCP Connector"
weight = 5
+++

本页说明如何在 Anthropic Messages API 中通过 MCP Connector 接入已经启动的 ShardingSphere-MCP HTTP Server。ShardingSphere-MCP 是 Apache ShardingSphere 提供的 MCP Server，用于把数据库元数据访问、受控 SQL 查询和数据库治理能力暴露给支持 MCP 的 AI 客户端和平台。

## 适用场景

- 适合在 Anthropic Messages API 中直接挂接远程 ShardingSphere-MCP，而不单独实现 MCP client。
- 适合在 Claude API 请求中按需暴露 ShardingSphere-MCP 的元数据查询、受控查询、规则规划和接入前预检能力。
- 接入完成后，可以在 Claude 会话中查看逻辑库中的表、查看表结构，或调用 `database_gateway_validate_proxy_connectivity` 对已经配置的 runtime database 进行接入前校验。

## 前置条件

- 已按[快速开始](../../quick-start/)启动 HTTP MCP Server，并准备一个可从 Anthropic Messages API 访问的远程 HTTPS 地址。
- 该远程 MCP Server 支持 `Streamable HTTP` 或 `SSE`。
- 已准备 Anthropic API Key。
- 当前 MCP Connector 版本要求携带 `anthropic-beta: mcp-client-2025-11-20` 请求头。
- 如果远程 MCP Server 需要 OAuth 认证，需要提前准备可传入 `authorization_token` 的访问令牌。

## 接入步骤

### 配置接入

在 Messages API 请求中，先用 `mcp_servers` 声明 ShardingSphere-MCP，再在 `tools` 数组里添加与之对应的 `mcp_toolset`。最小示例如下：

```bash
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: mcp-client-2025-11-20" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": "Use ShardingSphere-MCP to inspect the tables in the logic database."
      }
    ],
    "mcp_servers": [
      {
        "type": "url",
        "url": "https://example.com/mcp",
        "name": "shardingsphere"
      }
    ],
    "tools": [
      {
        "type": "mcp_toolset",
        "mcp_server_name": "shardingsphere"
      }
    ]
  }'
```

如果远程 MCP Server 需要 OAuth，可以在 `mcp_servers` 条目中增加：

```json
"authorization_token": "YOUR_TOKEN"
```

如果只想暴露一部分工具，可以在 `mcp_toolset` 中使用 `default_config` 和 `configs` 做 allowlist 或 denylist。例如，把默认值设为禁用，再显式启用少量工具：

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "shardingsphere",
  "default_config": {
    "enabled": false
  },
  "configs": {
    "database_gateway_search_metadata": {
      "enabled": true
    },
    "database_gateway_validate_proxy_connectivity": {
      "enabled": true
    }
  }
}
```

### 验证接入成功

识别成功：

- 先用一条最小请求确认 `mcp_servers` 与 `mcp_toolset` 已正确配对，没有触发服务名不匹配或 toolset 悬空错误。

调用成功：

- 在 Claude 会话中执行一条最小验证任务，例如：
  - 查看 `<logic-database>` 中有哪些表。
  - 查看 `orders` 表的列和索引。
  - 对已经配置的 runtime database 调用 `database_gateway_validate_proxy_connectivity`。
- 如果 Claude 能返回来自 ShardingSphere-MCP 的工具结果，说明接入已经生效。

如果接入失败，优先检查：

- 是否携带了 `anthropic-beta: mcp-client-2025-11-20` 请求头。
- `mcp_servers` 中的 `name` 和 `mcp_toolset` 中的 `mcp_server_name` 是否严格一致。
- 远程地址是否为 Anthropic 平台可访问的 HTTPS MCP 端点，而不是本地 `127.0.0.1` 地址。

## 注意事项

- Anthropic MCP Connector 当前只支持远程 HTTP MCP Server，不支持本地 `STDIO` 进程。
- 当前平台侧只支持 tool calls，不提供完整 MCP 资源面接入。
- 默认情况下 `mcp_toolset` 会启用远程 MCP Server 暴露的全部工具。对于只读助手或受控试用环境，建议显式禁用不需要的写入类工具。
- 该功能当前要求 `anthropic-beta: mcp-client-2025-11-20` 请求头；如果官方更新 beta 版本，需要同步调整文档与调用配置。
- 本页只说明 Anthropic 平台 API 接入方式。若希望在 Claude Code CLI 中接入，请参考 [Claude Code](../claude-code/)。
- 具体可用任务和使用边界见[能力清单](../../capabilities/)。

## 参考资料

### 相关文档

- [快速开始](../../quick-start/)
- [能力清单](../../capabilities/)
- [配置说明](../../configuration/)
- [Claude Code](../claude-code/)

### 官方参考

- [MCP connector](https://platform.claude.com/docs/en/agents-and-tools/mcp-connector)
