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
- `bin/start.sh` validates the config file, runtime libraries, and Java availability before startup, creates `data/`, `logs/`, and `plugins/`, then starts from the package root so relative runtime paths resolve consistently.
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
- The current public tools are `search_metadata`, `execute_query`, `plan_encrypt_rule`, `apply_encrypt_rule`, `validate_encrypt_rule`, `plan_mask_rule`, `apply_mask_rule`, and `validate_mask_rule`.
- The encrypt and mask workflow targets logical databases exposed by ShardingSphere-Proxy; the dedicated usage notes appear below.
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
- The packaged distribution keeps the official MCP baseline jars, including encrypt and mask, under `lib/`.
- If your target database driver or an extra MCP feature jar is not already packaged, copy that jar under `plugins/` before running `bin/start.sh`.
- If you embed `shardingsphere-mcp-bootstrap` directly instead of using the packaged distribution, add the feature jars you need to that runtime classpath explicitly.
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

## Feature SPI Layout

The current MCP subchain is organized as `features + core + bootstrap`:

- `mcp/features/spi`
  - defines feature SPI, shared workflow models, descriptors, and common issue / response semantics
- `mcp/features/encrypt`
  - provides encrypt MCP tools, resources, and workflow implementation
- `mcp/features/mask`
  - provides mask MCP tools, resources, and workflow implementation
- `mcp/core`
  - provides capability, metadata, session, execute-query, and shared runtime services
- `mcp/bootstrap`
  - aggregates registered features through the MCP Java SDK and exposes HTTP / STDIO transports

Encrypt and mask are not special cases hardcoded in bootstrap. They are pluggable MCP features registered through the feature SPI layer.

## How to Add a New MCP Feature

If you want to add another feature beyond encrypt and mask, keep the implementation path minimal:

- Create `mcp/features/<feature>` and depend on `mcp/features/spi` plus the required domain modules only; do not depend on `mcp/bootstrap`
- If this is a new feature module, wire it into both the build and the runtime classpath: add it under `mcp/features/pom.xml`, then either add it to `distribution/mcp/pom.xml` when it should ship in the official packaged runtime or place the built jar under `plugins/` before startup when it should stay optional
- For each public tool, implement `ToolHandler` and provide a unique `MCPToolDescriptor`
- For each public resource, implement `ResourceHandler` and provide a unique URI pattern
- Implement one `MCPFeatureProvider` that returns the feature-owned tool and resource handlers
- Register `org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider` under `src/main/resources/META-INF/services/`
- Keep feature URIs under `shardingsphere://features/<feature>/...` so they do not leak into shared metadata paths
- `mcp/core` discovers feature providers through `ShardingSphereServiceLoader`, flattens their handlers, and validates global uniqueness; `mcp/bootstrap` only publishes the final protocol surface
- Tool names and resource URI patterns must stay globally unique; duplicate registrations are rejected during startup validation

The encrypt and mask modules are the recommended reference implementations.

## Encrypt and Mask Workflow over ShardingSphere-Proxy

These workflow tools allow an MCP client to plan, execute, and validate encrypt or mask rules for one logical column by using natural language, structured arguments, or both.
The goal is not to re-implement encryption inside MCP. Instead, MCP translates user intent into executable DDL, DistSQL, and validation steps for ShardingSphere-Proxy.
The tools and resources described below are registered by the encrypt and mask feature modules through SPI; bootstrap only aggregates and publishes them to the protocol layer.

### Prerequisites

- V1 supports `ShardingSphere-Proxy` only.
- The MCP runtime must connect to the logical database exposed by `ShardingSphere-Proxy`, not to the underlying storage database directly.
- The demo H2 runtime from the quick start is good for discovery and query verification. For encrypt or mask workflows, replace `runtimeDatabases` with a Proxy-backed logical database.
- The current workflow focuses on rules, metadata, and SQL executability validation. It does not migrate or backfill existing data.

### Related tools and resources

The encrypt feature exposes 3 tools:

- `plan_encrypt_rule`
  - resolves encrypt intent, asks follow-up questions when required, and produces derived-column plans, DDL, DistSQL, index plans, and validation strategy
- `apply_encrypt_rule`
  - executes the generated encrypt artifacts, or exports them without execution in `manual-only` mode
- `validate_encrypt_rule`
  - validates the encrypt result across DDL state, rule state, logical metadata, and SQL executability

The mask feature exposes 3 tools:

- `plan_mask_rule`
  - resolves mask intent, asks follow-up questions when required, and produces DistSQL plus validation strategy
- `apply_mask_rule`
  - executes the generated mask artifacts, or exports them without execution in `manual-only` mode
- `validate_mask_rule`
  - validates the mask result across rule state, logical metadata, and SQL executability

The workflow also adds these feature resources:

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

The `features/*/algorithms` resources include both built-in algorithms and custom SPI algorithms visible from the current Proxy instance, so a model can recommend from the actual runtime pool.

### Keep these rules in mind before you start

- Reuse the same `MCP-Session-Id` for the whole workflow. If `plan`, `apply`, and `validate` switch to another session, later calls fail because the plan belongs to a different MCP session.
- The first `plan_encrypt_rule` or `plan_mask_rule` call does not need `plan_id`; after the first response returns `plan_id`, reuse that same `plan_id` for every follow-up planning call, apply call, and validation call inside the same feature workflow.
- `plan_encrypt_rule` and `plan_mask_rule` only plan. They do not execute DDL or DistSQL.
- `apply_encrypt_rule` and `apply_mask_rule` execute artifacts. `validate_encrypt_rule` and `validate_mask_rule` only check the current state.
- Users always target logical databases, logical tables, and logical columns. In encrypt workflows MCP may plan and create physical derived columns, but the user-facing target is still the logical object exposed by Proxy.
- `schema` is optional. MCP auto-fills it when the logical database contains a single schema. If the schema cannot be resolved uniquely, MCP asks for it explicitly.
- `delivery_mode` only affects how the client presents the workflow. It does not change the generated artifacts. The execution behavior is controlled by `execution_mode`.
- Sensitive algorithm properties are masked in plan and apply responses and are never echoed back in clear text.
- Every `curl` example below assumes you have already completed MCP `initialize` and are reusing the same `SESSION_ID` plus its matching `PROTOCOL_VERSION`.

### Recommended call order

If you want one encrypt or mask workflow to run end to end without ambiguity, use this order:

1. Start with the feature-specific planner: `plan_encrypt_rule` for encrypt or `plan_mask_rule` for mask. Do not start from `apply`.
2. If the response is `status = clarifying`, read `pending_questions` and call the same feature-specific `plan_*_rule` again with the same `plan_id`.
3. If the response is `status = planned`, review `derived_column_plan`, `ddl_artifacts`, `distsql_artifacts`, and `index_plan`.
4. Call the matching `apply_*_rule`. If you want manual execution, set `execution_mode` to `manual-only`.
5. If apply returns `awaiting-manual-execution`, execute the returned `manual_artifact_package` against ShardingSphere-Proxy first, then continue.
6. Call the matching `validate_*_rule` and make sure the returned validation layers pass.
7. If validation fails, inspect `issues` and `mismatches`, then either finish the remaining apply steps or re-plan after fixing the environment.

### Interaction model

Every `plan_encrypt_rule` and `plan_mask_rule` call returns a global step list and the current step. The default workflow is:

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
  - `apply_encrypt_rule` or `apply_mask_rule` finished its execution path
- `awaiting-manual-execution`
  - `manual-only` was selected, so MCP exported artifacts without executing them
- `validated`
  - `validate_encrypt_rule` or `validate_mask_rule` passed

In addition:

- `delivery_mode` supports `all-at-once` and `step-by-step`
  - MCP echoes the selected mode in the plan response so the client can decide whether to present the whole plan at once or drive the conversation step by step
- `execution_mode` supports `review-then-execute` and `manual-only`
  - the former executes generated artifacts automatically, while the latter exports a manual artifact package only

### What to do next for each status

- `clarifying`
  - more input is required, so read `pending_questions` and call the same feature-specific `plan_*_rule` again with the same `plan_id`
- `planned`
  - the execution package is ready, so review the artifacts and continue with the matching `apply_*_rule`
- `completed`
  - automatic execution finished, so the next step is the matching `validate_*_rule`
- `awaiting-manual-execution`
  - `manual-only` was selected, so execute the returned artifacts manually first and then run validation
- `validated`
  - the workflow finished successfully
- `failed`
  - inspect `issues` and `mismatches` first, then decide whether to re-plan, finish a partial apply, or fix the environment before validating again

### The response fields you will read most often

In `plan_encrypt_rule` and `plan_mask_rule`, the most important fields are:

- `plan_id`
  - the workflow identifier reused by every follow-up step
- `status`
  - tells you whether to clarify, apply, or troubleshoot
- `pending_questions`
  - the missing pieces you must send back during the clarifying phase
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

In `apply_encrypt_rule` and `apply_mask_rule`, the most important fields are:

- `status`
- `issues`
- `step_results`
- `executed_ddl`
- `executed_distsql`
- `skipped_artifacts`
- `manual_artifact_package`

In `validate_encrypt_rule` and `validate_mask_rule`, the most important fields are:

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
      "name":"plan_encrypt_rule",
      "arguments":{
        "database":"logic_db",
        "table":"orders",
        "column":"status",
        "natural_language_intent":"Encrypt status reversibly without equality lookup and without like lookup",
        "algorithm_type":"AES",
        "primary_algorithm_properties":{"aes-key-value":"123456abc"}
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
      "name":"plan_encrypt_rule",
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
  "pending_questions": ["Please provide property `aes-key-value`."],
  "algorithm_recommendations": [
    {"algorithm_role": "primary", "algorithm_type": "AES"},
    {"algorithm_role": "assisted_query", "algorithm_type": "MD5"}
  ]
}
```

In real usage, store the returned `plan_id` first:

```bash
PLAN_ID='plan-xxx'
```

This means:

- do not call `apply_encrypt_rule` yet
- keep using the same `plan_id`
- send the missing `aes-key-value` back to `plan_encrypt_rule`

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
      "name":"plan_encrypt_rule",
      "arguments":{
        "plan_id":"'"${PLAN_ID}"'",
        "primary_algorithm_properties":{"aes-key-value":"123456abc"}
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

Step 3: apply the plan

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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

If natural language does not make the algorithm clear, or if a required property such as `aes-key-value` is missing, `plan_encrypt_rule` returns:

- `status = clarifying`
- `pending_questions`
- `algorithm_recommendations`
- `property_requirements`

Continue with the same `plan_id` and send the missing fields back to `plan_encrypt_rule` instead of creating a new plan.

#### Default derived-column conventions

- encrypt uses `*_cipher` by default
- equality lookup adds `*_assisted_query` plus the corresponding index plan
- like lookup adds `*_like_query`
- when a default name conflicts with an existing column, MCP appends a numeric suffix and returns the final name in `derived_column_plan`

#### Apply and validate

The default execution mode is `review-then-execute`:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

If you want MCP to export the artifacts without executing them, switch to `manual-only`:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-apply-2\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\",\"execution_mode\":\"manual-only\"}}}"
```

`manual-only` returns `manual_artifact_package` with:

- `ddl_artifacts`
- `index_plan`
- `distsql_artifacts`

If you need partial execution, `approved_steps` can be used to limit execution to `ddl`, `index_ddl`, or `rule_distsql`.
Partial execution is mainly for reviewed or staged rollouts. Until every required step has been executed, `validate_encrypt_rule` may fail as expected.

After execution, validate immediately:

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"encrypt-validate-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_encrypt_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
      "name":"plan_encrypt_rule",
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
- `pending_questions` is empty because `drop` does not need encrypt capability clarification
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
      "name":"plan_mask_rule",
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
      "name":"plan_mask_rule",
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
  "pending_questions": ["Please provide property `from-x`.", "Please provide property `to-y`."],
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
      "name":"plan_mask_rule",
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

Step 3: apply the mask rule

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-apply-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"apply_mask_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
```

Step 4: validate the final rule state

```bash
curl -sS http://127.0.0.1:18088/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -H "MCP-Session-Id: ${SESSION_ID}" \
  -H "MCP-Protocol-Version: ${PROTOCOL_VERSION}" \
  --data "{\"jsonrpc\":\"2.0\",\"id\":\"mask-validate-complete-1\",\"method\":\"tools/call\",\"params\":{\"name\":\"validate_mask_rule\",\"arguments\":{\"plan_id\":\"${PLAN_ID}\"}}}"
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
      "name":"plan_mask_rule",
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
- Prepare the module dependencies once before targeted local reproduction:

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

- Local reproduction for the LLM smoke lane:

```bash
docker run -d --rm --name ollama-mcp-llm-e2e -p 11434:11434 ollama/ollama:latest
docker exec ollama-mcp-llm-e2e ollama pull qwen3:1.7b
MCP_LLM_E2E_ENABLED=true \
MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1 \
MCP_LLM_MODEL=qwen3:1.7b \
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- Local reproduction for the LLM usability lane uses the same Ollama process:

```bash
MCP_LLM_E2E_ENABLED=true \
MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1 \
MCP_LLM_MODEL=qwen3:1.7b \
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

- The LLM smoke artifacts are written under `test/e2e/mcp/target/llm-e2e/`.
- The dedicated GitHub Actions entry point is `.github/workflows/mcp-llm-e2e.yml`, delivered as `workflow_dispatch` plus nightly schedule instead of a PR gate.
- `mcp/features/spi`: feature SPI, shared workflow models, descriptors, and common issue / response semantics
- `mcp/features/encrypt`: encrypt tools, resources, planning / apply / validation, and algorithm visibility assembly
- `mcp/features/mask`: mask tools, resources, planning / apply / validation, and algorithm visibility assembly
- `mcp/core`: capability, metadata, session, audit, execute-query contracts, shared runtime service assembly, JDBC runtime configuration, metadata discovery, `DatabaseRuntime` assembly, and the JDBC-backed runtime context factory
- `mcp/bootstrap`: MCP Java SDK based bootstrap, HTTP / STDIO transport, top-level config loading, feature SPI aggregation, and lifecycle management
- `distribution/mcp`: standalone packaging, scripts, config, Dockerfile
- `test/e2e/mcp`: end-to-end contract validation

For local debugging and richer semantic verification, prefer the integration tests in `mcp/bootstrap` and the E2E suite in `test/e2e/mcp`.
