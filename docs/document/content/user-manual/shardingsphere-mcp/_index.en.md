+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently and expose ShardingSphere logical database metadata, safe SQL access, and plugin workflows to MCP clients.
This manual uses the MCP terms tools, resources, prompts, and completions for the corresponding protocol capabilities. Protocol method names and JSON field names stay in their original form, such as `tools/list` and `resources/read`.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases that the MCP Server can connect to, then read metadata or call SQL tools through an MCP client.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the HTTP MCP Server, and verify metadata reads and read-only SQL queries.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: use HTTP, STDIO, session response headers, and capability discovery calls.
- Workflow Basics: understand the shared planning, apply, and validation flow used by feature plugins.
- Feature Plugins: use official MCP feature plugins.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, transport, session, and SQL tool issues.
