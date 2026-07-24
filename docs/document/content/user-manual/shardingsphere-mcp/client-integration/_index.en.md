+++
title = "Client Integration"
weight = 4
+++

Client integration connects ShardingSphere-MCP to MCP-capable AI clients, product entry points, and platform APIs. After configuration, users can inspect metadata, run controlled SQL queries, or start database governance tasks through natural language in the application.

See the [Capability Catalog](../capabilities/) for supported tasks and usage boundaries.

## Client integrations

- [Codex](./codex/) connects an already running HTTP MCP Server to Codex CLI or the Codex IDE extension.
- [Claude Code](./claude-code/) connects an already running HTTP MCP Server to Claude Code CLI, or lets Claude Code start a local STDIO MCP Server.

## Platform and API integrations

- [OpenAI Responses API](./openai-responses-api/) connects a remote MCP Server through the OpenAI API in backend applications.
- [ChatGPT Developer Mode](./chatgpt-developer-mode/) connects a remote MCP Server directly in the ChatGPT web product.
- [Anthropic MCP Connector](./anthropic-mcp-connector/) connects a remote MCP Server directly in the Anthropic Messages API.

Platform and API integrations require a secured, remotely reachable MCP endpoint.
Do not expose the built-in ShardingSphere-MCP HTTP Server directly to remote platforms because it does not provide authentication or authorization.
For remote platform access, place it behind a trusted gateway or reverse proxy that provides TLS termination, authentication, authorization policy, network access control, and audit logs.
See [Deployment](../deployment/) and [Configuration](../configuration/) for the security boundary.
Local examples such as `http://127.0.0.1:18088/mcp` are only suitable for local client integration pages and cannot be reused directly for OpenAI or Anthropic platform entry points.

## Choose an entry point

- Use a client integration when ShardingSphere-MCP is primarily used in local development or coding assistants.
- Use a platform or API integration when a backend service needs to call models through an API and let those models call MCP tools.
- When multiple clients should share the same ShardingSphere-MCP service, prefer an independently started HTTP MCP Server.
- When the MCP process should start only on demand in local development, choose a client that supports STDIO.
- When the target entry point runs inside OpenAI or Anthropic infrastructure, confirm that the secured remote MCP address is reachable from that platform before continuing.

## Typical usage after integration

After configuration, users describe tasks directly in the conversation. Examples:

- Show the tables in `logic_db`.
- Inspect columns and indexes for `orders`.
- Run a read-only query and limit the result to 100 rows.
- Call `database_gateway_validate_runtime_database` for a configured runtime database.
- Plan a data encryption or data masking rule and preview it without execution.

When SQL execution, rule changes, or rule change plan execution is involved, review the preview content before confirming execution. For custom integration or protocol debugging, see the [Custom Integration Appendix](../developer-appendix/).
