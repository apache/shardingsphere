+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把 ShardingSphere-MCP 接入支持 MCP 的 AI 应用、IDE 插件或 Agent 平台。
配置完成后，用户可以在应用中通过自然语言查看元数据、执行受控 SQL 查询，或发起数据库治理任务。

适合使用客户端集成的场景：

- 在 AI 应用、IDE 插件或 Agent 平台中接入 ShardingSphere。
- 基于 ShardingSphere 元数据完成查询辅助、结构理解、问题诊断或治理规划。
- 为团队提供统一的受控数据库访问通路。
- 为 AI 应用集成 ShardingSphere 元数据、受控 SQL 和规则变更能力。

可完成的任务和使用边界见[能力清单](../capabilities/)。

## 选择传输方式

- HTTP 适合 MCP Server 独立启动，AI 应用通过固定端点访问的场景。
- STDIO 适合本地 AI 应用拉起 ShardingSphere-MCP 子进程的场景。

## HTTP 配置

将下面片段写入 AI 应用的 MCP Server 配置；具体文件位置由应用决定。
`url` 指向已经启动的 HTTP MCP Server。

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

不同 AI 应用的配置文件位置和字段名称可能不同，请以应用自身文档为准。

## STDIO 配置

将下面片段写入 AI 应用的 MCP Server 配置；具体文件位置由应用决定。
`command` 指向发行包内的启动脚本，`args` 指向 STDIO 配置文件。

```json
{
  "mcpServers": {
    "shardingsphere": {
      "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
      "args": ["/path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml"]
    }
  }
}
```

将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

STDIO 模式下：

- 诊断日志写到 stderr 或 `logs/mcp.log`。
- 配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## 集成后的使用方式

AI 应用完成 MCP Server 配置后，用户在对话中直接描述任务。

示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段和索引。
- 执行一条只读查询，并限制返回 100 行。
- 规划一个数据加密或数据脱敏规则，先预览不要执行。

涉及 SQL 执行、规则变更或规则变更计划执行时，应先审查预览内容，再确认执行。
自研集成或协议调试场景见[开发者附录](../developer-appendix/)。
