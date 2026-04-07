# Quickstart: ShardingSphere MCP V1

## Purpose

Provide the expected developer and reviewer flow for building, packaging, configuring, and smoke-validating the MCP subchain runtime that is currently implemented in this repository.

## Prerequisites

- JDK 17 toolchain available for the MCP build lane
- Maven wrapper available from the repository root
- A configuration source or test fixture for ShardingSphere metadata

## 1. Build the MCP code chain

From the repository root, run the MCP-only build path:

```bash
./mvnw -pl mcp -am test -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false
```

Expected outcome:

- `mcp/core` and `mcp/bootstrap` compile on JDK 17
- MCP participates in the standard reactor and can still be built with module-scoped commands

## 2. Package the standalone distribution

Build the distribution artifact:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Expected packaged layout:

```text
apache-shardingsphere-mcp-<version>/
├── bin/
├── conf/
├── lib/
├── logs/
└── LICENSE / NOTICE / README.md
```

## 3. Configure runtime

Prepare `conf/mcp.yaml` with at least:

- `transport`
  - `http.enabled`
  - `http.bindHost`
  - `http.port`
  - `http.endpointPath`
  - `stdio.enabled`
- `runtimeDatabases`
  - one or more logical database definitions

## 4. Start the service

After unpacking the distribution, start the MCP runtime:

```bash
bin/start.sh
```

Operational expectations:

- Startup reads `conf/mcp.yaml` and applies server and transport settings before wiring the runtime
- Local mode binds to `127.0.0.1` by default
- The built-in runtime focuses on session lifecycle and local runtime boundary checks
- If the HTTP endpoint is exposed outside a trusted network, place it behind an external gateway or reverse proxy
- Streamable HTTP lifecycle endpoint is exposed at the configured path
- The packaged runtime exposes JSON-RPC `initialize`, session-bound `tools/*` and `resources/*`, SSE stream opening, and explicit session close over the standalone HTTP listener
- The packaged distribution enables HTTP by default and keeps STDIO disabled unless explicitly turned on for local debugging
- `bin/start.sh` is a packaged-distribution entrypoint; local source-tree debugging should use tests or a dedicated dev launcher instead of the release script

## 5. Run transport smoke checks

Validate the transport contract:

1. Send a JSON-RPC `initialize` request and confirm the response returns `MCP-Session-Id`.
2. Confirm the initialize response also returns `MCP-Protocol-Version`.
3. Call a follow-up JSON-RPC `tools/call` or `resources/read` request with `MCP-Session-Id` and verify dispatch succeeds.
4. Open `GET /mcp` with `MCP-Session-Id` and confirm SSE compatibility.
5. Verify follow-up requests without `MCP-Session-Id` are rejected.
6. Call `DELETE /mcp` with `MCP-Session-Id` and verify the session closes cleanly.
7. Optionally confirm omitted `MCP-Protocol-Version` falls back to the negotiated session version.
8. In local mode, verify a non-loopback `Origin` is rejected.

For local debugging, use the in-process STDIO integration path to repeat the same capability and tool smoke cases.

## 6. Run feature smoke scenarios

The minimum in-process smoke suite should cover:

1. Service-level capability retrieval
2. Database-level capability retrieval
3. `shardingsphere://databases`
4. `shardingsphere://databases/{database}/schemas`
5. `shardingsphere://databases/{database}/schemas/{schema}/tables`
6. `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`
7. `search_metadata`
8. `execute_query(SELECT)`
9. `execute_query(DML)`
10. `BEGIN / COMMIT / ROLLBACK`
11. Successful `SAVEPOINT`
12. `SAVEPOINT unsupported`
13. `DDL / DCL refresh visibility and audit capture`

## 7. Run E2E validation

Execute the MCP E2E module:

```bash
./mvnw -pl test/e2e/mcp -am test -Dsurefire.failIfNoSpecifiedTests=false
```

The E2E matrix should include:

- Databases that support transactions and savepoints
- Databases that support transactions but not savepoints
- Databases that do not support transaction control
- One database that supports `index`
- One database that does not support `index`
- Audit and refresh-visibility cases for DDL / DCL
