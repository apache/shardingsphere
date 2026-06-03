+++
title = "Troubleshooting"
weight = 7
+++

This page organizes troubleshooting by user-visible symptoms for ShardingSphere-MCP, AI application integration, database connectivity, metadata inspection, queries, and rule changes.
For feature-specific business rule issues, see the corresponding feature plugin documentation.

## Troubleshooting Index

| Symptom | Possible cause | Action |
| --- | --- | --- |
| MCP Server startup failure | JDK, config path, YAML field, or required field is wrong. | Inspect the startup terminal and `logs/mcp.log`; confirm the configuration file path and required fields. |
| The AI application cannot connect to ShardingSphere-MCP | Port, endpoint path, transport type, bind address, or client configuration is wrong. | Check `port`, `endpointPath`, `bindHost`, and the MCP Server address configured in the AI application. |
| Remote HTTP access fails | HTTP bind address, security policy, or gateway configuration is wrong. | Use loopback locally; place remote access behind a controlled gateway or reverse proxy; inspect server logs for details. |
| No response in STDIO mode | STDIO is used as an interactive command-line entry, or the AI application does not launch the process correctly. | Let the AI application launch ShardingSphere-MCP; inspect stderr or `logs/mcp.log` for diagnostics. |
| Logical databases are not visible | No logical database is configured, the logical database name is wrong, connection failed, permission is insufficient, or the target scope is empty. | Verify `runtimeDatabases`, logical database names, connection failure category, and account privileges. |
| Tables, columns, or indexes cannot be found | The connection target is different, schema is wrong, account privileges are insufficient, or Proxy-visible logical metadata differs from the physical database. | Confirm whether the target is ShardingSphere-Proxy or a regular database, then check schema, account privileges, and Proxy-visible metadata. |
| JDBC driver error | Driver is not on the classpath, or `driverClassName` is wrong. | Put the driver jar under `plugins/`, and keep `driverClassName` non-empty and correct. |
| Read-only query fails | SQL syntax, target table name, schema, privilege, or row limit is wrong. | Ask the AI application to inspect table structure first, then run a read-only query with a row limit. |
| Side-effecting SQL cannot be executed | The SQL has side effects, or it was not previewed and confirmed first. | Preview the change SQL first, review the impact, then confirm execution. |
| Encryption or masking plan cannot be generated | The target is not Proxy, the target column is not visible, the algorithm is unavailable, or required parameters are missing. | Ensure that `runtimeDatabases` points to a Proxy logical database, then provide logical database, table, column, algorithm, and parameters. |
| Validation fails after a rule change | The rule was not applied successfully, permission is insufficient, metadata is not refreshed, or a manual package was not executed. | Inspect the rule change plan, execution result, and logs; validate again after manual execution is completed. |
| Secret input cannot be passed safely | A rule change needs a key or credential. | Resolve the value through the client, a key management system, or an operations control channel, then pass it through a protected call or manual execution step. |

Additional notes:

- `username` and `driverClassName` must be declared explicitly and cannot be empty; a no-password account can omit `password` or use `""`.
- Secret placeholders in manual packages should be replaced by operators in a controlled environment.
- For protocol request debugging, see the [Custom Integration Appendix](../developer-appendix/).

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

## Query and Change Recommendations

| Scenario | Recommendation |
| --- | --- |
| Query data | Limit returned rows. |
| Analyze an SQL execution plan | Use only when the target logical database capability allows it. |
| Change data, structure, rules, or transaction state | Preview and review side effects before deciding whether to execute. |

## Information for Administrators or Troubleshooters

When reporting an issue, provide:

- Startup command.
- MCP configuration file, with passwords, keys, and tokens removed.
- Transport type and endpoint.
- MCP Server configuration summary from the AI application.
- The natural-language task entered by the user.
- Error response content, including the failure category and guidance.
- Relevant errors from `logs/mcp.log`.
