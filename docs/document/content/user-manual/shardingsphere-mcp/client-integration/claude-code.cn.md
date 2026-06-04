+++
title = "Claude Code"
weight = 2
+++

本页说明如何在 Claude Code 中接入已经启动的 ShardingSphere-MCP HTTP Server，或由 Claude Code 拉起本地 STDIO MCP Server。

## 前置条件

- 已按[快速开始](../../quick-start/)准备 ShardingSphere-MCP 发行包和数据库配置。
- Claude Code CLI 可用。
- 使用 HTTP 方式时，Claude Code 所在环境可以访问 `http://127.0.0.1:18088/mcp`，或访问你实际配置的 MCP Server 地址。

## 添加 HTTP MCP Server

在使用 Claude Code 的项目目录中执行：

```bash
claude mcp add --transport http shardingsphere http://127.0.0.1:18088/mcp
```

查看是否添加成功：

```bash
claude mcp list
```

在 Claude Code 中运行：

```text
/mcp
```

如果希望当前用户的所有 Claude Code 项目都可使用该 MCP Server，可以使用用户级配置：

```bash
claude mcp add --transport http --scope user shardingsphere http://127.0.0.1:18088/mcp
```

也可以在项目根目录创建 `.mcp.json`：

```json
{
  "mcpServers": {
    "shardingsphere": {
      "type": "http",
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

## 使用 STDIO MCP Server

如果希望 Claude Code 在本地拉起 ShardingSphere-MCP 进程，可以使用 STDIO：

```bash
claude mcp add --transport stdio shardingsphere -- \
  /path/to/apache-shardingsphere-mcp/bin/start.sh \
  /path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml
```

将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

## 使用方式

在 Claude Code 中直接描述数据库任务，例如：

- 查看 `<logic-database>` 中有哪些表。
- 查看 `orders` 表的列和索引。
- 查询 `orders` 表前 10 行。
- 为 `orders.status` 规划可逆加密，先预览不要执行。

涉及 SQL 执行或规则变更时，应先审查预览内容，再确认执行。

## 参考

- [Connect Claude Code to tools via MCP](https://code.claude.com/docs/en/mcp)
