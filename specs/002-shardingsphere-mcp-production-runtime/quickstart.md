# Quickstart: ShardingSphere MCP Production Runtime Integration

## Purpose

Provide the expected operator and reviewer flow for validating that the packaged MCP distribution no longer stops at the transport shell and can reach a real ShardingSphere-backed runtime path.

## Prerequisites

- JDK 17 toolchain available for the MCP build lane
- Maven wrapper available from the repository root
- A real ShardingSphere-backed metadata source reachable from the MCP runtime
- Provider-specific runtime dependencies available to the packaged distribution, or copied under `ext-lib/`
- A trusted network boundary, gateway, or reverse proxy if the HTTP endpoint will be exposed
- One MCP-capable host or client for registration and smoke validation

## 1. Build the standalone distribution

From the repository root, build the packaged MCP runtime:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Expected outcome:

- The standalone MCP distribution is produced under the distribution build output.
- Packaging includes scripts, configuration, logs directory, and provider-ready runtime dependencies.

## 2. Prepare production runtime configuration

Review `conf/mcp.yaml` so production startup declares a concrete runtime provider instead of relying on an implicit empty runtime.

The production configuration must define:

- `server`
  - bind host
  - port
  - endpoint path
- `transport`
  - HTTP enablement
  - STDIO enablement for local debugging only, even though the packaged distribution keeps it enabled by default
- `runtime`
  - provider type
  - provider properties needed to locate shared metadata and real execution entrypoints
  - optional database scope or deployment overrides

Operational expectations:

- Missing provider type or required provider properties must fail startup.
- Missing runtime dependencies must fail startup.
- Unreachable metadata sources or unsupported topologies must fail startup.
- A successful startup means both real metadata discovery and real execution wiring are ready.

## 3. Start the packaged service

After unpacking the distribution, start the MCP runtime:

```bash
bin/start.sh
```

Expected startup behavior:

- Startup validates provider configuration before the HTTP listener is published.
- The service must not start successfully with an empty `MetadataCatalog` or empty `DatabaseRuntime` as a production fallback.
- Local mode continues to bind `127.0.0.1` by default unless the operator explicitly configures another trusted deployment boundary.
- The packaged demo configuration is allowed to start against the bundled H2-backed `SHARDINGSPHERE_JDBC` provider for first-run validation.

## 4. Validate initialization and session negotiation

Use the packaged README smoke flow to confirm:

1. `POST /mcp` with `initialize` returns `MCP-Session-Id`.
2. The initialize response also returns `MCP-Protocol-Version`.
3. Follow-up requests with the returned session id succeed.
4. Follow-up requests without the session id are rejected.
5. `DELETE /mcp` closes the session cleanly.

This step proves the transport shell is alive, but not yet that production runtime integration is real.

## 5. Validate real metadata discovery

Before declaring the runtime usable, confirm at least the following:

1. `list_databases` returns one or more logical databases from the real runtime scope.
2. `get_capabilities(database)` returns a database-level capability assembled from matrix defaults, runtime metadata, and deployment overrides.
3. `list_tables(database, schema)` or `search_metadata` returns real metadata objects rather than an empty fallback.
4. `describe_table(database, schema, table)` returns real column details.

If the runtime actually contains visible logical databases but these calls return empty success payloads, the deployment is not production-ready.

## 6. Validate real SQL execution

Use one known logical database and run at least:

1. `execute_query(database, "SELECT ...")`
2. One DML statement if the selected database capability allows it
3. `BEGIN` and `COMMIT` when transaction control is supported
4. `SAVEPOINT` or an `unsupported` response consistent with capability

Expected outcome:

- Query results use the unified MCP result models.
- Unsupported statements still return unified MCP errors.
- Active transactions remain bound to one logical database.

## 7. Validate metadata refresh behavior

When the selected capability allows DDL or DCL through MCP:

1. Execute one metadata-affecting change.
2. Re-read the affected object in the same session and confirm immediate visibility.
3. Confirm global visibility converges within the documented 60-second SLA.

## 8. Register the endpoint in an MCP host

Register the packaged HTTP endpoint in the chosen MCP-capable host or agent runtime after replacing the demo runtime block with target deployment settings when needed.

Registration expectations:

- The host is configured manually with the MCP HTTP endpoint.
- The host does not discover the endpoint by network scanning.
- After registration and `initialize`, the host automatically discovers non-empty tools and resources from the MCP server.

## 9. Run production acceptance validation

The minimum acceptance path for this follow-up is:

1. Build and start the packaged distribution
2. Complete initialize and session negotiation
3. Confirm non-empty metadata discovery
4. Confirm at least one real `execute_query` success
5. Confirm one transaction-control or `unsupported` case
6. Confirm session close via `DELETE /mcp`

This quickstart is complete only when the packaged runtime proves real metadata and real execution behavior, not merely a reachable HTTP endpoint.
