+++
title = "Codex"
weight = 1
+++

本页说明如何在 Codex 中接入已经启动的 ShardingSphere-MCP HTTP Server。ShardingSphere-MCP 是 Apache ShardingSphere 提供的 MCP Server，用于把数据库元数据访问、受控 SQL 查询和数据库治理能力暴露给支持 MCP 的 AI 客户端和平台。

## 适用场景

- 适合在 Codex CLI 或 Codex IDE 扩展中使用已经启动的 ShardingSphere-MCP HTTP Server。
- 适合把数据库元数据查询、只读 SQL 查询、规则规划和接入前预检能力带入 Codex 会话。
- 接入完成后，可以在 Codex 中查看逻辑库中的表、查看表结构、执行受控只读查询，或调用 `database_gateway_validate_proxy_connectivity` 进行接入前校验。

## 前置条件

- 已按[快速开始](../../quick-start/)启动 HTTP MCP Server。
- Codex CLI 或 Codex IDE 扩展可用。
- Codex 所在环境可以访问 `http://127.0.0.1:18088/mcp`，或访问你实际配置的 MCP Server 地址。

## 接入步骤

### 配置接入

使用 Codex CLI 添加 ShardingSphere-MCP：

```bash
codex mcp add shardingsphere --url http://127.0.0.1:18088/mcp
```

查看是否添加成功：

```bash
codex mcp list
```

也可以直接写入 `~/.codex/config.toml`：

```toml
[mcp_servers.shardingsphere]
url = "http://127.0.0.1:18088/mcp"
```

如果 MCP Server 地址不是默认值，请把 URL 替换为实际地址。

### 验证接入成功

识别成功：

- 运行 `codex mcp list`，确认 `shardingsphere` 已出现在 MCP Server 列表中。

调用成功：

- 在 Codex 会话中执行一条最小验证任务，例如：
  - 查看 `<logic-database>` 中有哪些表。
  - 查看 `orders` 表的列和索引。
  - 对已经配置的 runtime database 执行 `database_gateway_validate_proxy_connectivity`。
- 如果工具列表和查询结果可返回，说明接入已经生效。

## 注意事项

- 本页只说明通过已经启动的 HTTP MCP Server 接入 Codex，不覆盖本地 `STDIO` 进程拉起模式。
- 具体可用任务和使用边界见[能力清单](../../capabilities/)。
- 涉及 SQL 执行或规则变更时，应先审查预览内容，再确认执行。
- 如果希望在 OpenAI API 或 ChatGPT 产品中接入 ShardingSphere-MCP，请分别参考平台与 API 接入文档，而不要直接复用本页步骤。

## 参考资料

### 相关文档

- [快速开始](../../quick-start/)
- [能力清单](../../capabilities/)
- [配置说明](../../configuration/)
- [OpenAI Responses API](../openai-responses-api/)
- [ChatGPT Developer Mode](../chatgpt-developer-mode/)

### 官方参考

- [OpenAI Docs MCP](https://platform.openai.com/docs/docs-mcp)
