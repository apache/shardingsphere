+++
title = "Codex"
weight = 1
+++

This page explains how to connect Codex to an already running ShardingSphere-MCP HTTP Server. ShardingSphere-MCP is the Apache ShardingSphere MCP Server that exposes database metadata access, controlled SQL queries, and database governance capabilities to MCP-capable AI clients and platforms.

## Applicable Scenarios

- Use this page when you want to connect an already running ShardingSphere-MCP HTTP Server to Codex CLI or the Codex IDE extension.
- Use this integration when Codex sessions need database metadata queries, controlled read-only SQL queries, rule planning, or preflight validation.
- After integration, Codex can inspect logic databases, inspect table structures, run controlled read-only queries, or call `database_gateway_validate_runtime_database` for preflight validation.

## Prerequisites

- Start the HTTP MCP Server by following [Quick Start](../../quick-start/).
- Codex CLI or the Codex IDE extension is available.
- The Codex environment can reach `http://127.0.0.1:18088/mcp`, or the actual MCP Server address you configured.

## Integration Steps

### Configure the integration

Use Codex CLI to add ShardingSphere-MCP:

```bash
codex mcp add shardingsphere --url http://127.0.0.1:18088/mcp
```

Verify that it has been added:

```bash
codex mcp list
```

Alternatively, write the configuration directly to `~/.codex/config.toml`:

```toml
[mcp_servers.shardingsphere]
url = "http://127.0.0.1:18088/mcp"
```

If the MCP Server address is not the default value, replace the URL with the actual address.

### Verify the integration

Recognition succeeds when:

- Run `codex mcp list` and confirm that `shardingsphere` appears in the MCP Server list.

Invocation succeeds when:

- In a Codex session, run a minimal validation task such as:
  - Show the tables in `logic_db`.
  - Show columns and indexes for the `orders` table.
  - Run `database_gateway_validate_runtime_database` against a configured runtime database.
- If the tool list and query results are returned, the integration is working.

## Notes

- This page only covers connecting Codex to an already running HTTP MCP Server. It does not cover starting a local `STDIO` process from Codex.
- See the [Capability Catalog](../../capabilities/) for the supported task surface and usage boundaries.
- When SQL execution or rule changes are involved, review the preview content before confirming execution.
- If you want to integrate ShardingSphere-MCP through the OpenAI API or the ChatGPT product UI, use the platform and API integration pages instead of reusing the steps on this page.

## References

### Related documents

- [Quick Start](../../quick-start/)
- [Capability Catalog](../../capabilities/)
- [Configuration](../../configuration/)
- [OpenAI Responses API](../openai-responses-api/)
- [ChatGPT Developer Mode](../chatgpt-developer-mode/)

### Official references

- [Docs MCP](https://platform.openai.com/docs/docs-mcp)
