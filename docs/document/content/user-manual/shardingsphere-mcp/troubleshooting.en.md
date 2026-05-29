+++
title = "Troubleshooting"
weight = 6
+++

This page covers common MCP Server, transport, session, SQL tool, and workflow mechanism issues.
For plugin-specific business issues, see the corresponding feature plugin documentation.

## Startup failure

Check:

- JDK version is JDK 21.
- The configuration file path is correct.
- `conf/mcp-http.yaml` or `conf/mcp-stdio.yaml` exists.
- The YAML file does not contain unsupported fields.
- `username`, `password`, and `driverClassName` are explicitly declared; use an empty string `""` when no value is needed.

When startup fails, inspect the terminal error and `logs/mcp.log` first.

## HTTP connection failure

Check:

- The default endpoint is `http://127.0.0.1:18088/mcp`.
- The port is not occupied.
- `transport.type` is `STREAMABLE_HTTP`.
- `transport.http.endpointPath` matches the client URL.
- Remote machines cannot access a runtime bound to `127.0.0.1` directly.
- When binding to `0.0.0.0`, authentication and network access control should be handled by an upstream gateway.

## HTTP 403 response

Origin validation rules:

- For loopback bindings, if the request carries `Origin`, the Origin must also be loopback.
- For non-loopback bindings, non-browser requests without `Origin` are accepted.
- For non-loopback bindings, any explicit `Origin` is rejected.

Possible actions:

- Adjust the client Origin behavior.
- Use local loopback access.
- Forward requests through a controlled gateway.

## Session or protocol header issues

After an HTTP client calls `initialize`, it must keep:

- `MCP-Session-Id`
- `MCP-Protocol-Version`

Later requests must include both headers.
After the session is closed, it cannot be reused.
Workflow `plan_id` values are valid only in the current session.

## No response in STDIO mode

STDIO is for MCP clients that launch the runtime as a child process. It is not an interactive shell.

Check:

- `command` points to `bin/start.sh` or `bin\start.bat`.
- `args` includes `conf/mcp-stdio.yaml`.
- stdout is not polluted by logs.
- Diagnostics are read from stderr or `logs/mcp.log`.

## Logical database not found or metadata is empty

Check:

- `runtimeDatabases` contains the target logical database.
- MCP exposes ShardingSphere logical databases, not physical storage units.
- `databaseType` and `jdbcUrl` match the target logical database.
- The target JDBC driver jar is under `plugins/`.
- The connection user has permission to read JDBC metadata.

## JDBC driver errors

The distribution only packages a limited set of JDBC drivers.
If the target database driver is missing, place the driver jar under `plugins/`.

If embedding `shardingsphere-mcp-bootstrap` directly, add the driver to the runtime classpath.

The `driverClassName` field must be explicitly present.
When the driver auto-registers and no explicit override is needed, set it to `""`.

## SQL tool call failure

`database_gateway_execute_query` is for:

- `SELECT`
- `EXPLAIN ANALYZE`

Use `database_gateway_execute_update` for DML, DDL, DCL, transaction control, savepoints, and other side-effecting SQL.

For side-effecting SQL, preview first:

```json
{"execution_mode":"preview"}
```

Then execute after review:

```json
{"execution_mode":"execute"}
```

Other limits:

- Multiple statements are rejected.
- `max_rows` range is `0..5000`.
- `timeout_ms` range is `0..300000`.

## Common workflow issues

Check:

- `plan_id` was not lost.
- The same `MCP-Session-Id` is used for apply and validate.
- `database_gateway_apply_workflow` includes `execution_mode`.
- After `manual-only`, the returned SQL or DistSQL artifacts have been executed manually.
- `approved_steps` values come from `preview_artifacts[].approval_step`.

## Collect diagnostics

When reporting an issue, provide:

- Startup command.
- MCP configuration file, with passwords and keys removed.
- Transport type and endpoint.
- Whether `MCP-Session-Id` was initialized. Do not publish sensitive real headers.
- Tool or resource request body.
- JSON-RPC error payload.
- Relevant errors from `logs/mcp.log`.
