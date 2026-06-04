+++
title = "Client Integration"
weight = 4
+++

Client integration connects ShardingSphere-MCP to MCP-capable AI applications, IDE extensions, or agent platforms.
After configuration, users can inspect metadata, run controlled SQL queries, or start database governance tasks through natural language in the application.

Use client integration when:

- An AI application, IDE extension, or agent platform needs to connect to ShardingSphere-MCP.
- Users need to inspect metadata, run controlled SQL, or plan ShardingSphere rule changes through natural language.
- A team needs a unified controlled database access entry.

See [Capability Catalog](../capabilities/) for supported tasks and usage boundaries.

## Typical Clients

- [Codex](./codex/): use ShardingSphere-MCP in Codex CLI or IDE extension.
- [Claude Code](./claude-code/): use ShardingSphere-MCP in Claude Code project or user configuration.

## Choose an Integration Method

- If ShardingSphere-MCP is already started independently, or multiple applications need to access the same service, choose HTTP integration.
- If it is used only in a local development environment and the AI application should start ShardingSphere-MCP when needed, choose STDIO integration.
- If you use Codex or Claude Code, start with the corresponding page in this section.
- If you use another client, follow that client's documentation and configure the ShardingSphere-MCP address or startup script.

## Using the Integration

After the AI application is configured with the MCP Server, users describe tasks directly in the conversation.

Examples:

- Show the tables in `<logic-database>`.
- Inspect columns and indexes for `<table-name>`.
- Run a read-only query and limit the result to 100 rows.
- Plan a data encryption or data masking rule and preview it without execution.

When SQL execution, rule changes, or rule change plan execution is involved, review the preview content before confirming execution.
For custom integration or protocol debugging, see the [Custom Integration Appendix](../developer-appendix/).
