+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently.
It connects ShardingSphere logical databases to MCP-capable clients, so users can inspect metadata, run controlled SQL queries, and plan reviewable governance changes through natural language.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases that the MCP Server can connect to, then describe database tasks in the client.

## Use MCP through natural language

ShardingSphere-MCP is designed for MCP-capable clients, IDE extensions, and agent platforms.
After client integration, users can describe database tasks directly in the client conversation.

Common task examples:

- Show the tables in `<logic-database>`.
- Inspect columns, indexes, and structure for `<table-name>`.
- Check whether `<table-name>` already has encryption or masking rules.
- Plan a masking rule for `<table-name>.<column-name>` and preview it without execution.
- Confirm the previous governance change plan and validate the result.

Tasks with side effects should create or preview a plan first, then run only after the user reviews the changes.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the HTTP MCP Server, and verify basic tasks.
- Capabilities: understand the database tasks, readable information, and usage boundaries provided by the MCP Server.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: connect the MCP Server to a client through HTTP or STDIO, and understand how to use it after integration.
- Feature Plugins: use official MCP feature plugins and understand how to review, apply, and validate plugin changes.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, connection, configuration, metadata, and SQL execution issues.
