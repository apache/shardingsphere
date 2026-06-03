+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently.
MCP is an open protocol for connecting AI applications to external data sources and tools. For protocol details, see the [official MCP documentation](https://modelcontextprotocol.io/docs/learn/architecture).

AI application developers can integrate ShardingSphere-MCP as a controlled database access capability.
After integration, users can inspect database structure, run controlled queries, and plan reviewable ShardingSphere rule changes through natural language.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases or regular databases that it can connect to, then complete integration in the AI application.

## Database Access for AI Applications

ShardingSphere-MCP is designed for MCP-capable AI applications, IDE extensions, and agent platforms.
After MCP integration, users can describe database tasks in natural language in the AI application.

Common task examples:

- Show the tables in `<logic-database>`.
- Inspect columns, indexes, and structure for `<table-name>`.
- Check whether `<table-name>` already has encryption or masking rules.
- Plan a masking rule for `<table-name>.<column-name>` and preview it without execution.
- Confirm the previous governance change plan and validate the result.

Tasks with side effects should create or preview a plan first, then run only after the user reviews the changes.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the MCP Server, and verify natural-language tasks in an AI application.
- Capability Catalog: understand the database tasks and usage boundaries that users can access through natural language.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: connect the MCP Server to an AI application through HTTP or STDIO, and understand how to use it after integration.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, connection, configuration, metadata, and SQL execution issues.
- Feature Plugins: use official MCP feature plugins and understand how to review, apply, and validate plugin changes.
  - Rule Change Flow: understand confirmation, preview, execution, and validation for rule change tasks.
  - Data Encryption: plan, apply, and validate data encryption rule changes through MCP feature plugins.
  - Data Masking: plan, apply, and validate data masking rule changes through MCP feature plugins.
- Developer Appendix: reference protocol details and HTTP debugging examples for custom integration or protocol debugging.
