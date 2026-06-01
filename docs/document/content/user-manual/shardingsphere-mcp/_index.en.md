+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently and expose ShardingSphere logical database metadata, safe SQL access, and plugin workflows to MCP clients.
ShardingSphere-MCP provides models and agents with a controlled access path to ShardingSphere logical databases.
Through an MCP client, a model can actively discover database structure, read governance state, and call SQL tools or create reviewable governance change plans within defined boundaries. Database connections, sessions, and execution boundaries are managed by the MCP Server.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases that the MCP Server can connect to, then read metadata or call SQL tools through an MCP client.

## Use MCP through natural language

ShardingSphere-MCP is designed for model clients, IDE extensions, and agent platforms that support MCP.
After client integration, users describe database tasks in the model conversation, and the model can read resources, call tools, request completions, or create plugin workflow plans as needed.
Regular users do not need to hand-write JSON-RPC requests, remember resource URIs, or directly choose low-level tools.

Common task examples:

- Show the tables in `<logic-database>`.
- Inspect columns, indexes, and structure for `<table-name>`.
- Check whether `<table-name>` already has encryption or masking rules.
- Plan a masking rule for `<table-name>.<column-name>` and preview it without execution.
- Confirm the previous governance change plan and validate the result.

Tasks with side effects should create or preview a plan first, then run only after the user reviews the changes.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the HTTP MCP Server, and verify metadata reads and read-only SQL queries.
- Capabilities: understand the resources, tools, prompts, completions, and workflows exposed by the MCP Server.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: use HTTP, STDIO, session response headers, and capability discovery calls.
- Feature Plugins: use official MCP feature plugins and understand the shared planning, apply, and validation phases used by plugin workflows.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, transport, session, and SQL tool issues.
