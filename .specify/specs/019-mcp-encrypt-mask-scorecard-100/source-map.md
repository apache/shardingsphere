<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Source Map: MCP Encrypt/Mask Scorecard 100

## Official Sources

- MCP Specification `2025-11-25`: `https://modelcontextprotocol.io/specification/2025-11-25`
- MCP Tools: `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- MCP Resources: `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- MCP Prompts: `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- MCP Transports: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- MCP Java SDK Server: `https://java.sdk.modelcontextprotocol.io/latest/server/`

## Source-Driven Decisions

- Tool results do not require Markdown. Official MCP tool results may contain text, resource links, embedded resources, and structured content.
  When structured content is returned, serialized JSON text fallback is recommended for compatibility.
- Tool `outputSchema` is optional, but when present the server must return schema-conforming `structuredContent`.
- Tool annotations are hints and must not replace security enforcement.
- Resource subscriptions, list-changed notifications, progress, cancellation, roots, sampling, logging, icons, and task execution support are score-affecting
  only when advertised, required by this package, or claimed as implemented.
- SDK-deferred fields must be documented instead of emitted through ShardingSphere-only metadata as if they were official MCP fields.

## 2026-05-15 Phase 2 Evidence Refresh

### Official MCP Baseline

- Lifecycle starts with `initialize`, negotiates protocol version and capabilities, then requires the client to send `notifications/initialized`.
- HTTP clients must send `MCP-Protocol-Version` on subsequent requests after initialization.
- During operation, both sides must respect the negotiated protocol version and only use negotiated capabilities.
- Tools are model-controlled, discovered with `tools/list`, and invoked with `tools/call`.
- Servers that support tools must declare the `tools` capability. `listChanged` only means the server emits tool-list change notifications.
- Tool definitions include `inputSchema`, optional `outputSchema`, optional annotations, optional `icons`, and optional `execution`.
- Tool names should be unique and use only ASCII letters, digits, underscore, hyphen, and dot.
- Tool results may include text, image, audio, resource links, embedded resources, and `structuredContent`.
- Tools that return `structuredContent` should also return serialized JSON as text for backward compatibility.
- When `outputSchema` is present, servers must return structured results that conform to it.
- Tool execution errors should be reported as tool results with `isError=true`; protocol errors remain JSON-RPC errors.
- Resources are read with `resources/read`; resource templates are listed with `resources/templates/list`.
- Resource subscriptions and list-changed notifications are optional capability-backed features.
- Prompts are user-controlled templates. Servers that support prompts must declare the `prompts` capability.
- Prompts are discovered with `prompts/list`, retrieved with `prompts/get`, and may expose arguments for customization.
- Completion supports prompt and resource-template arguments through `completion/complete`; servers that support it must declare `completions`.
- MCP list pagination uses opaque `cursor` request values and optional `nextCursor` response values.
- Paginated operations include `resources/list`, `resources/templates/list`, `prompts/list`, and `tools/list`.
- Authorization is optional. When HTTP authorization is enabled, MCP defines transport-level OAuth resource-server behavior.
- STDIO transports should retrieve credentials from the environment instead of following the HTTP authorization flow.
- Protected MCP servers act as OAuth resource servers and must expose protected resource metadata for authorization-server discovery.
- Streamable HTTP allows GET/SSE support but permits HTTP 405 when the server does not offer an SSE stream at that endpoint.

### MCP Java SDK `1.1.2` Local API Evidence

Local SDK jar inspected:

- `~/.m2/repository/io/modelcontextprotocol/sdk/mcp-core/1.1.2/mcp-core-1.1.2.jar`

`javap` evidence:

- `McpSchema.Tool` exposes `name`, `title`, `description`, `inputSchema`, `outputSchema`, `annotations`, and `meta`.
- `McpSchema.Tool.Builder` exposes `outputSchema`, `annotations`, and `meta`, but does not expose `icons` or `execution`.
- `McpSchema.Resource` exposes `size`, `annotations`, and `meta`.
- `McpSchema.Resource.Builder` exposes `size`, `annotations`, and `meta`, but does not expose `icons`.
- `McpSchema.ResourceTemplate.Builder` exposes `annotations` and `meta`, but does not expose `icons` or `size`.
- `McpSchema.CallToolResult.Builder` exposes `structuredContent`, `addTextContent`, `addContent`, `isError`, and `meta`.
- `McpSchema.ServerCapabilities.Builder` exposes `resources(subscribe, listChanged)`, `tools(listChanged)`, `prompts(listChanged)`,
  `completions`, `logging`, and `experimental`.

### Local Implementation Evidence

- `MCPTransportPayloadUtils#createCallToolResult` sets `structuredContent(payload)`, adds serialized JSON text content, propagates `isError`,
  and emits bounded official resource links.
- `MCPToolSpecificationFactory#createTool` maps descriptor input schema, output schema, annotations, and meta to the SDK `Tool` object.
- `MCPToolSpecificationFactory#createCallToolResult` validates success payloads against declared `outputSchema` and returns an `invalid_output_schema`
  tool-result error when validation fails.
- `MCPSyncServerFactory#create` advertises implemented resources, tools, prompts, and completions. Resource/tool/prompt list-change signals are disabled.
- `MCPResourceSpecificationFactory#createResource` maps official fixed resource `size` through the SDK-supported `Resource.size` field when present.
- `mcp/README.md` documents disabled subscriptions/listChanged, SDK-owned logging, future progress/cancellation/task support, SDK-deferred icons/execution,
  client-side roots/sampling, and non-sensitive elicitation scope.

### Command Evidence

```bash
./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPTransportPayloadUtilsTest,MCPSyncServerFactoryTest,MCPToolSpecificationFactoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: `30` tests run, `0` failures, `0` errors, `0` skipped.
- Covered evidence: tool result structured content and JSON fallback, resource links, output-schema validation path, elicitation fallback,
  and implemented capability advertisement.
- Scope note: this command was executed on the current dirty worktree. It proves the observed workspace passes the focused Phase 2 bootstrap evidence,
  but final score closure still requires rerunning the relevant gates after implementation changes settle.

## 2026-05-16 Phase 4 Safety Evidence Refresh

### Official MCP Safety Sources

- MCP Tools `2025-11-25`: tool annotations are hints, not security guarantees; clients should require human confirmation before invoking tools with side effects.
- MCP Authorization `2025-11-25`: HTTP authorization is enforced at the transport layer; bearer tokens are header credentials and protected-resource metadata is exposed through
  `WWW-Authenticate` and `/.well-known/oauth-protected-resource`.
- MCP Elicitation `2025-11-25`: elicitation requests must not ask users for sensitive information.

### Local Boundary Decisions

- Preview and approval remain in core workflow execution and HTTP E2E contract tests instead of feature-specific handlers.
- Session isolation remains in workflow session context, completion providers, and transport DELETE cleanup listeners.
- Encrypt/mask feature modules own rule and artifact planning; SQL identifier checks and algorithm literal formatting are centralized in `WorkflowSQLUtils`.
- Bootstrap HTTP authorization accepts bearer tokens only from the `Authorization` header; query-string tokens are ignored and rejected.

### Command Evidence

```bash
./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest=WorkflowSQLUtilsTest,EncryptRuleDistSQLPlanningServiceTest,PhysicalDDLPlanningServiceTest,MaskRuleDistSQLPlanningServiceTest,HttpBearerAuthorizationHandlerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: `42` focused unit tests run, `0` failures, `0` errors, `0` skipped.

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dtest=HttpTransportAccessTokenE2ETest,HttpTransportRecoveryE2ETest,HttpTransportCompletionE2ETest,HttpTransportApprovalSafetyE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: `23` programmatic HTTP E2E tests run, `0` failures, `0` errors, `0` skipped.

```bash
./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp
./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:check -B -ntp
```

- Exit codes: `0`, `0`

### Covered Phase 4 Claims

- T030: `HttpTransportApprovalSafetyE2ETest` proves preview-before-apply and explicit user approval for update and workflow apply operations.
- T031: `HttpTransportCompletionE2ETest`, `HttpTransportApprovalSafetyE2ETest`, and session lifecycle tests prove plan ID completion and approval stay session-scoped.
- T032: `MCPToolSpecificationFactoryTest`, `WorkflowArtifactMaskUtilsTest`, and `HttpTransportRecoveryE2ETest` prove non-sensitive elicitation and secret-safe planning/recovery payloads.
- T033: `HttpBearerAuthorizationHandlerTest`, `HttpTransportAccessTokenE2ETest`, OAuth introspection E2E, and origin policy E2E prove auth, no-token-passthrough, and fail-closed behavior.
- T034: `WorkflowSQLUtilsTest`, `EncryptRuleDistSQLPlanningServiceTest`, `MaskRuleDistSQLPlanningServiceTest`, and `PhysicalDDLPlanningServiceTest` prove identifier rejection and literal escaping.

## 2026-05-16 Phase 5 Architecture Evidence Refresh

### Official MCP Boundary Source

- MCP Tools `2025-11-25`: tool definitions include `inputSchema`; the schema follows JSON Schema usage guidelines, defaults to 2020-12 without `$schema`,
  and must be a valid JSON Schema object.
- MCP Tools `2025-11-25`: tool results may return `structuredContent`; serialized JSON text fallback is recommended for compatibility.

### Local Boundary Decisions

- `MCPToolArgumentContract` is a bounded pre-dispatch guard for required fields, primitive types, enum values, arrays, recursive objects, and unknown fields.
  It is not a full JSON Schema engine.
- `StatementClassifier` remains a lightweight MCP execution safety scanner. Full SQL grammar ownership stays in parser modules, so this package records boundaries
  instead of adding speculative parser behavior.
- `WorkflowFieldNames` is the shared workflow field-name vocabulary for encrypt/mask MCP planning arguments, payloads, inference evidence, and artifacts.
  External field names are unchanged.
- Production nullable returns were reduced where safe through `Optional<SQLCommonTableExpression>`. Remaining nulls are documented as tri-state, YAML/framework,
  lifecycle-copy, or external-normalization boundaries.
- Direct Mockito static/constructor mocks remain scoped with `try-with-resources` where `AutoMockExtension` is unavailable on the module test classpath or lacks the needed
  construction behavior.

### Command Evidence

```bash
./mvnw -pl mcp/core,mcp/support,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true \
  -Dtest=<focused support/core test set> \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: `111` focused support/core tests run, `0` failures, `0` errors, `0` skipped.
- Focused support/core test set: `MCPHandlerLoaderTest`, `WorkflowRuntimeDefinitionRegistryTest`, `MCPToolControllerTest`,
  `MCPResourceControllerTest`, `ToolHandlerRegistryTest`, `StatementClassifierTest`, `WorkflowRequestBinderTest`,
  `WorkflowPlanPayloadBuilderTest`, and `WorkflowGuidancePayloadBuilderTest`.

```bash
./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true \
  -Dtest=WorkflowRequestBinderTest,WorkflowPlanPayloadBuilderTest,EncryptToolHandlerTest,MaskToolHandlerTest,EncryptWorkflowPlanningServiceTest,MaskWorkflowPlanningServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: `38` focused support/encrypt/mask tests run, `0` failures, `0` errors, `0` skipped.

```bash
./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp
./mvnw -pl mcp/core -Pcheck -DskipTests -DskipITs spotless:apply -B -ntp
./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -Pcheck -DskipTests -DskipITs spotless:check -B -ntp
```

- Exit codes: `0`, `0`, `0`
- Result: scoped Checkstyle and Spotless gates passed.

### Covered Phase 5 Claims

- T040: nullable return search, one safe Optional refactor, and documented retained null semantics.
- T041: static/constructor mock search and documented try-with-resources exceptions.
- T042: workflow field-name duplication reduced without changing the MCP contract.
- T043: nested input-schema unknown-field test and SQL scanner non-inference test added.
- T044: historical score reconciliation documented in current and historical Speckit packages.

## Local Source Evidence Targets

- MCP module chain: `mcp/pom.xml`
- MCP Java SDK version: `mcp/bootstrap/pom.xml`
- Server capabilities and registration: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`
- Tool result payload mapping: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`
- Tool descriptor and output-schema enforcement: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`
- Input-schema contract: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java`
- Encrypt descriptor: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-encrypt.yaml`
- Mask descriptor: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-mask.yaml`
- Encrypt workflow planning: `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/service/EncryptWorkflowPlanningService.java`
- Mask workflow planning: `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/tool/service/MaskWorkflowPlanningService.java`
- Default E2E switches: `test/e2e/mcp/src/test/resources/env/e2e-env.properties`
- mcp-builder evaluation artifact: `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`

## Repository Standards

- `CODE_OF_CONDUCT.md` requires readability, cleanliness, consistency, simplicity, abstraction, successful build checks, and full unit-test coverage except simple getters/setters.
- `AGENTS.md` requires module-scoped verification, Checkstyle/Spotless, no branch switching for this task, and evidence-first reporting.
- `.specify/memory/constitution.md` requires Proxy-first logical abstraction, explicit operator control, minimal safe automation, deterministic naming, and complete verification before completion.
