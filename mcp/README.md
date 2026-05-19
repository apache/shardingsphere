# ShardingSphere MCP

ShardingSphere MCP provides a standalone Model Context Protocol runtime for Apache ShardingSphere.
This quick start is optimized for the first successful local run: build the packaged distribution, start the bundled demo runtime, initialize one session, and verify discovery and query behavior over HTTP.
Additional notes cover how to replace the demo runtime with a real JDBC-backed deployment.

## Quick Start

### Prerequisites

- JDK 21 available from `JAVA_HOME` or `PATH`
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

```bat
cd /d "%DIST_DIR%"
bin\start.bat
```

Notes:

- `bin/start.sh` and `bin\start.bat` run in the foreground. Keep this terminal open and use a second terminal for the `curl` commands below.
- The packaged runtime defaults to `conf/mcp-http.yaml`, also ships `conf/mcp-stdio.yaml`, and reads `conf/logback.xml` for logging.
- When HTTP is enabled, the default endpoint is `http://127.0.0.1:18088/mcp`.
- Logs are written under `logs/`.
- `conf/mcp-http.yaml` is strict about supported field names: `transport.type`, `transport.http.bindHost`, `transport.http.port`,
  `transport.http.endpointPath`, and all runtime database fields must be declared with supported keys only.
- MCP YAML values are explicit. Put deployment-specific secrets such as JDBC credentials in a protected custom configuration file,
  then select that file with `SHARDINGSPHERE_MCP_CONFIG` or a startup script argument.
- Exactly one transport must be selected per process with `transport.type`. The packaged sample configuration selects Streamable HTTP.
- `bin/start.sh` and `bin\start.bat` validate the config file, runtime libraries, and Java availability before startup, create `data/`, `logs/`, and `plugins/`, then start from the package root so relative runtime paths resolve consistently.
- If startup succeeds, the process stays running in the foreground. If it exits immediately, inspect the terminal error and `logs/mcp.log` first.
- The bundled demo runtime exposes two logical databases named `orders` and `billing`, both backed by the packaged H2 driver and seed data under `data/`.

The packaged sample configuration is:

```yaml
transport:
  type: STREAMABLE_HTTP

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
- The current public tools are `database_gateway_search_metadata`, `database_gateway_execute_query`, `database_gateway_execute_update`, `database_gateway_plan_encrypt_rule`, `database_gateway_plan_mask_rule`, `database_gateway_apply_workflow`, and `database_gateway_validate_workflow`.
- `database_gateway_execute_query` accepts read-only `SELECT` and `EXPLAIN ANALYZE` statements only. Use `database_gateway_execute_update` for DML, DDL, DCL, transaction control, savepoints, and other supported side-effecting SQL.
- `database_gateway_execute_query.max_rows` uses server default `100` when omitted or set to `0`; explicit values from `1` to `5000` bound returned rows.
- The encrypt and mask workflow targets logical databases exposed by ShardingSphere-Proxy; the dedicated usage notes appear below.
- `database_gateway_search_metadata.object_types` accepts `database`, `schema`, `table`, `view`, `column`, `index`, and `sequence` only.

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"tool-1","method":"tools/call","params":{"name":"database_gateway_search_metadata","arguments":{"database":"orders","query":"order","object_types":["table","view"]}}}'
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
  --data '{"jsonrpc":"2.0","id":"tool-2","method":"tools/call","params":{"name":"database_gateway_execute_query","arguments":{"database":"orders","schema":"public","sql":"SELECT status FROM orders ORDER BY order_id","max_rows":10}}}'
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
- The payload is generated from the descriptor catalog and includes `resources`, `resourceTemplates`, `tools`, `prompts`, `completionTargets`, `resourceNavigation`,
  `protocolAvailability`, and deterministic `fingerprints`.

### Descriptor-Driven Discovery

MCP tools, resources, prompts, and completions publish model-facing metadata from YAML descriptors under `META-INF/shardingsphere-mcp/mcp-descriptors`.
The same descriptor source is used by `resources/list`, `resources/templates/list`, `tools/list`, `prompts/list`, `completion/complete`, and the aggregate `shardingsphere://capabilities` resource.

Descriptors must describe what the model should use the surface for, not just repeat the URI or tool name:

- Resource descriptors include URI template parameter meaning, logical versus physical object scope, MIME type, title, description, annotations, and relationship metadata.
- Tool descriptors include input field descriptions, structured object properties where keys are known, output schema, MCP annotations, related resources, follow-up tools, and side-effect notes.
- Prompt descriptors expose task guides such as metadata inspection, safe SQL execution, encrypt planning, mask planning, and workflow recovery.
- Completion targets provide descriptor-backed suggestions for runtime names, algorithms, and current-session workflow plan IDs.
- Completion responses include diagnostics such as missing context, candidate counts, and deterministic ranking reasons.
- `resourceNavigation` explains lightweight public next hops such as databases to schemas, tables to columns, algorithms to planning tools,
  and workflow plans to apply or validation tools.
- `shardingsphere://runtime` exposes a small runtime status readout, and `shardingsphere://workflows/{plan_id}` lets clients read back a workflow plan by ID.
- `fingerprints` records deterministic hashes for descriptor, prompt, navigation, and model-facing schema surfaces so test artifacts can prove which MCP surface a model used.
- Item-list responses always include `items`, `count`, `has_more`, and `continuation_mode`. `next_page_token` appears only when a response supports ShardingSphere application-level pagination;
  these structured payload fields are not MCP list-method `cursor` or `nextCursor`.
  Resource reads also include `self_uri`, and include typed `parent_resource` or typed `next_resources` when applicable.
- Workflow tool responses include `missing_required_inputs`, `clarification_questions`, `resources_to_read`, `review_summary`, and `next_actions`
  so a model can continue the workflow without guessing or relying on legacy recommendation fields.
- Recoverable error payloads keep the original `error_code` and `message`, and add `recovery` hints for missing arguments,
  unsupported tools or resources, invalid enum values, workflow state errors, and unsafe SQL tool selection.
  `error_code` is the single ShardingSphere MCP error code contract; JSON-RPC numeric error codes are transport envelopes only.

Descriptor annotations follow the MCP `2025-11-25` schema and are developer-maintained surface metadata, not end-user runtime configuration:

- Resource annotations are optional. When present, they may only use `audience`, `priority`, and `lastModified`; omit the whole `annotations` map when no field is needed.
- Resource `audience` values must be MCP roles `user` or `assistant`; `priority` must be finite and between `0.0` and `1.0`; `lastModified` must include an ISO 8601 UTC marker or offset.
- Tool annotations use MCP `ToolAnnotations`. MCP defines effective defaults of `readOnlyHint=false`, `destructiveHint=true`, `idempotentHint=false`, and `openWorldHint=true`.
- ShardingSphere public tool descriptors must still declare all four tool boolean hints explicitly in YAML, so reviewers can see the safety decision before primitive defaults are applied.
- Tool annotations are client hints only. They do not replace runtime validation, SQL safety checks, or server-side authorization.

### MCP Protocol Capability Scope

ShardingSphere MCP targets MCP protocol revision `2025-11-25`. The public protocol surface is intentionally narrow:

- `resources`, `tools`, `prompts`, and `completions` are enabled. The server supports `resources/list`, `resources/templates/list`,
  `resources/read`, `tools/list`, `tools/call`, `prompts/list`, `prompts/get`, and `completion/complete`.
- Resource subscriptions and resource/tool/prompt list-changed notifications are not implemented. The server advertises `subscribe=false` and `listChanged=false` for those primitives.
- The MCP Java SDK advertises `logging` and accepts `logging/setLevel`; ShardingSphere MCP does not emit ShardingSphere product log messages
  through `notifications/message`. Operational logs stay in stderr or `logs/mcp.log`.
- `progress`, `notifications/cancelled`, and task-augmented requests are future scope for ShardingSphere MCP. Clients should use structured workflow tool responses and workflow resources for status.
- MCP `icons` and `Tool.execution` are official `2025-11-25` descriptor fields, but MCP Java SDK `1.1.2` does not expose `icons` in `Resource`, `ResourceTemplate`, or `Tool`, and does not expose `execution` in `Tool`; they remain future scope until the SDK boundary supports them.
- `roots` and `sampling` are client capabilities. ShardingSphere MCP does not require roots and does not send `sampling/createMessage` requests.
- Elicitation is client-negotiated and used only for non-sensitive workflow clarification. Secret-bearing fields remain out-of-band as described in the workflow security notes.

### ShardingSphere Feature Scope

The runtime exposes ShardingSphere through logical database metadata, safe SQL tools, and selected workflow helpers. Current V1 scope:

- Supported: logical database discovery; schemas, tables, views, columns, indexes, and sequences where JDBC metadata and dialect capability permit;
  read-only query; side-effecting SQL preview or execute with explicit execution mode; encrypt and mask planning, apply, and validation workflows against
  ShardingSphere-Proxy; encrypt and mask algorithm and rule resources.
- Partially supported: dialect-specific SQL and metadata capability discovery through `shardingsphere://databases/{database}/capabilities`;
  unsupported statement classes and metadata object types return structured MCP errors.
- Future scope: direct MCP management for sharding rules, readwrite-splitting, shadow, traffic governance, database discovery rule configuration,
  mode governance or registry operations, and observability metrics, tracing, or dashboards.
- Unsupported in V1: using encrypt or mask workflows against physical storage databases directly, applying workflow changes without preview,
  or treating MCP as a general ShardingSphere administration shell.

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

```bat
bin\start.bat conf\mcp-stdio.yaml
```

Notes:

- The process still runs in the foreground.
- `transport.type` must be `STREAMABLE_HTTP` or `STDIO`.
- `transport.http` is valid only when `transport.type` is `STREAMABLE_HTTP`; omit it for `STDIO`.
- The default `conf/logback.xml` writes console logs to stderr and file logs to `logs/mcp.log`, so stdout stays reserved for MCP protocol messages.
- STDIO mode is for MCP clients, not for a human-oriented interactive shell. Launch it from an MCP client configuration rather than typing requests manually in the terminal.

Reference:

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioMCPServer.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java`

## Client Configuration and Troubleshooting

- Generic STDIO client shape:

  ```json
  {
    "mcpServers": {
      "shardingsphere": {
        "command": "/path/to/apache-shardingsphere-mcp/bin/start.sh",
        "args": ["conf/mcp-stdio.yaml"]
      }
    }
  }
  ```

- Generic HTTP client shape:

  ```json
  {
    "mcpServers": {
      "shardingsphere-http": {
        "url": "http://127.0.0.1:18088/mcp"
      }
    }
  }
  ```

- Startup logs one concise diagnostic line through the configured logger: configuration path, log path, runtime database count,
  active transport, and endpoint.
- Configure clients to use official MCP discovery methods first (`tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`,
  `completion/complete`), then read `shardingsphere://capabilities` as a domain catalog when needed.
- The built-in HTTP runtime does not provide authorization in this release. Put remote deployments behind a trusted network boundary,
  reverse proxy, or gateway that handles authentication and authorization.
- If startup reports zero or missing runtime databases, fix `runtimeDatabases`; MCP resources expose ShardingSphere logical databases,
  not physical storage units.
- If `shardingsphere://runtime` reports `server_status=configuration_required`, configure at least one `runtimeDatabases` entry before metadata discovery or SQL execution.
- If JDBC metadata or SQL execution fails with a driver error, add the target JDBC driver jar under `plugins/` or the embedding classpath.
- In STDIO mode, keep stdout reserved for MCP protocol frames. Send diagnostics to stderr or `logs/mcp.log`.
- Workflow tools plan ShardingSphere logical rule changes; preview before apply so side effects are visible.

## Runtime Notes

- The packaged `conf/mcp-http.yaml` now ships with a demo multi-database JDBC `runtimeDatabases` block so the distribution can prove logical-database discovery and real query execution on the first run.
- For real deployments, replace the `runtimeDatabases` block with your own logical database mapping and JDBC connection properties. Each logical database entry must declare its own required runtime fields; schema discovery now comes from JDBC metadata, and legacy `runtime.*` aliases are no longer supported.
- `driverClassName` is optional for JDBC 4 drivers that auto-register through `DriverManager`. Keep it only when your target driver requires an explicit override.
- The packaged distribution keeps the official MCP baseline jars, including encrypt and mask, under `lib/`.
- If your target database driver or an extra MCP feature jar is not already packaged, copy that jar under `plugins/` before running `bin/start.sh` or `bin\start.bat`.
- If you embed `shardingsphere-mcp-bootstrap` directly instead of using the packaged distribution, add the feature jars you need to that runtime classpath explicitly.
- Exactly one transport must be selected for each runtime process with `transport.type`.
- For local-only HTTP usage, set `transport.type: STREAMABLE_HTTP`.
- For local MCP client integration, set `transport.type: STDIO` and omit `transport.http`.
- `transport.http` is optional for Streamable HTTP. Missing `bindHost`, `port`, and `endpointPath` use defaults: `127.0.0.1`, `18088`, and `/mcp`.
- `transport.http.bindHost` controls which address the HTTP service listens on: `127.0.0.1`, `localhost`, and `::1` are local-only; `0.0.0.0` or a specific intranet IP exposes the matching network interface.
- For loopback HTTP bindings, a present `Origin` header must also be loopback; malformed or remote origins fail with `403`.
- For non-loopback HTTP bindings, missing `Origin` is accepted for non-browser clients, but any present `Origin` is rejected with `403`.
- The built-in HTTP runtime is unauthenticated. Keep externally exposed endpoints behind a trusted network, gateway, or reverse proxy.
- To start with a custom configuration file, run `bin/start.sh /path/to/mcp-http.yaml` on Unix-like systems or `bin\start.bat path\to\mcp-http.yaml` on Windows.
- To tune the JVM for local experiments, use `JAVA_OPTS`, for example `JAVA_OPTS="-Xms256m -Xmx256m" bin/start.sh` on Unix-like systems or `set "JAVA_OPTS=-Xms256m -Xmx256m" && bin\start.bat` on Windows.

## Feature SPI Layout

The current MCP subchain is organized as `api + support + features + core + bootstrap`:

- `mcp/api`
  - defines public tool / resource handler contracts, shared descriptors, protocol responses, and MCP protocol exceptions
- `mcp/support`
  - provides database metadata, execution, capability, and workflow contexts, models, facades, SPI, and reusable helpers for MCP core and pluggable features
- `mcp/features/encrypt`
  - provides encrypt MCP tools, resources, and workflow implementation
- `mcp/features/mask`
  - provides mask MCP tools, resources, and workflow implementation
- `mcp/core`
  - provides capability, metadata, session, execute-query, and shared runtime services
- `mcp/bootstrap`
  - aggregates contributed features through the MCP Java SDK and exposes HTTP / STDIO transports

Encrypt and mask are not special cases hardcoded in bootstrap. They are pluggable MCP features contributed through the MCP handler provider SPI.

## How to Add a New MCP Feature

If you want to add another feature beyond encrypt and mask, keep the implementation path minimal:

- Create `mcp/features/<feature>` and depend on `mcp/api`, `mcp/support` for database metadata, execution, or workflow handlers, `mcp/core` only when service-level handler context is needed, plus the required domain modules only; do not depend on `mcp/bootstrap`
- If this is a new feature module, wire it into both the build and the runtime classpath: add it under `mcp/features/pom.xml`, then either add it to `distribution/mcp/pom.xml` when it should ship in the official packaged runtime or place the built jar under `plugins/` before startup when it should stay optional
- For each public tool, implement `MCPToolHandler<T extends MCPHandlerContext>` with the required context type and canonical tool name, and add its descriptor under `META-INF/shardingsphere-mcp/mcp-descriptors`
- For each public resource, implement `MCPResourceHandler<T extends MCPHandlerContext>` with the required context type and canonical URI template, and add its descriptor under `META-INF/shardingsphere-mcp/mcp-descriptors`
- Resolve handler-owned descriptor metadata through `MCPHandlerDescriptorUtils` when runtime code needs the catalog descriptor; do not duplicate descriptor fields inside handlers
- Use `MCPServiceHandlerContext` for service-level handlers, `MCPDatabaseHandlerContext` for database metadata or execution handlers, and `MCPWorkflowHandlerContext` for workflow handlers
- Implement one `MCPHandlerProvider` that returns the feature-owned handlers through `getToolHandlers()` and `getResourceHandlers()`
- If the feature owns workflow definitions, implement `MCPWorkflowDefinitionProvider` on the same provider
- Register `org.apache.shardingsphere.mcp.api.MCPHandlerProvider` under `src/main/resources/META-INF/services/`
- Keep feature URIs under `shardingsphere://features/<feature>/...` so they do not leak into shared metadata paths
- `mcp/core` discovers handler providers through `ShardingSphereServiceLoader`, flattens their handlers, and validates global uniqueness; `mcp/bootstrap` only publishes the final protocol surface
- Tool names and resource URI patterns must stay globally unique; duplicate handlers and duplicate descriptors are rejected during startup validation

The encrypt and mask modules are the recommended reference implementations.

## Encrypt and Mask Workflow over ShardingSphere-Proxy

These workflow tools allow an MCP client to plan, execute, and validate encrypt or mask rules for one logical column by using natural language, structured arguments, or both.
The goal is not to re-implement encryption inside MCP. Instead, MCP translates user intent into executable DDL, DistSQL, and validation steps for ShardingSphere-Proxy.
The tools and resources described below are registered by the encrypt and mask feature modules through SPI; bootstrap only aggregates and publishes them to the protocol layer.
For a condensed scorecard-oriented flow, see `.specify/specs/020-mcp-encrypt-mask-scoped-100/quickstart.md`.

### Prerequisites

- V1 supports `ShardingSphere-Proxy` only.
- The MCP runtime must connect to the logical database exposed by `ShardingSphere-Proxy`, not to the underlying storage database directly.
- The demo H2 runtime from the quick start is good for discovery and query verification. For encrypt or mask workflows, replace `runtimeDatabases` with a Proxy-backed logical database.
- The current workflow focuses on rules, metadata, and SQL executability validation. It does not migrate or backfill existing data.

### Related tools and resources

The encrypt feature exposes 1 planning tool:

- `database_gateway_plan_encrypt_rule`
  - resolves encrypt intent, asks follow-up questions when required, and produces derived-column plans, DDL, DistSQL, index plans, and validation strategy

The mask feature exposes 1 planning tool:

- `database_gateway_plan_mask_rule`
  - resolves mask intent, asks follow-up questions when required, and produces DistSQL plus validation strategy

The workflow runtime exposes 2 generic tools shared by encrypt and mask plans:

- `database_gateway_apply_workflow`
  - executes the generated artifacts for the current `plan_id`, or exports them without execution in `manual-only` mode
- `database_gateway_validate_workflow`
  - validates the current plan result across the workflow-specific validation layers

The workflow also adds these feature resources:

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

The `features/*/algorithms` resources expose the algorithm plugins visible from the current Proxy instance, so a model can recommend from the actual runtime pool.

### Keep these rules in mind before you start

- Reuse the same `MCP-Session-Id` for the whole workflow. If `plan`, `apply`, and `validate` switch to another session, later calls fail because the plan belongs to a different MCP session.
- The first `database_gateway_plan_encrypt_rule` or `database_gateway_plan_mask_rule` call does not need `plan_id`; after the first response returns `plan_id`, reuse that same `plan_id` for every follow-up planning call, apply call, and validation call inside the same feature workflow.
- `database_gateway_plan_encrypt_rule` and `database_gateway_plan_mask_rule` only plan. They do not execute DDL or DistSQL.
- `database_gateway_apply_workflow` executes artifacts for the current workflow plan. `database_gateway_validate_workflow` only checks the current state.
- Users always target logical databases, logical tables, and logical columns. In encrypt workflows MCP may plan and create physical derived columns, but the user-facing target is still the logical object exposed by Proxy.
- `schema` is optional. MCP auto-fills it when the logical database contains a single schema. If the schema cannot be resolved uniquely, MCP asks for it explicitly.
- `delivery_mode` only affects how the client presents the workflow. It does not change the generated artifacts. The execution behavior is controlled by `execution_mode`.
- Sensitive algorithm properties are masked in plan and apply responses and are never echoed back in clear text.
- MCP form elicitation is only used for non-sensitive clarification questions.
  If a question has `secret: true`, `input_type: "secret"`, or a field name containing password, token, key, secret, or credential,
  keep the returned `plan_id`, collect the value through URL mode when available, a secret manager, a protected environment variable,
  or an operator-controlled channel, and then retry the same planner.
- Every `curl` example below assumes you have already completed MCP `initialize` and are reusing the same `SESSION_ID` plus its matching `PROTOCOL_VERSION`.

### Recommended call order

If you want one encrypt or mask workflow to run end to end without ambiguity, use this order:

1. Start with the feature-specific planner: `database_gateway_plan_encrypt_rule` for encrypt or `database_gateway_plan_mask_rule` for mask. Do not start from `apply`.
2. If the response is `status = clarifying`, read `clarification_questions`, send non-sensitive values for the listed `field` entries,
   collect sensitive values through the approved secret channels above, and call the same feature-specific `plan_*_rule` again with the same `plan_id`.
3. If the response is `status = planned`, review `derived_column_plan`, `ddl_artifacts`, `distsql_artifacts`, and `index_plan`.
4. Call `database_gateway_apply_workflow` with `execution_mode=preview` so MCP shows the artifacts and side-effect scope without changing runtime state.
5. After reviewing the preview, call `database_gateway_apply_workflow` with `execution_mode=review-then-execute`, or use `manual-only` when the artifacts should be exported.
6. If apply returns `awaiting-manual-execution`, execute the returned `manual_artifact_package` against ShardingSphere-Proxy first, then continue.
7. Call `database_gateway_validate_workflow` and make sure the returned validation layers pass.
8. If validation fails, inspect `issues` and `mismatches`, then either finish the remaining apply steps or re-plan after fixing the environment.

### Interaction model

Every `database_gateway_plan_encrypt_rule` and `database_gateway_plan_mask_rule` call returns a global step list and the current step. The default workflow is:

1. Confirm database, table, column, and target lifecycle
2. Inspect existing rules, plugins, and logical metadata
3. Clarify missing requirements and recommend algorithms
4. Collect algorithm properties and create the derived naming plan
5. Generate DDL, DistSQL, and index artifacts
6. Review artifacts and choose the execution mode
7. Execute or export artifacts
8. Validate and summarize

Common status values are:

- `clarifying`
  - more input is required, for example the logical database, algorithm type, or key properties
- `planned`
  - artifacts are ready and the workflow can move to apply
- `completed`
  - `database_gateway_apply_workflow` finished its execution path
- `awaiting-manual-execution`
  - `manual-only` was selected, so MCP exported artifacts without executing them
- `validated`
  - `database_gateway_validate_workflow` passed

In addition:

- `delivery_mode` supports `all-at-once` and `step-by-step`
  - MCP echoes the selected mode in the plan response so the client can decide whether to present the whole plan at once or drive the conversation step by step
- Planning `execution_mode` supports `review-then-execute` and `manual-only` as the preferred final apply mode.
- `database_gateway_apply_workflow` requires explicit `execution_mode`
  - use `preview` first, then `review-then-execute` after reviewing the preview, or `manual-only` to export a manual artifact package only

### What to do next for each status

- `clarifying`
  - more input is required, so read `clarification_questions`, provide each non-sensitive listed `field`,
    collect sensitive fields through an approved secret channel, and call the same feature-specific `plan_*_rule` again with the same `plan_id`
- `planned`
  - the execution package is ready, so review the artifacts and continue with `database_gateway_apply_workflow`
- `completed`
  - automatic execution finished, so the next step is `database_gateway_validate_workflow`
- `awaiting-manual-execution`
  - `manual-only` was selected, so execute the returned artifacts manually first and then run validation
- `validated`
  - the workflow finished successfully
- `failed`
  - inspect `issues` and `mismatches` first, then decide whether to re-plan, finish a partial apply, or fix the environment before validating again

### The response fields you will read most often

In `database_gateway_plan_encrypt_rule` and `database_gateway_plan_mask_rule`, the most important fields are:

- `plan_id`
  - the workflow identifier reused by every follow-up step
- `status`
  - tells you whether to clarify, apply, or troubleshoot
- `clarification_questions`
  - typed clarification prompts; send values for each `field`, using `display_message` only as user-facing text
- `algorithm_recommendations`
  - the recommended algorithm pool when natural language did not provide enough detail
- `derived_column_plan`
  - encrypt only, because it contains the final `*_cipher`, `*_assisted_query`, and `*_like_query` names
- `ddl_artifacts`
  - physical DDL such as `ALTER TABLE ... ADD COLUMN ...`; encrypt may produce it, mask normally does not
- `distsql_artifacts`
  - the `CREATE/ALTER/DROP ENCRYPT RULE` or `MASK RULE` statements that will be applied on Proxy
- `index_plan`
  - encrypt only, when equality or like-query capabilities require derived indexes

In `database_gateway_apply_workflow`, the most important fields are:

- `status`
- `issues`
- `step_results`
- `executed_ddl`
- `executed_distsql`
- `skipped_artifacts`
- `manual_artifact_package`

In `database_gateway_validate_workflow`, the most important fields are:

- `status`
- `overall_status`
- `issues`
- `mismatches`
- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

### Encrypt workflow

#### Minimum useful input

For encrypt `create` or `alter`, the recommended minimum input is:

- `database`
- `table`
- `column`
- `natural_language_intent`
  - or explicit `operation_type=create|alter` when the caller already knows the target lifecycle
- `algorithm_type`
  - if natural language already makes the algorithm clear, MCP can infer it; if you already know the target algorithm, send it directly
- `primary_algorithm_properties`
  - for example `AES` requires `aes-key-value`
- `schema`
  - strongly recommended in multi-schema logical databases

#### Typical input

Natural language and structured overrides can be mixed. The following example plans a reversible encrypt workflow directly:

```bash
AES_KEY_VALUE="${SHARDINGSPHERE_AES_KEY_VALUE}"

curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"Encrypt status reversibly without equality lookup and without like lookup",
        "algorithm_type":"AES",
        "primary_algorithm_properties":{"aes-key-value":"'"${AES_KEY_VALUE}"'"}
      }
    }
  }'
```

Expected result:

- the response returns a `plan_id`
- `status = planned`
- `derived_column_plan` contains the final derived-column names, for example `status_cipher`
- `ddl_artifacts`, `distsql_artifacts`, and `index_plan` describe the execution plan
- sensitive values stay masked in `masked_property_preview` and the previewed DistSQL

#### Full example: from natural language to a validated encrypt workflow

The example below shows a complete encrypt flow. The first call intentionally omits the key so MCP enters `clarifying`, and the second call continues with the same `plan_id`.

Step 1: ask MCP to recognize the requirement and recommend algorithms

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-clarifying-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"Encrypt status reversibly with equality lookup and without like lookup"
      }
    }
  }'
```

Typical response snippet:

```json
{
  "plan_id": "plan-xxx",
  "status": "clarifying",
  "clarification_questions": [
    {"field": "primary_algorithm_properties.aes-key-value", "input_type": "secret", "secret": true, "display_message": "Please provide property `aes-key-value`."}
  ],
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "AES"},
    {"algorithm_role": "assisted_query", "algorithm_type": "MD5"}
  ]
}
```

This secret-bearing question is returned as tool structured content only; it is not converted into MCP form elicitation.
Keep the `plan_id`, obtain the key through URL mode when available, a secret manager, a protected environment variable, or an operator-controlled channel,
then retry the planner.

In real usage, store the returned `plan_id` first:

```bash
PLAN_ID='plan-xxx'
AES_KEY_VALUE="${SHARDINGSPHERE_AES_KEY_VALUE}"
```

This means:

- do not call `database_gateway_apply_workflow` yet
- keep using the same `plan_id`
- provide the missing `aes-key-value` only after the client has obtained it through an approved secret channel

Step 2: continue the same plan and provide the missing property

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-clarifying-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"aes-key-value":"'"${AES_KEY_VALUE}"'"}
      }
    }
  }'
```

Typical response snippet:

```json
{
  "plan_id": "plan-xxx",
  "status": "planned",
  "current_step": "review",
  "derived_column_plan": {
    "cipher_column_name": "status_cipher",
    "assisted_query_column_name": "status_assisted_query",
    "assisted_query_column_required": true,
    "like_query_column_required": false
  },
  "ddl_artifacts": [
    {"sql": "ALTER TABLE orders ADD COLUMN status_cipher VARCHAR(...) ..."}
  ],
  "index_plan": [
    {"sql": "CREATE INDEX idx_orders_status_assisted_query ON orders (status_assisted_query)"}
  ],
  "distsql_artifacts": [
    {"sql": "ALTER ENCRYPT RULE orders ..."}
  ]
}
```

At this point the workflow is ready to execute. Review:

- whether the derived column names are acceptable
- whether assisted-query derived columns and indexes are expected
- whether the DistSQL mapping matches the intended logical column and algorithm types

Step 3: apply the approved plan

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

Typical response snippet:

```json
{
  "status": "completed",
  "step_results": [
    {"artifact_type": "add-column", "status": "passed"},
    {"artifact_type": "create-index", "status": "passed"},
    {"artifact_type": "rule_distsql", "status": "passed"}
  ],
  "executed_ddl": ["ALTER TABLE ...", "CREATE INDEX ..."],
  "executed_distsql": ["ALTER ENCRYPT RULE ..."]
}
```

Step 4: validate the final state

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

Typical response snippet:

```json
{
  "status": "validated",
  "overall_status": "passed",
  "ddl_validation": {"status": "passed"},
  "rule_validation": {"status": "passed"},
  "logical_metadata_validation": {"status": "passed"},
  "sql_executability_validation": {"status": "passed"}
}
```

When all four validation layers are `passed`, the encrypt workflow is complete.

#### When algorithms or properties are missing

If natural language does not make the algorithm clear, or if a required property such as `aes-key-value` is missing, `database_gateway_plan_encrypt_rule` returns:

- `status = clarifying`
- `clarification_questions`
- `algorithm_recommendations`
- `property_requirements`

Continue with the same `plan_id` and send missing non-sensitive fields back to `database_gateway_plan_encrypt_rule` instead of creating a new plan.
For secret fields, obtain the value through URL mode when available, a secret manager, a protected environment variable,
or an operator-controlled channel before retrying.

#### Default derived-column conventions

- encrypt uses `*_cipher` by default
- equality lookup adds `*_assisted_query` plus the corresponding index plan
- like lookup adds `*_like_query`
- when a default name conflicts with an existing column, MCP appends a numeric suffix and returns the final name in `derived_column_plan`

#### Apply and validate

The planned default final mode is `review-then-execute`, but `database_gateway_apply_workflow` should be previewed first so the side-effect scope is visible before execution.
Preview first:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"preview\"}}}"
```

After reviewing the preview, execute the artifacts:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-2\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

If you want MCP to export the artifacts without executing them, switch to `manual-only`:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-3\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"manual-only\"}}}"
```

`manual-only` returns `manual_artifact_package` with:

- `ddl_artifacts`
- `index_plan`
- `distsql_artifacts`

If you need partial execution, `approved_steps` can be used as an execution filter with only `ddl`, `index_ddl`, or `rule_distsql`.
It is not an approval token; copy values from `preview_artifacts[].approval_step` after reviewing the preview.
Unknown values are rejected instead of silently skipped.
Partial execution is mainly for reviewed or staged rollouts. Until every required step has been executed, `database_gateway_validate_workflow` may fail as expected.

After execution, validate immediately:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

Validation covers 4 layers:

- `ddl_validation`
- `rule_validation`
- `logical_metadata_validation`
- `sql_executability_validation`

#### Drop an encrypt rule

V1 supports `encrypt drop` as a rule-removal workflow:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"encrypt-plan-drop-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "operation_type":"drop"
      }
    }
  }'
```

Expected result:

- `status = planned`
- `missing_required_inputs` is empty because `drop` does not need encrypt capability clarification
- `distsql_artifacts` contains `DROP ENCRYPT RULE` or `ALTER ENCRYPT RULE`
- the response includes warnings explaining that physical derived columns and indexes are not cleaned automatically

If sibling encrypt columns still exist on the same table, MCP generates `ALTER ENCRYPT RULE` and keeps the sibling entries. It generates `DROP ENCRYPT RULE` only when no encrypt column remains on that table.

### Mask workflow

#### Minimum useful input

For mask `create` or `alter`, the recommended minimum input is:

- `database`
- `table`
- `column`
- `natural_language_intent`
  - or explicit `operation_type=create|alter` when the caller already knows the target lifecycle
- `operation_type`
- `algorithm_type`
- `primary_algorithm_properties`
- `schema`
  - strongly recommended in multi-schema logical databases

For mask `drop`, the minimum input is:

- `database`
- `table`
- `column`
- `operation_type=drop`

#### Create or alter a mask rule

Mask supports the same mixed input style. The following example creates one mask rule directly:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "operation_type":"create",
        "algorithm_type":"KEEP_FIRST_N_LAST_M",
        "primary_algorithm_properties":{"first-n":"3","last-m":"4","replace-char":"*"}
      }
    }
  }'
```

Expected result:

- `status = planned`
- `distsql_artifacts` contains `CREATE MASK RULE` or `ALTER MASK RULE`
- mask does not require physical derived columns, so it does not produce encrypt-style derived-column DDL

Natural language such as "Mask phone as a mobile number and keep the first 3 and last 4 digits" is also supported. If required properties are still missing, the tool returns `clarifying` first.

#### Full example: from natural language to a validated mask workflow

The example below shows a complete mask flow. The first call uses natural language only, so MCP recommends an algorithm and asks for the missing properties.

Step 1: plan from natural language

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-clarifying-1",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "natural_language_intent":"Mask phone as a mobile number and keep the first 3 and last 4 digits"
      }
    }
  }'
```

Typical response snippet:

```json
{
  "plan_id": "plan-yyy",
  "status": "clarifying",
  "clarification_questions": [
    {"field": "primary_algorithm_properties.from-x", "input_type": "string", "secret": false, "display_message": "Please provide property `from-x`."},
    {"field": "primary_algorithm_properties.to-y", "input_type": "string", "secret": false, "display_message": "Please provide property `to-y`."}
  ],
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "MASK_FROM_X_TO_Y"}
  ]
}
```

In real usage, store the returned `plan_id` first:

```bash
PLAN_ID='plan-yyy'
```

Step 2: continue the same plan and provide the missing properties

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-clarifying-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"from-x":"4","to-y":"7"}
      }
    }
  }'
```

Typical response snippet:

```json
{
  "status": "planned",
  "distsql_artifacts": [
    {"sql": "CREATE MASK RULE orders ..."}
  ],
  "ddl_artifacts": [],
  "index_plan": []
}
```

Two important things to verify here:

- mask should not generate physical derived columns, so `ddl_artifacts` should normally stay empty
- the main review target is `distsql_artifacts`

Step 3: apply the approved mask rule

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_apply_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"review-then-execute\"}}}"
```

Step 4: validate the final rule state

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"database_gateway_validate_workflow\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

As long as `rule_validation`, `logical_metadata_validation`, and `sql_executability_validation` pass, the mask workflow can be considered complete.

#### Drop a mask rule

V1 supports `mask drop`:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{
    "jsonrpc":"2.0",
    "id":"mask-plan-2",
    "method":"tools/call",
    "params":{
      "name":"database_gateway_plan_mask_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"phone",
        "operation_type":"drop"
      }
    }
  }'
```

If sibling mask columns still exist on the same table, MCP generates `ALTER MASK RULE` and keeps the sibling entries. It generates `DROP MASK RULE` only when no mask column remains on that table.

### Inspect rules and algorithm pools

Read the encrypt rules for one logical table:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-encrypt-1","method":"resources/read","params":{"uri":"shardingsphere://features/encrypt/databases/logic_db/tables/orders/rules"}}'
```

Read the available encrypt and mask algorithms:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-plugin-1","method":"resources/read","params":{"uri":"shardingsphere://features/encrypt/algorithms"}}'
```

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data '{"jsonrpc":"2.0","id":"resource-plugin-2","method":"resources/read","params":{"uri":"shardingsphere://features/mask/algorithms"}}'
```

### Current scope and limitations

- `ShardingSphere-Proxy` only
- encrypt supports `create`, `alter`, and `drop`
- mask supports `create`, `alter`, and `drop`
- `encrypt drop` removes rules only; physical derived columns and indexes still require manual cleanup
- no existing-data migration or backfill
- no automatic rollback support
- no audit persistence
- V1 supports unquoted SQL identifiers only

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

### Run the published image with custom runtime config

```bash
docker run --rm -i \
  -e SHARDINGSPHERE_MCP_TRANSPORT=stdio \
  -e SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml \
  -v /path/to/mcp-stdio.yaml:/opt/shardingsphere-mcp/conf/custom-mcp-stdio.yaml:ro \
  -v /path/to/plugins:/opt/shardingsphere-mcp/plugins:ro \
  ghcr.io/apache/shardingsphere-mcp:5.5.4
```

Notes:

- `SHARDINGSPHERE_MCP_TRANSPORT=stdio` selects the packaged `conf/mcp-stdio.yaml`.
- Leaving `SHARDINGSPHERE_MCP_TRANSPORT` unset keeps the Docker image on the HTTP default.
- If you need a custom config path inside the container, mount the YAML file and set `SHARDINGSPHERE_MCP_CONFIG=/opt/shardingsphere-mcp/conf/your-config.yaml`.
- Use the same `SHARDINGSPHERE_MCP_CONFIG` pattern for HTTP; keep the port mapping and mount an HTTP-enabled YAML file.
- `.github/workflows/mcp-build.yml` publishes the GHCR image and then runs `mcp-publisher publish`.

## Development Pointers

- `test/e2e/mcp` also includes a real-model smoke lane for MCP:
  - default stack: Docker-owned `llama.cpp` server + `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
  - runtime coverage: file-backed H2 runtime plus a Testcontainers MySQL runtime
  - runtime shape: the tests launch the production bootstrap runtime in-process over HTTP and STDIO
  - final assertion: structured JSON plus MCP tool trace
- Prepare the module dependencies once before targeted local reproduction:

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

- Check Docker disk usage before building the local score-closing LLM runtime image:

```bash
docker system df
```

- Validate local architecture selection without downloading the model:

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh --dry-run
```

- Build the local score-closing LLM runtime image before Maven starts the LLM lane:

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh
```

- Local reproduction for the LLM smoke lane:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- The score-closing LLM lane starts the local `apache/shardingsphere-mcp-llm-runtime:local` image through Testcontainers. Maven does not download the model; the Docker build prepackages the pinned GGUF file and verifies it with `ADD --checksum`.

- Local reproduction for the LLM usability lane:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- For local debugging only, an already running OpenAI-compatible endpoint can be used with
  `-Dmcp.llm.runtime-mode=external-debug -Dmcp.llm.base-url=http://127.0.0.1:8080/v1`.
  External debug endpoints are not valid score-closing evidence.
- The LLM artifacts are written under `test/e2e/mcp/target/llm-e2e/`.
- The dedicated GitHub Actions entry points are `.github/workflows/mcp-llm-e2e.yml` and `.github/workflows/mcp-llm-usability-e2e.yml`.
  They run for PRs that touch MCP module paths or the dedicated workflow file itself, and they also keep `workflow_dispatch` plus weekday schedules.
  Keep these checks out of required branch-protection or ruleset checks; failures should stay visible, but an unfinished LLM lane must not block merge by itself.
  If a very large PR misses a path-filter match, use `workflow_dispatch` for manual score evidence.
- For local Docker cleanup, inspect first with `docker system df`, then use `docker image prune` or `docker builder prune` for dangling images and build cache.
  Do not run volume pruning as part of the default cleanup flow; volumes may contain local database state and require explicit confirmation.
- `mcp/api`: public tool / resource handler contracts, shared descriptors, protocol responses, and MCP protocol exceptions
- `mcp/support`: database metadata, execution, capability, and workflow contexts, models, facades, SPI, and reusable helpers for MCP core and pluggable features
- `mcp/features/encrypt`: encrypt tools, resources, planning / apply / validation, and algorithm visibility assembly
- `mcp/features/mask`: mask tools, resources, planning / apply / validation, and algorithm visibility assembly
- `mcp/core`: handler discovery, registry, request scope implementation, session, audit, execute-query runtime service assembly, JDBC runtime configuration, metadata discovery, `DatabaseRuntime` assembly, and the JDBC-backed runtime context factory
- `mcp/bootstrap`: MCP Java SDK based bootstrap, HTTP / STDIO transport, top-level config loading, feature SPI aggregation, and lifecycle management
- `distribution/mcp`: standalone packaging, scripts, config, Dockerfile
- `test/e2e/mcp`: end-to-end contract validation

For local debugging and richer semantic verification, prefer the integration tests in `mcp/bootstrap` and the E2E suite in `test/e2e/mcp`.
