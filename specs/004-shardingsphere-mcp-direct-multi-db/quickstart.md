# Quickstart: ShardingSphere MCP Direct Multi-Database Runtime

## 1. Goal

验证一个 MCP 服务实例可以直接接入多个独立数据库，并在保持现有 MCP V1 公共契约的前提下完成 discovery、
execution、单数据库事务隔离和单库故障隔离。

## 2. Prerequisites

- JDK 17 toolchain available for the MCP build lane
- Maven wrapper available from the repository root
- At least two reachable databases from the V1 supported set
- Required JDBC driver jars available to the packaged distribution, or copied under `ext-lib/`
- One trusted deployment boundary if HTTP will be exposed
- One MCP-capable host or client for smoke validation

## 3. Build the standalone distribution

From the repository root, build the packaged MCP runtime:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Expected outcome:

- The standalone MCP distribution is produced under the distribution build output.
- Packaging includes scripts, configuration, `ext-lib/`, and runtime libraries needed for direct JDBC-backed startup.

## 4. Prepare multi-database runtime configuration

Review `conf/mcp.yaml` so runtime configuration declares multiple logical databases rather than a single direct database binding.

The configuration must define at least:

- `server`
  - bind host
  - port
  - endpoint path
- `transport`
  - HTTP enablement
  - STDIO enablement
- `runtime`
  - one or more logical database bindings
  - per-binding database type
  - per-binding backend locator and credentials
  - per-binding unified schema semantics when needed

Operational expectations:

- Duplicate logical database names must fail startup.
- Unsupported database types must fail startup.
- Missing JDBC drivers must fail startup.
- Initial metadata load failure for any configured logical database must fail startup.

## 5. Start the packaged service

After unpacking the distribution, start the MCP runtime:

```bash
bin/start.sh
```

Expected startup behavior:

- Startup validates the full logical database topology before the MCP endpoint is published.
- A successful startup means every configured logical database completed its initial metadata load.
- A failed startup means no partially valid endpoint is published.

## 6. Validate initialization and session negotiation

Use the packaged README smoke flow to confirm:

1. `POST /mcp` with `initialize` returns `MCP-Session-Id`
2. The initialize response returns `MCP-Protocol-Version`
3. Follow-up requests with the session id succeed
4. Follow-up requests without the session id are rejected
5. `DELETE /mcp` closes the session cleanly

This step proves the transport shell is alive and that the runtime topology has already passed startup validation.

## 7. Validate discovery across multiple logical databases

Confirm at least the following:

1. `list_databases` returns all configured logical database names
2. `get_capabilities(database)` succeeds for each logical database
3. `list_tables(database, schema)` or `search_metadata` returns objects from more than one database
4. `describe_table(database, schema, table)` resolves correctly for at least two logical databases

If `search_metadata` is called without `database`, it should search across all loaded logical databases.

## 8. Validate routed SQL execution

For at least two logical databases, run:

1. `execute_query(database, "SELECT ...")`
2. One DML statement when capability allows it
3. One `BEGIN` plus `COMMIT` or `ROLLBACK` path when transaction control is supported
4. One unsupported statement path

Expected outcome:

- Each request stays routed to its target logical database
- Result objects stay within the existing MCP V1 result models
- Unsupported paths still return the unified MCP error semantics

## 9. Validate cross-database transaction isolation

Use one session and confirm:

1. Start a transaction on logical database A
2. Attempt to execute SQL on logical database B in the same session
3. Confirm the request returns `conflict` or `transaction_state_error`
4. Confirm no cross-database execution occurs

## 10. Validate targeted metadata refresh

When the selected capability allows DDL or DCL through MCP:

1. Execute one metadata-affecting change on logical database A
2. Re-read the affected metadata in the same session and confirm immediate visibility
3. Confirm global visibility converges within the 60-second SLA
4. Confirm logical database B still exposes its previous snapshot and is not force-refreshed by A's change

## 11. Validate single-database outage isolation

After successful startup, make one logical database temporarily unreachable.

Expected outcome:

1. Requests targeting healthy logical databases continue to work
2. Requests needing live backend access on the failed logical database return `unavailable`
3. Read-only discovery for the failed logical database continues from the last successful metadata snapshot
4. Restart is not required for unrelated logical databases to keep serving

## 12. Reviewer checklist

- Does startup fail before publishing the endpoint when any configured logical database is invalid?
- Does `list_databases` remain stable after successful startup?
- Is cross-database transaction switching still rejected?
- Are DDL / DCL refreshes isolated to the target logical database?
- Do runtime diagnostics and audit outputs remain secret-safe?
