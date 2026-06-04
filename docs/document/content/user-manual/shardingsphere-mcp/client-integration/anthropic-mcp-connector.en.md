+++
title = "Anthropic MCP Connector"
weight = 5
+++

This page explains how to connect an already running ShardingSphere-MCP HTTP Server through the Anthropic MCP Connector in the Messages API. ShardingSphere-MCP is the Apache ShardingSphere MCP Server that exposes database metadata access, controlled SQL queries, and database governance capabilities to MCP-capable AI clients and platforms.

## Applicable Scenarios

- Use this page when you want to attach a remote ShardingSphere-MCP server directly in the Anthropic Messages API without implementing a separate MCP client.
- Use this integration when Claude API requests should expose ShardingSphere-MCP tools for metadata queries, controlled queries, rule planning, and preflight validation.
- After integration, Claude can inspect logic databases, inspect table structures, or call `database_gateway_validate_proxy_connectivity` for preflight validation against configured runtime databases.

## Prerequisites

- Start the HTTP MCP Server by following [Quick Start](../../quick-start/).
- Expose only a secured remote HTTPS endpoint that the Anthropic Messages API can reach. The built-in ShardingSphere-MCP HTTP Server does not provide authentication or authorization.
- For remote platform access, place ShardingSphere-MCP behind a trusted gateway or reverse proxy that provides TLS termination, authentication,
  authorization policy, network access control, and audit logs.
  See [Deployment](../../deployment/) and [Configuration](../../configuration/) for the security boundary.
- The secured remote endpoint supports `Streamable HTTP` or `SSE`.
- Prepare an Anthropic API key.
- The current MCP Connector version requires the request header `anthropic-beta: mcp-client-2025-11-20`.
- If the secured remote endpoint or gateway requires OAuth or Bearer authentication, prepare an access token that can be passed as `authorization_token`.

## Integration Steps

### Configure the integration

In a Messages API request, declare ShardingSphere-MCP in `mcp_servers` first, then add the matching `mcp_toolset` in the `tools` array. A minimal example is:

```bash
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: mcp-client-2025-11-20" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": "Use ShardingSphere-MCP to inspect the tables in the logic database."
      }
    ],
    "mcp_servers": [
      {
        "type": "url",
        "url": "https://example.com/mcp",
        "name": "shardingsphere"
      }
    ],
    "tools": [
      {
        "type": "mcp_toolset",
        "mcp_server_name": "shardingsphere"
      }
    ]
  }'
```

If the secured remote endpoint or gateway requires OAuth or Bearer authentication, add this field to the `mcp_servers` entry:

```json
"authorization_token": "YOUR_TOKEN"
```

To expose only a subset of tools, use `default_config` and `configs` in the `mcp_toolset` as an allowlist or denylist. For example, disable everything by default and enable only a small subset:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "shardingsphere",
  "default_config": {
    "enabled": false
  },
  "configs": {
    "database_gateway_search_metadata": {
      "enabled": true
    },
    "database_gateway_validate_proxy_connectivity": {
      "enabled": true
    }
  }
}
```

### Verify the integration

Recognition succeeds when:

- Start with a minimal request to confirm that `mcp_servers` and `mcp_toolset` are correctly paired and do not trigger validation errors for mismatched server names or dangling toolsets.

Invocation succeeds when:

- In a Claude conversation, run a minimal validation task such as:
  - Show the tables in `<logic-database>`.
  - Show columns and indexes for the `orders` table.
  - Call `database_gateway_validate_proxy_connectivity` for a configured runtime database.
- If Claude returns tool results from ShardingSphere-MCP, the integration is working.

If the integration fails, check these items first:

- Confirm that the request includes the `anthropic-beta: mcp-client-2025-11-20` header.
- Confirm that the `name` in `mcp_servers` exactly matches `mcp_server_name` in the `mcp_toolset`.
- Confirm that the remote address is a secured HTTPS MCP endpoint reachable from Anthropic, not a local `127.0.0.1` address or a directly exposed unauthenticated built-in HTTP Server.

## Notes

- The Anthropic MCP Connector currently supports remote HTTP MCP Servers only. It does not connect to local `STDIO` processes.
- The platform integration currently supports tool calls only. It does not expose the full MCP resource surface.
- By default, `mcp_toolset` enables all tools exposed by the remote MCP Server. For read-only assistants or controlled trial environments, explicitly disable write-oriented tools that are not needed.
- This feature currently requires the `anthropic-beta: mcp-client-2025-11-20` header. If Anthropic updates the beta version, update the documentation and the request configuration together.
- This page covers the Anthropic platform API path only. For Claude Code CLI integration, use [Claude Code](../claude-code/).
- See the [Capability Catalog](../../capabilities/) for the supported task surface and usage boundaries.

## References

### Related documents

- [Quick Start](../../quick-start/)
- [Capability Catalog](../../capabilities/)
- [Configuration](../../configuration/)
- [Claude Code](../claude-code/)

### Official references

- [MCP connector](https://platform.claude.com/docs/en/agents-and-tools/mcp-connector)
