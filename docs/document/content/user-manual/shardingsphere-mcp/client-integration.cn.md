+++
title = "客户端集成"
weight = 4
+++

客户端集成用于把 ShardingSphere-MCP 接入支持 MCP 的桌面客户端、IDE 插件、Agent 平台或自研应用。
配置完成后，用户可以在客户端中通过自然语言查看元数据、执行受控 SQL 查询，或发起数据库治理任务。

适合使用客户端集成的场景：

- 在支持 MCP 的客户端、IDE 插件或 Agent 平台中接入 ShardingSphere。
- 基于 ShardingSphere 元数据完成查询辅助、结构理解、问题诊断或治理规划。
- 为团队提供统一的受控数据库访问通路。
- 为自研 Agent 平台集成 ShardingSphere 元数据、安全 SQL 和治理插件能力。

可完成的任务和使用边界见[能力清单](../capabilities/)。

## 选择传输方式

- HTTP 适合 MCP Server 独立启动，客户端通过固定端点访问的场景。
- STDIO 适合本地客户端拉起 ShardingSphere-MCP 子进程的场景。

## HTTP 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
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

不同客户端的配置文件位置和字段名称可能不同，请以客户端自身文档为准。

## STDIO 配置

将下面片段写入 MCP 客户端的 server 配置；具体文件位置由客户端决定。
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

STDIO 模式适合由本地 MCP 客户端拉起 ShardingSphere-MCP 子进程。
将 `/path/to/apache-shardingsphere-mcp` 替换为实际发行包目录。

STDIO 模式下：

- 诊断日志写到 stderr 或 `logs/mcp.log`。
- 客户端配置中的 `command` 和 `args` 应指向发行包内的启动脚本和 STDIO 配置文件。

## 集成后的使用方式

客户端完成 MCP Server 配置后，用户在客户端对话中直接描述任务。

示例：

- 查看 `<logic-database>` 中有哪些表。
- 查询 `<table-name>` 的字段和索引。
- 执行一条只读查询，并限制返回 100 行。
- 规划一个数据加密或数据脱敏规则，先预览不要执行。

如果客户端提供工具调用审批界面，应重点审查 SQL 执行、规则变更、插件工作流执行等有副作用的调用。
自研客户端或协议调试场景，可结合[能力清单](../capabilities/)确认可用能力。
