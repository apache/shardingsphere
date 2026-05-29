+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the MCP Server for Apache ShardingSphere. It can run independently and expose ShardingSphere logical database metadata, safe SQL access, and plugin workflows to MCP clients.
It is designed for models or agents to understand database structure, read governance state, and call controlled tools when they need to query data or create reviewable governance change plans.

ShardingSphere-MCP configuration starts from databases: configure the ShardingSphere logical databases that the MCP Server can connect to, then read metadata or call SQL tools through an MCP client.

## Structure

- Quick Start: build the distribution, configure a reachable logical database, start the HTTP MCP Server, and verify metadata reads and read-only SQL queries.
- Capabilities: understand the resources, tools, prompts, completions, and workflows exposed by the MCP Server.
- Configuration: configure transport, `runtimeDatabases`, plugin directories, and launch parameters.
- Client Integration: use HTTP, STDIO, session response headers, and capability discovery calls.
- Workflows: understand the shared planning, apply, and validation flow used by feature plugins.
- Feature Plugins: use official MCP feature plugins.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP Server, transport, session, and SQL tool issues.
