+++
title = "ChatGPT Developer Mode"
weight = 4
+++

This page explains how to connect an already running ShardingSphere-MCP HTTP Server in ChatGPT Developer Mode. ShardingSphere-MCP is the Apache ShardingSphere MCP Server that exposes database metadata access, controlled SQL queries, and database governance capabilities to MCP-capable AI clients and platforms.

## Applicable Scenarios

- Use this page when you want to connect a remote ShardingSphere-MCP server directly in the ChatGPT web product without writing backend integration code.
- Use this integration when ChatGPT conversations should select a remote MCP app and directly call ShardingSphere-MCP tools for metadata queries, controlled queries, rule planning, or preflight validation.
- After integration, ChatGPT can inspect logic databases, inspect table structures, or call `database_gateway_validate_runtime_database` for preflight validation against configured runtime databases.

## Prerequisites

- Start the HTTP MCP Server by following [Quick Start](../../quick-start/).
- Expose only a secured remote endpoint that ChatGPT can reach. The built-in ShardingSphere-MCP HTTP Server does not provide authentication or authorization.
- For remote platform access, place ShardingSphere-MCP behind a trusted gateway or reverse proxy that provides TLS termination, authentication,
  authorization policy, network access control, and audit logs.
  See [Deployment](../../deployment/) and [Configuration](../../configuration/) for the security boundary.
- The secured remote endpoint supports `SSE` or `streaming HTTP`.
- Use a ChatGPT web account that is eligible for Developer Mode. The current beta is available on the web to Pro, Plus, Business, Enterprise, and Education accounts.
- Decide whether the secured remote endpoint uses `OAuth`, `No Authentication`, or `Mixed Authentication`.
  Use `No Authentication` only for controlled private testing or when an outer network boundary already restricts access.

## Integration Steps

### Configure the integration

1. In ChatGPT Web, go to `Settings -> Apps -> Advanced settings -> Developer mode` and enable Developer Mode.
2. Open the app settings page and use `Create app` to create a new app for ShardingSphere-MCP.
3. In the app configuration, enter the secured remote ShardingSphere-MCP address and select the authentication mode that matches the endpoint:
   - `OAuth`
   - `No Authentication` for controlled private testing or an already restricted endpoint only
   - `Mixed Authentication`
4. Save the app and refresh the tool list on the app details page so ChatGPT can pull the latest tools and descriptions from ShardingSphere-MCP.

### Verify the integration

Recognition succeeds when:

- In the app settings page, confirm that the ShardingSphere-MCP app exists and that imported tools are visible.
- In a ChatGPT conversation, switch to Developer Mode and select the app.

Invocation succeeds when:

- Start with a minimal validation task such as:
  - Show the tables in `logic_db`.
  - Show columns and indexes for the `orders` table.
  - Call `database_gateway_validate_runtime_database` for a configured runtime database.
- If ChatGPT recognizes the app and can invoke its imported tools, the integration is working.

If the integration fails, check these items first:

- Confirm that the remote MCP address is reachable from ChatGPT and is not a local `127.0.0.1` address or a directly exposed unauthenticated built-in HTTP Server.
- Confirm that the tool list was refreshed after saving the app and that ChatGPT successfully imported the ShardingSphere-MCP tools.
- Confirm that the selected authentication mode matches the actual MCP Server configuration.

## Notes

- ChatGPT Developer Mode supports both read and write tools. Review write actions and approval prompts carefully before allowing SQL execution or rule changes.
- This entry point works only with remote MCP Servers. It does not connect to local `STDIO` processes.
- When multiple apps are enabled in the same conversation, explicitly tell ChatGPT to prefer ShardingSphere-MCP.
- This page covers the ChatGPT product UI path only. For OpenAI API code integration, use [OpenAI Responses API](../openai-responses-api/).
- See the [Capability Catalog](../../capabilities/) for the supported task surface and usage boundaries.

## References

### Related documents

- [Quick Start](../../quick-start/)
- [Capability Catalog](../../capabilities/)
- [Configuration](../../configuration/)
- [Codex](../codex/)
- [OpenAI Responses API](../openai-responses-api/)

### Official references

- [ChatGPT Developer mode](https://platform.openai.com/docs/guides/developer-mode)
