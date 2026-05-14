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

# MCP Contract and E2E Finding Ledger

## Purpose

This ledger maps the current mcp-builder findings to Speckit owners.
It prevents package 016 from duplicating implementation work that is already owned by 012, 013, 014, or 015.

Source status:

- Source artifact available in the repository:
  `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`.
- The current gap list comes from the 2026-05-14 mcp-builder review conversation
  and the doubt-driven review loop for package 016.
- Exact official-source and repository-path evidence is recorded in `source-path-evidence.md`.
- Evidence mapping was refreshed on 2026-05-14 after source-driven and doubt-driven reanalysis.

Owner states:

- **tracked-only**: 016 records the gap, but another Speckit package owns implementation.
- **new-owner**: 016 owns the future task after overlap review found no adjacent package owner.
- **split-owner**: another package owns core implementation, and 016 owns E2E or evidence mapping.

## Adjacent Owner Rule

- 012 owns complete OAuth validation and scorecard closure.
- 013 owns descriptor field standardization, official field placement, and metadata naming.
- 014 owns accepted public tool naming, annotations, output-schema validation, distribution packaging,
  and PR E2E hardening.
- 015 owns protocol/domain API boundaries, completion dispatch, protocol error channels, ResourceLink contracts,
  prompt/catalog labeling, and planner API cleanup.
- 016 owns this inventory, unowned safety gaps, unowned lifecycle gaps, E2E disposition, and source-path evidence mapping.

## Finding Map

- **MCE-P0-001 Secret-safe elicitation**
  - Owner: 016, new-owner.
  - Source: MCP elicitation `2025-11-25`.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`;
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`;
    `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`.
  - Evidence gate: no form-mode request asks for passwords, API keys, access tokens, or payment credentials.
    Sensitive collection uses URL mode, OAuth, environment, or explicit out-of-band configuration.
    Implemented by T020/T024 on 2026-05-14: form elicitation now runs only for non-sensitive clarification questions;
    questions marked `secret`, using `input_type=secret`, or naming password/token/key/secret/credential fields stay in the tool response
    so the client can collect values through URL mode when available, a secret manager, a protected environment variable, or an operator-controlled channel.

- **MCE-P0-002 Strict Streamable HTTP negotiation**
  - Owner: 016, new-owner.
  - Source: MCP Streamable HTTP transport `2025-11-25`.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`.
  - Evidence gate: unsupported or missing `Accept` behavior matches the selected baseline.
    Implemented by T021/T025 on 2026-05-14: missing and unsupported `Accept` initialize requests now reject with HTTP 400 and do not create sessions.

- **MCE-P0-003 Remote HTTP origin policy**
  - Owner: 016, new-owner.
  - Source: MCP Streamable HTTP transport and mcp-builder DNS rebinding guidance.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`;
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java`;
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`;
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java`.
  - Evidence gate: configured allowed remote origins pass, unlisted remote origins fail, and loopback defaults remain explicit.
    Implemented by T022/T026 on 2026-05-14: remote HTTP requires explicit `allowedOrigins`, allowed origins pass, and missing,
    malformed, loopback-only, or unlisted origins fail with HTTP 403.

- **MCE-P0-004 Authorization fail-closed gate**
  - Owner: 012, tracked-only.
  - Source: MCP Authorization `2025-11-25`; OAuth introspection and bearer-token RFC sources stay in 012.
  - Affected paths:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java`;
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization`.
  - Evidence gate: 012 must cover inactive, expired, wrong issuer, wrong audience/resource, insufficient scope,
    introspection failure, 401/403 challenge, and no token passthrough.
    Cross-linked by T023 on 2026-05-14: package 016 keeps this as tracked-only, while package 012 remains the implementation and evidence owner
    through its complete OAuth token validation gate.

- **MCE-P1-001 Input-schema enforcement**
  - Owner: 016, new-owner.
  - Source: MCP tools and JSON Schema shape.
  - Affected paths:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArguments.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/MCPToolController.java`;
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`.
  - Evidence gate: required fields, enum values, types, and unknown-field policy are enforced before handler execution.
  - Closure evidence on 2026-05-14:
    `MCPToolArgumentContract` enforces the supported `inputSchema` subset before handler dispatch;
    `MCPToolArgumentContractViolationException` and recovery payload conversion return model-correctable errors;
    `ToolHandlerRegistryTest`, `MCPToolSpecificationFactoryTest`, and `HttpTransportContractE2ETest` cover registry, SDK-facing, and HTTP paths.

- **MCE-P1-002 Output-schema strictness**
  - Owner: 014, tracked-only.
  - Source: MCP tools structured content and output schema.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`;
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`;
    `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`;
    `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`;
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`;
    `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`.
  - Evidence gate: 014 records validation evidence or removes non-conforming schemas.
    Cross-linked by T035 on 2026-05-14: package 016 does not expand output-schema behavior; package 014 remains the strictness and descriptor conformance owner.

- **MCE-P1-003 Canonical enum casing**
  - Owner: 013, tracked-only.
  - Source: descriptor schema and ShardingSphere public contract consistency.
  - Affected paths:
    `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`;
    `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`;
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`;
    `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArguments.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArgumentsTest.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support`.
  - Evidence gate: one canonical public casing for `object_types` and related enums.
    Cross-linked by T035 on 2026-05-14: package 016 rejects values that do not exactly match declared enum entries, while package 013 remains the owner
    for choosing and documenting canonical public casing across descriptors, clients, scripts, and docs.

- **MCE-P1-004 Lifecycle initialized evidence**
  - Owner: 016, new-owner.
  - Source: MCP lifecycle `2025-11-25`.
  - Affected paths:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPHttpInteractionClient.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/AbstractHttpProgrammaticRuntimeE2ETest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/AbstractProcessMCPStdioInteractionClient.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`.
  - Evidence gate: HTTP and STDIO initialize flows include required initialized notification behavior,
    or a sourced HTTP exception is documented.
  - Closure evidence on 2026-05-14:
    T040/T044 updated HTTP programmatic and interaction-client helpers to send `notifications/initialized` after successful initialize and assert HTTP `202`;
    `HttpTransportContractE2ETest` now proves initialized notification behavior on the HTTP contract path, while the existing STDIO client already sends it.

- **MCE-P1-005 Positive completion coverage**
  - Owner: 015 and 016, split-owner.
  - Source: MCP completion `2025-11-25`.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactory.java`;
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactoryTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionH2RuntimeSmokeE2ETest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyEncryptWorkflowE2ETest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyMaskWorkflowE2ETest.java`.
  - Evidence gate: 015 owns provider dispatch; 016 tracks positive E2E coverage for metadata, algorithms, and workflow plans.
  - Closure evidence on 2026-05-14:
    T042 added positive HTTP E2E coverage for prompt and resource-reference completion across database, schema, table, column, index, sequence, algorithms,
    and current-session workflow `plan_id`; descriptor validation now rejects prompt completion targets that name undeclared prompt arguments.
    Package 015 remains owner for broader completion dispatch/API generalization.

- **MCE-P1-006 Resource URI encoding boundaries**
  - Owner: 016, new-owner.
  - Source: MCP resources and resource templates `2025-11-25`.
  - Affected paths:
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/resource/MCPUriTemplateUtils.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/uri/MCPUriPattern.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/uri/MCPUriPatternTest.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolServiceTest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/MetadataDiscoveryE2ETest.java`.
  - Evidence gate: implementation has unit evidence; 016 still needs product E2E evidence for encoded spaces,
    slashes, reserved characters, malformed encoding, and missing variables.
  - Closure evidence on 2026-05-14:
    T041 added strict UTF-8 path-segment decoding, malformed percent-encoding rejection, raw template-marker rejection,
    support/core unit coverage, and `MetadataDiscoveryE2ETest` product E2E coverage for encoded spaces, encoded slashes, reserved characters,
    malformed encoding, and unexpanded resource-template variables.

- **MCE-P1-007 Session and transaction isolation**
  - Owner: 016, new-owner.
  - Source: MCP Streamable HTTP session behavior.
  - Affected paths:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSessionLifecycleE2ETest.java`;
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/session`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow`;
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow`.
  - Evidence gate: two-client session, workflow completion, transaction cleanup, and DELETE behavior are isolated.
  - Closure evidence on 2026-05-14:
    T043/T045 added two-client E2E for DELETE session isolation, transaction isolation, DELETE rollback cleanup, workflow approval rejection,
    and current-session plan-id completion. The new workflow approval E2E exposed a cross-session plan reuse defect; `WorkflowSessionSnapshotResolver`
    now enforces session ownership before continuing, validating, or applying a plan.

- **MCE-P1-008 Registry manifest schema**
  - Owner: 014 and 016, split-owner.
  - Source: official MCP Registry `server.json` documentation and release scripts.
  - Affected paths:
    `mcp/server.json`;
    `.github/workflows/resources/scripts/prepare-mcp-server-json.py`;
    `.github/workflows/jdk21-subchain-ci.yml`;
    `.github/workflows/mcp-build.yml`;
    `distribution/mcp/Dockerfile`;
    `mcp/README.md`.
  - Evidence gate: registry schema `2025-12-11`, package transport mapping,
    release version hygiene, and OCI publication metadata are validated.
  - Closure evidence on 2026-05-14:
    T050/T051 added script-level coverage and release-gate validation for schema URL, description length,
    stdio and Streamable HTTP package transports, OCI identifier/version alignment, required package environment variables,
    and release-only SNAPSHOT rejection. `mcp/server.json` now includes a schema-compliant description and packaged Streamable HTTP URL.

- **MCE-P2-001 Optional MCP capabilities**
  - Owner: 016, new-owner.
  - Source: MCP logging, progress, cancellation, tasks, roots, sampling, and resource subscriptions docs.
  - Affected paths:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandler.java`;
    `mcp/README.md`.
  - Evidence gate: unimplemented capabilities are absent from advertised capabilities and documented as unsupported or future work.

- **MCE-P2-002 ShardingSphere feature breadth**
  - Owner: 016, new-owner.
  - Source: ShardingSphere MCP product scope.
  - Affected paths:
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`;
    `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`;
    `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`;
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`;
    `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`;
    `mcp/README.md`.
  - Evidence gate: sharding, readwrite-splitting, shadow, traffic, database discovery, mode governance,
    and observability are marked supported, unsupported, or future scope.

- **MCE-P2-003 Prompt/resource catalog clarity**
  - Owner: 015, tracked-only.
  - Source: MCP prompts and resources `2025-11-25`.
  - Affected paths:
    `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`;
    `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`;
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`;
    `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`;
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`;
    `mcp/README.md`.
  - Evidence gate: official MCP objects and ShardingSphere guidance are labeled separately.

- **MCE-P2-004 Error recovery stability**
  - Owner: 015 and 016, split-owner.
  - Source: MCP protocol error and tool error channels.
  - Affected paths:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportRecoveryE2ETest.java`;
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java`;
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/response`.
  - Evidence gate: tests assert stable codes and structured recovery fields, not volatile display prose.
