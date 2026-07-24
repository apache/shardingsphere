+++
title = "Custom Integration Appendix"
weight = 9
+++

This page is for developers who build custom MCP integrations, debug protocol requests, or troubleshoot client adaptation issues.
For normal usage, see [Quick Start](../quick-start/), [Client Integration](../client-integration/), and [Capability Catalog](../capabilities/).

## Generic Client Configuration Examples

If the AI application you use does not have a dedicated page, choose HTTP or STDIO configuration according to that client's own documentation.

HTTP example:

```json
{
  "mcpServers": {
    "shardingsphere-http": {
      "url": "http://127.0.0.1:18088/mcp"
    }
  }
}
```

STDIO example:

```json
{
  "mcpServers": {
    "shardingsphere": {
      "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
      "args": ["/path/to/apache-shardingsphere-mcp/conf/mcp-stdio.yaml"]
    }
  }
}
```

Replace `/path/to/apache-shardingsphere-mcp` with the actual distribution directory.

## Protocol Capability Discovery

| Entry                                                | Returned content                                                                              | Usage scenario                                                           |
|------------------------------------------------------|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| `tools/list`                                         | Tools exposed by the current MCP Server.                                                      | Build the callable action catalog for a custom client.                   |
| `resources/list`                                     | Static resources exposed by the current MCP Server.                                           | Read fixed context from a custom client.                                 |
| `resources/templates/list`                           | Resource templates exposed by the current MCP Server.                                         | Read context by database, schema, table, and other parameters.           |
| `prompts/list`                                       | Prompts exposed by the current MCP Server.                                                    | Read task guidance templates from a custom client.                       |
| `completion/complete`                                | Completion candidates for a specified parameter.                                              | Provide completion for database, schema, table, column, and other names. |
| `shardingsphere://capabilities`                      | Runtime databases, connection targets, feature plugins, and side-effect boundaries.           | Determine which database tasks the current MCP Server supports.          |
| `shardingsphere://databases/{database}/capabilities` | SQL, transaction, schema, and metadata-object capabilities of the specified runtime database. | Determine available operations and limits for one database.              |

When a client cannot choose a database, schema, table, column, algorithm, storage unit, or workflow `plan_id`, call `completion/complete` for one argument at a time.
If completion reports missing context or no candidates, follow the returned meta `next_actions`; those actions usually point to the nearest resource or resource template to read before retrying completion.

## Resources

| Resource URI or template                                                                 | Purpose                                                                                                                                    |
|------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `shardingsphere://capabilities`                                                          | Reads available tasks and side-effect notes for the MCP Server.                                                                            |
| `shardingsphere://runtime`                                                               | Reads the current transport, runtime status, and configured runtime database summary.                                                      |
| `shardingsphere://databases`                                                             | Lists runtime databases reachable by the current MCP Server. When connected to Proxy, they correspond to ShardingSphere logical databases. |
| `shardingsphere://databases/{database}`                                                  | Reads one runtime database and its metadata summary.                                                                                       |
| `shardingsphere://databases/{database}/capabilities`                                     | Reads SQL, transaction, schema, and metadata-object capabilities for one runtime database.                                                 |
| `shardingsphere://databases/{database}/schemas`                                          | Lists schemas or namespaces inside one runtime database.                                                                                   |
| `shardingsphere://databases/{database}/schemas/{schema}`                                 | Reads one schema or namespace.                                                                                                             |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences`                       | Lists sequences in one schema.                                                                                                             |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`            | Reads one sequence.                                                                                                                        |
| `shardingsphere://databases/{database}/schemas/{schema}/tables`                          | Lists tables in one schema.                                                                                                                |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`                  | Reads one table.                                                                                                                           |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`          | Lists columns for one table.                                                                                                               |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | Reads one table column.                                                                                                                    |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`          | Lists indexes for one table.                                                                                                               |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`  | Reads one table index.                                                                                                                     |
| `shardingsphere://databases/{database}/schemas/{schema}/views`                           | Lists views in one schema.                                                                                                                 |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`                    | Reads one view.                                                                                                                            |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`            | Lists columns for one view.                                                                                                                |
| `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`   | Reads one view column.                                                                                                                     |
| `shardingsphere://workflows/{plan_id}`                                                   | Reads the current governance change plan, clarification questions, artifacts, and next actions.                                            |

Workflow resources and workflow tools include a short `summary` plus structured `next_actions` so clients can continue preview, apply, manual execution, validation, or recovery without reading every nested field first.

## Tools

| Tool                                           | Purpose                                                                                                                  | Side effect                                                                                |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| `database_gateway_search_metadata`             | Search runtime database metadata by name fragment and object type, and return resource hints for follow-up reads.        | None.                                                                                      |
| `database_gateway_validate_runtime_database` | Validate whether runtime database configuration is reachable, mainly for diagnosing integration failures.                | None.                                                                                      |
| `database_gateway_execute_query`               | Execute a statement classified as query, such as `SELECT`.                                                               | None. Rejects DML, DDL, DCL, transaction control, savepoint, and other side-effecting SQL. |
| `database_gateway_execute_explain_query`       | Execute a model-generated database-native `EXPLAIN` for one classifier-approved `SELECT`.                                | None. Rejects `EXPLAIN ANALYZE`, `EXPLAIN PLAN FOR`, multiple statements, and side-effecting SQL. |
| `database_gateway_execute_update`              | Preview or execute SQL that may change data, metadata, rules, or transaction state.                                      | Yes. Preview and confirmation are recommended.                                             |
| `database_gateway_apply_workflow`              | Preview, execute, or export a governance change plan created by a feature plugin.                                        | Depends on the execution choice. Preview and manual packages do not change runtime state.  |
| `database_gateway_validate_workflow`           | After rule change execution, validate rule state or workflow execution results according to the feature plugin boundary. | None.                                                                                      |

Additional tools provided by feature plugins are documented on the corresponding plugin pages.

## Prompts

| Prompt               | Purpose                                                                                                      |
|----------------------|--------------------------------------------------------------------------------------------------------------|
| `inspect_metadata`   | Guides metadata inspection tasks to read database metadata before choosing a search tool or detail resource. |
| `safe_sql_execution` | Guides SQL execution tasks to distinguish read-only queries from side-effecting SQL.                         |
| `recover_workflow`   | Guides recovery or re-planning after rule change failure.                                                    |

## Completion Targets

### Resource Completion Targets

| Target                                                                                   | Completion parameters                   |
|------------------------------------------------------------------------------------------|-----------------------------------------|
| `shardingsphere://databases/{database}`                                                  | `database`                              |
| `shardingsphere://databases/{database}/schemas`                                          | `database`                              |
| `shardingsphere://databases/{database}/schemas/{schema}`                                 | `database`, `schema`                    |
| `shardingsphere://databases/{database}/schemas/{schema}/tables`                          | `database`, `schema`                    |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`                  | `database`, `schema`, `table`           |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`          | `database`, `schema`, `table`           |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}` | `database`, `schema`, `table`, `column` |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`          | `database`, `schema`, `table`           |
| `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`  | `database`, `schema`, `table`, `index`  |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences`                       | `database`, `schema`                    |
| `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`            | `database`, `schema`, `sequence`        |
| `shardingsphere://workflows/{plan_id}`                                                   | `plan_id`                               |

### Prompt Completion Targets

| Target               | Completion parameters |
|----------------------|-----------------------|
| `inspect_metadata`   | `database`, `schema`  |
| `safe_sql_execution` | `database`, `schema`  |
| `recover_workflow`   | `plan_id`             |

## HTTP Debugging Examples

The following examples are for developers debugging HTTP transport. They are not the normal usage flow.

Initialize a session:

```bash
curl -i -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"curl-client","version":"1.0.0"}}}'
```

Read the database list:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: demo-session-id' \
  -H 'MCP-Protocol-Version: 2025-11-25' \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases"}}'
```

Call the metadata search tool:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H 'MCP-Session-Id: demo-session-id' \
  -H 'MCP-Protocol-Version: 2025-11-25' \
  --data '{
    "jsonrpc":"2.0",
    "id":"tool-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_search_metadata",
      "arguments":{
        "database":"logic_db",
        "query":"orders",
        "object_types":["table","view"]
      }
    }
  }'
```
