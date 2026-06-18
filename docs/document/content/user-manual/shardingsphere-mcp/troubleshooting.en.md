+++
title = "Troubleshooting"
weight = 7
+++

This page organizes troubleshooting by user-visible symptoms for ShardingSphere-MCP, AI application integration, database connectivity, metadata inspection, queries, and rule changes.
For feature-specific rule planning, execution, and validation issues, see the corresponding feature plugin documentation.
When troubleshooting, distinguish external environment issues from MCP protection behavior. Database service availability, account privileges, gateway forwarding, and AI application configuration must be fixed in their own systems. MCP provides failure categories and runtime protection details to help locate the issue.
If you have not completed the basic post-deployment checks yet, start with the health-check and observability entrypoints in [Deployment](../deployment/) before using this symptom-oriented page.

## Issue List

| Symptom                                                  | Possible cause                                                                                                                                                                                                             | Action                                                                                                                                                                                                                                    |
|----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MCP Server startup failure                               | Java version, config file path, distribution directory, or required YAML field is wrong.                                                                                                                                   | Inspect the startup terminal and `logs/mcp.log`; use Java 21 or later, confirm that the config file exists, ensure that the distribution `lib/` directory is complete, and configure at least one runtime database in `runtimeDatabases`. |
| The AI application cannot connect to ShardingSphere-MCP  | Transport type, port, endpoint path, bind address, or the MCP Server configuration in the AI application is inconsistent.                                                                                                  | Check `transport.type`, `port`, `endpointPath`, `bindHost`, and the address configured in the AI application.                                                                                                                             |
| Remote HTTP access fails or the HTTP request is rejected | HTTP bind address, Origin header, reverse proxy, gateway forwarding, or session attribution configuration does not satisfy the security policy.                                                                            | Use loopback for local debugging; place remote access behind a controlled gateway or reverse proxy; check whether the gateway forwards the expected headers; inspect server logs for details.                                             |
| No response in STDIO mode                                | STDIO is used as an interactive command-line entry, or the AI application does not launch the MCP process correctly.                                                                                                       | Let the AI application launch ShardingSphere-MCP; inspect stderr or `logs/mcp.log` for diagnostics.                                                                                                                                       |
| Databases or logical databases are not visible           | The name in `runtimeDatabases` is wrong, connection failed, privileges are insufficient, or the target scope is empty.                                                                                                     | Read the currently visible databases; if a connection failure category is returned, follow the connection failure table below; confirm that the account can read metadata.                                                                |
| Tables, columns, or indexes cannot be found              | The connection target is different, schema or namespace is wrong, account privileges are insufficient, or Proxy-visible logical metadata differs from the underlying physical database.                                    | Confirm whether the target is ShardingSphere-Proxy or a direct database connection, then check schema, namespace, account privileges, and Proxy-visible metadata.                                                                         |
| Query is rejected                                        | The SQL is not read-only, or it contains locking reads, data changes, schema changes, privilege changes, or transaction control.                                                                                           | Use query statements for read-only tasks. For side-effecting tasks, preview the change first and then decide whether to execute it.                                                                                                       |
| Query execution fails or the result is truncated         | SQL syntax, target object, privilege, row limit, or timeout limit is wrong.                                                                                                                                                | Inspect table structure first; narrow the predicate, reduce the projection, request fewer rows, or adjust timeout within the supported range.                                                                                             |
| Side-effecting SQL cannot be executed                    | The change SQL was not previewed and confirmed, or the current account lacks execution privileges.                                                                                                                         | Preview the change SQL, review the impact, and then confirm execution; privilege issues require administrator action.                                                                                                                     |
| Rule change planning or validation fails                 | The target is not ShardingSphere-Proxy, the target column is not visible, the algorithm is unavailable, required parameters are missing, the rule was not applied successfully, or a manual package has not been executed. | Ensure that `runtimeDatabases` points to a Proxy logical database; provide the rule target, algorithm, and parameters according to the feature plugin documentation; validate again after manual execution is completed.                  |

Additional notes:

- `username` and `driverClassName` must be declared explicitly and cannot be empty; a no-password account can omit `password` or use `""`.
- `runtimeDatabases` is required at startup and must contain at least one runtime database.
- Queries return at most 100 rows by default. A single query can request at most 5000 rows, and the maximum requested query timeout is 300000 milliseconds.
- Secret placeholders in manual packages should be replaced by operators in a controlled environment.
- ShardingSphere-MCP does not fetch real sensitive values; real sensitive values must stay outside the AI application. If a rule change needs keys or credentials, replace the neutral placeholders in the manual execution package outside MCP and the AI application in a controlled environment.
- For protocol request debugging, see the [Custom Integration Appendix](../developer-appendix/).

## Connection Failure Categories

When a runtime database or ShardingSphere-Proxy connection fails, MCP responses return a connection failure category to help locate the issue. Categories describe the failure cause without exposing JDBC URLs, passwords, environment variables, or stack traces.

| Category                | Meaning                                                                                 |
|-------------------------|-----------------------------------------------------------------------------------------|
| `missing_jdbc_driver`   | The configured JDBC driver cannot be found.                                             |
| `authentication_failed` | Username or password authentication failed.                                             |
| `authorization_failed`  | The current account does not have permission to access the target database or metadata. |
| `connection_timeout`    | The connection timed out. Check the address, port, network, or timeout settings.        |
| `invalid_configuration` | Runtime database configuration is incomplete or inconsistent.                           |
| `database_unavailable`  | The target database or ShardingSphere-Proxy is currently unavailable.                   |
| `connection_failed`     | The connection failed, but cannot be classified into a more specific cause.             |
| `database_not_visible`  | The specified database or logical database is not visible to the current connection.    |

## Runtime Protection

| Protection            | Meaning                                                                                                        | Action                                                                                          |
|-----------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| Query row limit       | Queries return at most 100 rows by default, and one query can request at most 5000 rows.                       | When a result is truncated, narrow the predicate, reduce the projection, or request fewer rows. |
| Query timeout         | One query can request at most 300000 milliseconds as the timeout.                                              | After a timeout, narrow the query scope or adjust timeout within the supported range.           |
| Tool-call quota       | When the current MCP session reaches the tool-call protection limit, MCP returns `tool_call_limit_exceeded`.   | Close the current session and create a new MCP session.                                         |
| Side-effect execution | Data, schema, rule, privilege, or transaction-state changes require preview and confirmation before execution. | Review the preview before deciding whether to execute.                                          |

## Information for Administrators or Troubleshooters

When reporting an issue, provide:

- Startup command.
- MCP configuration file, with passwords, keys, and tokens removed.
- Transport type and endpoint.
- MCP Server configuration summary from the AI application.
- The natural-language task entered by the user.
- Error response content, including the failure category and guidance.
- Relevant errors from `logs/mcp.log`.
