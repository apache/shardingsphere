# ShardingSphere MCP

ShardingSphere MCP provides a standalone Model Context Protocol runtime for Apache ShardingSphere.
This quick start is optimized for the first successful local run: build the packaged distribution, start the bundled demo runtime, initialize one session, and verify discovery and query behavior over HTTP.
Additional notes cover how to replace the demo runtime with a real JDBC-backed deployment.

## Quick Start

### Prerequisites

- JDK 17 available from `JAVA_HOME` or `PATH`
- Maven wrapper from the repository root
- A Unix-like shell with `curl`, `find`, `mktemp`, `sed`, and `tr`

### 1. Build the standalone distribution

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Resolve the packaged distribution directory:

```bash
DIST_DIR=$(find distribution/mcp/target -maxdepth 1 -type d -name 'apache-shardingsphere-mcp-*' | sed -n '1p')
echo "${DIST_DIR}"
```

Expected result:

- The command prints one non-empty distribution path.
- The resolved path contains `bin/`, `conf/`, and `lib/`.

### 2. Start the MCP runtime

```bash
cd "${DIST_DIR}"
bin/start.sh
```

Notes:

- `bin/start.sh` runs in the foreground. Keep this terminal open and use a second terminal for the `curl` commands below.
- The packaged runtime reads `conf/mcp.yaml` and `conf/logback.xml`.
- When HTTP is enabled, the default endpoint is `http://127.0.0.1:18088/mcp`.
- Logs are written under `logs/`.
- `conf/mcp.yaml` is now strict about supported field names: `transport.http.enabled`, `transport.http.bindHost`, `transport.http.allowRemoteAccess`, `transport.http.accessToken`, `transport.http.port`, `transport.http.endpointPath`, `transport.stdio.enabled`, and all runtime database fields must be declared with supported keys only.
- Exactly one transport must be enabled per process. The packaged sample configuration enables HTTP only.
- `bin/start.sh` validates the config file, runtime libraries, and Java availability before startup, creates `data/`, `logs/`, and `ext-lib/`, then starts from the package root so relative runtime paths resolve consistently.
- If startup succeeds, the process stays running in the foreground. If it exits immediately, inspect the terminal error and `logs/mcp.log` first.
- The bundled demo runtime exposes two logical databases named `orders` and `billing`, both backed by the packaged H2 driver and seed data under `data/`.

The packaged sample configuration is:

```yaml
transport:
  http:
    enabled: true
    bindHost: 127.0.0.1
    allowRemoteAccess: false
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: false

runtimeDatabases:
  orders:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-orders;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
    username: ""
    password: ""
    driverClassName: org.h2.Driver
  billing:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-billing;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
    username: ""
    password: ""
    driverClassName: org.h2.Driver
```

### 3. Initialize one MCP session

Run the following command in a second terminal:

```bash
INIT_HEADERS=$(mktemp)
curl -sS -D "${INIT_HEADERS}" -o /dev/null http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  --data '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"capabilities":{},"clientInfo":{"name":"curl-demo","version":"1.0.0"}}}'
SESSION_ID=$(sed -n 's/^[Mm][Cc][Pp]-[Ss]ession-[Ii][Dd]: //p' "${INIT_HEADERS}" | tr -d '\r')
PROTOCOL_VERSION=$(sed -n 's/^[Mm][Cc][Pp]-[Pp]rotocol-[Vv]ersion: //p' "${INIT_HEADERS}" | tr -d '\r')
rm -f "${INIT_HEADERS}"
printf 'SESSION_ID=%s\nPROTOCOL_VERSION=%s\n' "${SESSION_ID}" "${PROTOCOL_VERSION}"
```

Expected result:

- The command prints one non-empty session ID and one non-empty protocol version.
- The initialize response negotiates the protocol version and returns it in `MCP-Protocol-Version`.

### 4. Verify discovery and query behavior

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/tables"}}'
```

Expected result:

- The response content type is `text/event-stream`.
- The JSON payload appears on the `data:` line and includes `orders`, `order_items`, and `active_orders`.

Notes:

- Metadata list/detail/capability discovery is unified through `resources/read`.
- The current public tools are limited to `search_metadata` and `execute_query`.
- `search_metadata.object_types` accepts `database`, `schema`, `table`, `view`, `column`, `index`, and `sequence` only.

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
```

Expected result:

- The response content type is `text/event-stream`.
- The JSON payload appears on the `data:` line and includes matching names such as `orders`, `order_items`, or `active_orders`.

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-2","method":"tools/call","params":{"name":"execute_query","arguments":{"database":"orders","schema":"public","sql":"SELECT status FROM orders ORDER BY order_id","max_rows":10}}}'
```

Expected result:

- The response content type is `text/event-stream`.
- The JSON payload appears on the `data:` line and includes `result_kind = result_set`.
- The same payload also includes `statement_class = query`, `statement_type = SELECT`, plus `columns` and `rows`.

Close the session with the DELETE example below when you are done.

## Additional HTTP Verification

### Read `shardingsphere://capabilities`

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-1","method":"resources/read","params":{"uri":"shardingsphere://capabilities"}}'
```

Expected result:

- The response content type is `text/event-stream`.
- The `data:` line contains one resource payload for `shardingsphere://capabilities`.

### Read `shardingsphere://databases/orders/schemas/public/sequences`

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-2","method":"resources/read","params":{"uri":"shardingsphere://databases/orders/schemas/public/sequences"}}'
```

Expected result:

- The response content type is `text/event-stream`.
- The `data:` line contains sequence metadata such as `order_seq` when the database declares `SEQUENCE` support.

### Optional: open the SSE stream

Use this only when you want to inspect the long-lived server-sent event stream directly:

```bash
curl -N http://127.0.0.1:18088/mcp \
  -H 'Accept: text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

Notes:

- This command blocks until you stop it with `Ctrl+C`.
- Keep it in its own terminal while you debug follow-up traffic.

### Close the session

```bash
curl -sS -D - -o /dev/null \
  -X DELETE http://127.0.0.1:18088/mcp \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}"
```

Expected result:

- The response status is `200`.

## Using STDIO

STDIO is implemented as a real MCP stdio transport for local clients that launch the ShardingSphere MCP process as a child process.
Enable STDIO only when the client will communicate over the process `stdin` and `stdout`.

### Run with STDIO only

The packaged runtime now ships `conf/mcp-stdio.yaml` out of the box. Start with that file:

```bash
bin/start.sh conf/mcp-stdio.yaml
```

Notes:

- The process still runs in the foreground.
- If both `transport.http.enabled` and `transport.stdio.enabled` are `false`, startup fails with: "Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."
- If both transports are enabled, startup fails with: "HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."
- The default `conf/logback.xml` writes console logs to stderr and file logs to `logs/mcp.log`, so stdout stays reserved for MCP protocol messages.
- STDIO mode is for MCP clients, not for a human-oriented interactive shell. Launch it from an MCP client configuration rather than typing requests manually in the terminal.

Reference:

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioMCPServer.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java`

## Runtime Notes

- The packaged `conf/mcp.yaml` now ships with a demo multi-database JDBC `runtimeDatabases` block so the distribution can prove logical-database discovery and real query execution on the first run.
- For real deployments, replace the `runtimeDatabases` block with your own logical database mapping and JDBC connection properties. Each logical database entry must declare its own required runtime fields; schema discovery now comes from JDBC metadata, and legacy `runtime.*` aliases are no longer supported.
- `driverClassName` is optional for JDBC 4 drivers that auto-register through `DriverManager`. Keep it only when your target driver requires an explicit override.
- If your target database driver is not already packaged, copy the driver jar under `ext-lib/` before running `bin/start.sh`.
- Exactly one transport must be enabled for each runtime process.
- For local-only HTTP usage, keep `transport.http.enabled: true` and `transport.stdio.enabled: false`.
- For local MCP client integration, keep `transport.http.enabled: false` and `transport.stdio.enabled: true`.
- `transport.http.bindHost` controls which address the HTTP service listens on: `127.0.0.1`, `localhost`, and `::1` are local-only; `0.0.0.0` or a specific intranet IP exposes the matching network interface.
- Non-loopback `bindHost` values require `transport.http.allowRemoteAccess: true`, otherwise startup fails; this field only declares remote-exposure intent.
- When `transport.http.accessToken` is configured, every HTTP request must provide `Authorization: Bearer <token>`.
- Non-loopback `bindHost` values also require a non-blank `transport.http.accessToken`, so remote HTTP is not exposed anonymously.
- The built-in access token is a deployment-level shared secret, not a login or per-user credential.
- Even with the built-in access token enabled, keep externally exposed endpoints behind a trusted network, gateway, or reverse proxy.
- To start with a custom configuration file, run `bin/start.sh /path/to/mcp.yaml`.
- To tune the JVM for local experiments, use `JAVA_OPTS`, for example `JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh`.

## Registry and OCI Publication

- Official MCP Registry metadata lives in `mcp/server.json`.
- The published server name is `io.github.apache/shardingsphere-mcp`.
- The first public package type is OCI on GHCR: `ghcr.io/apache/shardingsphere-mcp:<version>`.
- The release workflow updates `mcp/server.json` to the GitHub release version before publishing to the official MCP Registry.

### Run the published image over stdio

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

### Run the published image over HTTP

```bash
docker run --rm -p 18088:18088 \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

Notes:

- `SHARDINGSPHERE_MCP_TRANSPORT=stdio` selects the packaged `conf/mcp-stdio.yaml`.
- Leaving `SHARDINGSPHERE_MCP_TRANSPORT` unset keeps the Docker image on the HTTP default.
- If you need a custom config path inside the container, set `SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/your-config.yaml`.
- `.github/workflows/mcp-build.yml` publishes the GHCR image and then runs `mcp-publisher publish`.

## Development Pointers

- `test/e2e/mcp` also includes a real-model smoke lane for MCP:
  - default stack: `Ollama + qwen3:1.7b`
  - runtime coverage: file-backed H2 runtime plus a Testcontainers MySQL runtime
  - runtime shape: the tests launch the production bootstrap runtime in-process over HTTP and STDIO
  - final assertion: structured JSON plus MCP tool trace
- Local reproduction for the LLM smoke lane:

```bash
docker run -d --rm --name ollama-mcp-llm-e2e -p 11434:11434 ollama/ollama:latest
docker exec ollama-mcp-llm-e2e ollama pull qwen3:1.7b
MCP_LLM_E2E_ENABLED=true \
MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1 \
MCP_LLM_MODEL=qwen3:1.7b \
./mvnw -pl test/e2e/mcp -am test -DskipITs -Dspotless.skip=true \
  '-Dtest=*ProductionLLMH2SmokeE2ETest,*ProductionLLMMySQLSmokeE2ETest' \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- The LLM smoke artifacts are written under `test/e2e/mcp/target/llm-e2e/`.
- The dedicated GitHub Actions entry point is `.github/workflows/mcp-llm-e2e.yml`, delivered as `workflow_dispatch` plus nightly schedule instead of a PR gate.
- `mcp/core`: capability, metadata, session, audit, execute-query contracts, shared runtime service assembly, JDBC runtime configuration, metadata discovery, `DatabaseRuntime` assembly, and the JDBC-backed runtime context factory
- `mcp/bootstrap`: MCP Java SDK based bootstrap, HTTP / STDIO transport, top-level config loading, and lifecycle management
- `distribution/mcp`: standalone packaging, scripts, config, Dockerfile
- `test/e2e/mcp`: end-to-end contract validation

For local debugging and richer semantic verification, prefer the integration tests in `mcp/bootstrap` and the E2E suite in `test/e2e/mcp`.
