+++
title = "Claude Code"
weight = 2
+++

本页说明如何在 Claude Code 中接入已经启动的 ShardingSphere-MCP HTTP Server，或由 Claude Code 拉起本地 STDIO MCP Server。ShardingSphere-MCP 是 Apache ShardingSphere 提供的 MCP Server，用于把数据库元数据访问、受控 SQL 查询和数据库治理能力暴露给支持 MCP 的 AI 客户端和平台。

## 适用场景

- 适合在 Claude Code CLI 中接入已经启动的 ShardingSphere-MCP HTTP Server。
- 适合在本地开发环境中由 Claude Code 直接拉起 ShardingSphere-MCP STDIO 进程。
- 接入完成后，可以在 Claude Code 中查看逻辑库中的表、查看表结构、执行受控只读查询，或调用 `database_gateway_validate_proxy_connectivity` 进行接入前校验。

## 前置条件

- 已按[快速开始](../../quick-start/)准备 ShardingSphere-MCP 发行包和数据库配置。
- Claude Code CLI 可用。
- 使用 HTTP 方式时，Claude Code 所在环境可以访问 `http://127.0.0.1:18088/mcp`，或访问你实际配置的 MCP Server 地址。

## 接入步骤

### 接入方式选择

- 如果 ShardingSphere-MCP 已经独立启动，或者需要多个应用共享同一个服务，选择 HTTP 接入。
- 如果只在本地开发环境中使用，并希望 Claude Code 在需要时启动 ShardingSphere-MCP，选择 STDIO 接入。

### 配置接入

使用 HTTP MCP Server：

```bash
claude mcp add --transport http shardingsphere http://127.0.0.1:18088/mcp
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

如果希望 Claude Code 在本地拉起 ShardingSphere-MCP 进程，可以使用 STDIO：

```bash
claude mcp add --transport stdio shardingsphere -- \
  /path/to/apache-shardingsphere-mcp/bin/start.sh \
  /path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml
```

将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

### 验证接入成功

识别成功：

- 运行 `claude mcp list`，确认 `shardingsphere` 已出现在 MCP Server 列表中。
- 在 Claude Code 中运行：

```text
/mcp
```

调用成功：

- 在 Claude Code 对话中执行一条最小验证任务，例如：
  - 查看 `<logic-database>` 中有哪些表。
  - 查看 `orders` 表的列和索引。
  - 对已经配置的 runtime database 执行 `database_gateway_validate_proxy_connectivity`。
- 如果工具已被列出并能返回查询结果，说明接入已经生效。

## 注意事项

- 使用 STDIO 时，Claude Code 需要能够访问本地 ShardingSphere-MCP 发行包和对应配置文件。
- 同一个 `shardingsphere` server name 应只对应一种接入方式；如果需要同时保留 HTTP 与 STDIO，建议使用不同的 server name。
- 具体可用任务和使用边界见[能力清单](../../capabilities/)。
- 涉及 SQL 执行或规则变更时，应先审查预览内容，再确认执行。
- 如果希望通过 Anthropic API 平台侧接入 ShardingSphere-MCP，请参考 [Anthropic MCP Connector](../anthropic-mcp-connector/)。

## 参考资料

### 相关文档

- [快速开始](../../quick-start/)
- [能力清单](../../capabilities/)
- [配置说明](../../configuration/)
- [Anthropic MCP Connector](../anthropic-mcp-connector/)

### 官方参考

- [Connect Claude Code to tools via MCP](https://code.claude.com/docs/en/mcp)
