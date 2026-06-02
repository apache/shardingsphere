+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently.
MCP is an open protocol for connecting AI applications to external data sources and tools. For protocol details, see the [official MCP documentation](https://modelcontextprotocol.io/docs/learn/architecture).

After users describe database tasks in an MCP-capable AI client, ShardingSphere-MCP can inspect ShardingSphere logical database metadata, run controlled SQL queries, and generate reviewable governance change plans.
Governance change plans describe the target objects, impact scope, and statements to be executed for rule changes such as data encryption and data masking, so users can review them before execution.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases that the MCP Server can connect to, then describe database tasks in the client.

## Use in AI Clients

ShardingSphere-MCP is designed for MCP-capable clients, IDE extensions, and agent platforms.
After client integration, users can describe database tasks in natural language in the client conversation.

Common task examples:

- Show the tables in `<logic-database>`.
- Inspect columns, indexes, and structure for `<table-name>`.
- Check whether `<table-name>` already has encryption or masking rules.
- Plan a masking rule for `<table-name>.<column-name>` and preview it without execution.
- Confirm the previous governance change plan and validate the result.

Tasks with side effects should create or preview a plan first, then run only after the user reviews the changes.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the HTTP MCP Server, and verify basic tasks.
- Capability Catalog: understand the database tasks, readable information, and usage boundaries provided by the MCP Server.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: connect the MCP Server to a client through HTTP or STDIO, and understand how to use it after integration.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, connection, configuration, metadata, and SQL execution issues.
- Feature Plugins: use official MCP feature plugins and understand how to review, apply, and validate plugin changes.
  - Plugin Workflows: understand confirmation, preview, execution, and validation for plugin change tasks.
  - Data Encryption: plan, apply, and validate data encryption rule changes through MCP feature plugins.
  - Data Masking: plan, apply, and validate data masking rule changes through MCP feature plugins.
