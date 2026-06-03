+++
title = "Codex"
weight = 1
+++

This page explains how to connect Codex to an already running ShardingSphere-MCP HTTP Server.

## Prerequisites

- Start the HTTP MCP Server by following [Quick Start](../../quick-start/).
- Codex CLI or the Codex IDE extension is available.
- The Codex environment can reach `http://127.0.0.1:18088/mcp`, or the actual MCP Server address you configured.

## Add the MCP Server

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

## Usage

Describe database tasks directly in a Codex session, for example:

- Show the tables in `<logic-database>`.
- Show columns and indexes for the `orders` table.
- Query the first 10 rows from the `orders` table.
- Plan a masking rule for `orders.phone` and preview it without execution.

When SQL execution or rule changes are involved, review the preview content before confirming execution.

## Reference

- [OpenAI Docs MCP](https://developers.openai.com/learn/docs-mcp)
