+++
pre = "<b>4.7. </b>"
title = "ShardingSphere-MCP"
weight = 7
chapter = true
+++

ShardingSphere-MCP is the standalone Model Context Protocol runtime for Apache ShardingSphere.
It exposes logical database metadata, safe SQL access, and plugin-provided governance workflows through MCP tools, resources, prompts, and completions.

This chapter is written for users who need to start the MCP runtime, connect an MCP client, configure logical runtime databases,
deploy the official distribution, and troubleshoot common runtime issues.

## Structure

- Quick Start: verify metadata discovery and query behavior with the packaged demo runtime.
- Configuration: configure transport, runtimeDatabases, plugin directories, and launch parameters.
- Client Integration: use HTTP, STDIO, session headers, and discovery calls.
- Workflow Basics: understand the shared plan, apply, and validate flow used by feature plugins.
- Feature Plugins: use official MCP feature plugins.
- Deployment: deploy the binary distribution and OCI image safely.
- Troubleshooting: diagnose common MCP runtime, transport, session, and SQL tool issues.
