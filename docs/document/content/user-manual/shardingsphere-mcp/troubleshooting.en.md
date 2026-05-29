+++
title = "Troubleshooting"
weight = 7
+++

This page organizes troubleshooting by symptom for the MCP Server, transport, sessions, SQL tools, and workflow mechanism.
For feature-specific business rule issues, see the corresponding feature plugin documentation.

## Troubleshooting index

| Symptom | Possible cause | Action | Needs code improvement |
| --- | --- | --- | --- |
| Startup failure | JDK, config path, YAML field, or required field is wrong. | Inspect terminal output and `logs/mcp.log`. | Partially: optional field support can be improved. |
| HTTP connection failure | Port, endpoint path, transport type, or bind address is wrong. | Check `port`, `endpointPath`, `bindHost`, and client URL. | Usually no. |
| HTTP 403 response | Request `Origin` does not match the bind-address policy. | Use loopback locally; use a controlled gateway for remote access. | Yes: error response can give clearer hints. |
| Session request failure | Session was not initialized, headers are missing, or a closed session is reused. | Call `initialize` first and keep sending the response headers. | Usually no. |
| No response in STDIO mode | STDIO is used as a shell, or stdout is polluted by logs. | Let an MCP client launch the process; read stderr or logs for diagnostics. | Yes: keep protecting stdout. |
| Logical database or metadata is empty | Config, driver, or permission is wrong. | Confirm Proxy logical database, driver, and permissions. | Yes: empty results can include diagnostics. |
| JDBC driver error | Driver is not on classpath, or `driverClassName` is wrong. | Put the driver jar under `plugins/`, or add it to the embedded runtime classpath. | Partially: optional field support can be improved. |
| SQL tool call failure | Wrong tool, multiple statements, or argument out of range. | Use `execute_query` for queries; use `execute_update` with preview for side effects. | Usually no; messages can improve. |
| Workflow failure | `plan_id`, session, execution mode, or manual step is wrong. | Reuse `plan_id` in one session; preview first; validate after manual execution. | Usually no. |
| Secret input cannot be passed safely | A clarification asks for a key or credential. | Resolve it outside the server, then pass it through a protected MCP call. | Server-side secret references require code changes. |

Additional notes:

- `username`, `password`, and `driverClassName` must currently be declared explicitly; use `""` when no value is needed.
- `MCP-Session-Id` and `MCP-Protocol-Version` come from the `initialize` response headers and cannot be reused after close.
- After `manual-only`, execute the returned SQL or DistSQL manually before calling validation.
- Secret placeholders in manual packages should be replaced by operators in a controlled environment.

## SQL tool selection

| SQL type | Tool | Recommendation |
| --- | --- | --- |
| `SELECT` | `database_gateway_execute_query` | Use for read-only queries. |
| `EXPLAIN ANALYZE` | `database_gateway_execute_query` | Use only when the target logical database capability allows it. |
| DML, DDL, DCL, transaction control, savepoint | `database_gateway_execute_update` | Preview with `execution_mode=preview` before deciding whether to execute. |

Preview parameter for `database_gateway_execute_update`:

```json
{"execution_mode":"preview"}
```

Execute after review:

```json
{"execution_mode":"execute"}
```

## Diagnostics

When reporting an issue, provide:

- Startup command.
- MCP configuration file, with passwords, keys, and tokens removed.
- Transport type and endpoint.
- Whether `initialize` has completed. Do not publish a real `MCP-Session-Id`.
- Tool or resource request body.
- JSON-RPC error payload.
- Relevant errors from `logs/mcp.log`.
