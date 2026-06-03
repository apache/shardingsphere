+++
title = "Troubleshooting"
weight = 7
+++

This page organizes troubleshooting by symptom for the MCP Server, transport, sessions, SQL tools, and workflow mechanism.
For feature-specific business rule issues, see the corresponding feature plugin documentation.

## Troubleshooting index

| Symptom | Possible cause | Action |
| --- | --- | --- |
| Startup failure | JDK, config path, YAML field, or required field is wrong. | Inspect terminal output and `logs/mcp.log`. |
| HTTP connection failure | Port, endpoint path, transport type, or bind address is wrong. | Check `port`, `endpointPath`, `bindHost`, and client URL. |
| HTTP 403 response | Request `Origin` does not match the bind-address policy. | Use loopback locally; use a controlled gateway for remote access; inspect server logs for the safe reason category. |
| Session request failure | Session was not initialized, headers are missing, or a closed session is reused. | Call `initialize` first and keep sending the response headers. |
| No response in STDIO mode | STDIO is used as a shell, or the client does not send JSON-RPC over MCP stdio. | Let an MCP client launch the process; read stderr or logs for diagnostics. |
| Logical database or metadata is empty | No logical database is configured, the logical database name is wrong, connection failed, permission is insufficient, or the target scope is empty. | Check runtime status, then verify database configuration, logical database name, connection failure category, and account privileges. |
| JDBC driver error | Driver is not on classpath, or `driverClassName` is wrong. | Put the driver jar under `plugins/`, and keep `driverClassName` non-empty and correct. |
| SQL tool call failure | Wrong tool, multiple statements, or argument out of range. | Use `execute_query` for queries; use `execute_update` with preview for side effects. |
| Workflow failure | `plan_id`, session, execution mode, or manual step is wrong. | Reuse `plan_id` in one session; preview first; validate after manual execution. |
| Secret input cannot be passed safely | A clarification asks for a key or credential. | Resolve the value through the client, a key management system, or an operations control channel, then pass it through a protected call or manual execution step. |

Additional notes:

- `username` and `driverClassName` must be declared explicitly and cannot be empty; a no-password account can omit `password` or use `""`.
- `MCP-Session-Id` and `MCP-Protocol-Version` come from the `initialize` response headers and cannot be reused after close.
- After `manual-only`, execute the returned SQL or DistSQL manually before calling validation.
- Secret placeholders in manual packages should be replaced by operators in a controlled environment.

## Connection Failure Categories

When a runtime database or ShardingSphere-Proxy connection fails, MCP responses return a connection failure category to help locate the issue. Categories describe the failure cause without exposing JDBC URLs, passwords, environment variables, or stack traces.

| Category | Meaning |
| --- | --- |
| `missing_jdbc_driver` | The configured JDBC driver cannot be found. |
| `authentication_failed` | Username or password authentication failed. |
| `authorization_failed` | The current account does not have permission to access the target database or metadata. |
| `connection_timeout` | The connection timed out. Check the address, port, network, or timeout settings. |
| `invalid_configuration` | Runtime database configuration is incomplete or inconsistent. |
| `database_unavailable` | The target database or ShardingSphere-Proxy is currently unavailable. |
| `connection_failed` | The connection failed, but cannot be classified into a more specific cause. |
| `database_not_visible` | The specified logical database is not visible to the current connection. |

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
- Error response content, including the failure category and guidance.
- Relevant errors from `logs/mcp.log`.
