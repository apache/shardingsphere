+++
title = "Codex"
weight = 1
+++

本页说明如何在 Codex 中接入已经启动的 ShardingSphere-MCP HTTP Server。

## 前置条件

- 已按[快速开始](../../quick-start/)启动 HTTP MCP Server。
- Codex CLI 或 Codex IDE 扩展可用。
- Codex 所在环境可以访问 `http://127.0.0.1:18088/mcp`，或访问你实际配置的 MCP Server 地址。

## 添加 MCP Server

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

## 使用方式

在 Codex 会话中直接描述数据库任务，例如：

- 查看 `<logic-database>` 中有哪些表。
- 查看 `orders` 表的列和索引。
- 查询 `orders` 表前 10 行。
- 为 `orders.phone` 规划脱敏规则，先预览不要执行。

涉及 SQL 执行或规则变更时，应先审查预览内容，再确认执行。

## 参考

- [OpenAI Docs MCP](https://developers.openai.com/learn/docs-mcp)
