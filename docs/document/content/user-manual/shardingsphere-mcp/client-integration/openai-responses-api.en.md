+++
title = "OpenAI Responses API"
weight = 3
+++

This page explains how to connect an already running ShardingSphere-MCP HTTP Server to the OpenAI Responses API. ShardingSphere-MCP is the Apache ShardingSphere MCP Server that exposes database metadata access, controlled SQL queries, and database governance capabilities to MCP-capable AI clients and platforms.

## Applicable Scenarios

- Use this page when a backend service, agent platform, or custom application needs to call ShardingSphere-MCP through the OpenAI API.
- Use this integration when the model should import ShardingSphere-MCP tools on demand and constrain them with `allowed_tools`, `require_approval`, or OAuth parameters.
- After integration, the model can inspect logic databases, inspect table structures, run controlled read-only queries, or call `database_gateway_validate_proxy_connectivity` for preflight validation against configured runtime databases.

## Prerequisites

- Start the HTTP MCP Server by following [Quick Start](../../quick-start/).
- Expose only a secured remote endpoint that OpenAI API can reach. The built-in ShardingSphere-MCP HTTP Server does not provide authentication or authorization.
- For remote platform access, place ShardingSphere-MCP behind a trusted gateway or reverse proxy that provides TLS termination, authentication,
  authorization policy, network access control, and audit logs.
  See [Deployment](../../deployment/) and [Configuration](../../configuration/) for the security boundary.
- The secured remote endpoint must support `Streamable HTTP` or `HTTP/SSE`.
- Prepare an OpenAI API key and choose a model that supports remote MCP.
- If the secured remote endpoint or gateway requires OAuth or Bearer authentication, prepare an access token that can be passed to the MCP tool.

## Integration Steps

### Configure the integration

Pass ShardingSphere-MCP as an `mcp` tool in the `tools` array of a Responses API request. A minimal example is:

```python
from openai import OpenAI

client = OpenAI()

response = client.responses.create(
    model="gpt-5",
    tools=[
        {
            "type": "mcp",
            "server_label": "shardingsphere",
            "server_url": "https://example.com/mcp",
            "allowed_tools": [
                "database_gateway_search_metadata",
                "database_gateway_validate_proxy_connectivity",
            ],
        }
    ],
    input="Use ShardingSphere-MCP to inspect the tables in the logic database.",
)
```

Pay attention to these fields:

- `server_label`: the label used to identify this MCP server in tool calls and approval events.
- `server_url`: the secured remote HTTP address that fronts ShardingSphere-MCP.
- `allowed_tools`: optional. Import only a subset of ShardingSphere-MCP tools, which is useful when starting with read-only and validation tools.
- `require_approval`: optional. Approval is required by default. Disable it only after you have reviewed the tool surface and accept automatic tool calls.
- `authorization`: optional. Pass authentication data here only when the secured remote endpoint or gateway requires an OAuth or Bearer access token.
  This does not enable authentication inside the built-in ShardingSphere-MCP HTTP Server.

### Verify the integration

Recognition succeeds when:

- When the tool import succeeds, the Responses API returns an `mcp_list_tools` output item.
- If a call requires approval, the Responses API returns an `mcp_approval_request` output item. Reply with `mcp_approval_response` by following the OpenAI approval flow.

Invocation succeeds when:

- Start with a minimal validation request such as:
  - Show the tables in `<logic-database>`.
  - Show columns and indexes for the `orders` table.
  - Call `database_gateway_validate_proxy_connectivity` for a configured runtime database.
- When `mcp_list_tools`, approval flow events, or final query results appear as expected, the integration is working.

If the integration fails, check these items first:

- Confirm that `server_url` is a secured endpoint reachable from OpenAI, not a local `127.0.0.1` address or a directly exposed unauthenticated built-in HTTP Server.
- Confirm that the tool import step returns `mcp_list_tools`; if not, start with the remote endpoint and server availability.
- Confirm whether an `mcp_approval_request` was returned and whether the matching `mcp_approval_response` was sent.

## Notes

- The OpenAI Responses API remote MCP tool works with remote HTTP MCP Servers. It does not connect to local `STDIO` processes.
- Remote MCP calls require approval by default. Keep approvals in place for tools that can write data or cause side effects, or explicitly constrain the surface with `allowed_tools`.
- Only expose ShardingSphere-MCP through an environment you trust, and prefer a server URL you control.
- This page covers the OpenAI API integration path only. If you want to connect ShardingSphere-MCP directly in the ChatGPT product UI, use [ChatGPT Developer Mode](../chatgpt-developer-mode/).
- See the [Capability Catalog](../../capabilities/) for the supported task surface and usage boundaries.

## References

### Related documents

- [Quick Start](../../quick-start/)
- [Capability Catalog](../../capabilities/)
- [Configuration](../../configuration/)
- [Codex](../codex/)
- [ChatGPT Developer Mode](../chatgpt-developer-mode/)

### Official references

- [Connectors and MCP servers](https://platform.openai.com/docs/guides/tools-remote-mcp)
- [Responses API remote MCP reference](https://platform.openai.com/docs/api-reference/responses/remote-mcp)
