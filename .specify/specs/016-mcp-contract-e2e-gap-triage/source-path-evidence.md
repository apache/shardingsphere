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

# MCP Source and Path Evidence

## Purpose

This file closes the source-path evidence gap for package 016.
It records official MCP sources, current repository evidence, and the concrete target layer for each finding.

## Source Baselines

- Protocol baseline: MCP Specification `2025-11-25`, locked by user decision on 2026-05-14.
- Registry metadata baseline: official MCP Registry `server.json` schema
  `https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json`.
- Registry schema `2025-12-11` is not a protocol-baseline drift.
  It is the current official metadata schema used by `mcp/server.json`.

Official source URLs:

- Lifecycle: https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle
- Streamable HTTP transport: https://modelcontextprotocol.io/specification/2025-11-25/basic/transports
- Tools: https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resources: https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- Elicitation: https://modelcontextprotocol.io/specification/2025-11-25/client/elicitation
- Completion: https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion
- Registry: https://modelcontextprotocol.io/registry/about

## Evidence Map

- **MCE-P0-001 Secret-safe elicitation**
  - Source: MCP elicitation forbids using elicitation to collect sensitive values.
  - SDK evidence:
    `io.modelcontextprotocol.spec.McpSchema$ElicitRequest` in MCP Java SDK `1.1.2` exposes message, requestedSchema, and meta only,
    so URL-mode elicitation is not available through the current Java SDK surface.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java:50-101`
    checks clarification questions before form elicitation and blocks questions marked `secret`, using `input_type=secret`,
    or naming password/token/key/secret/credential fields.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java:136-140`
    keeps form schemas non-sensitive and no longer emits password-format form fields.
  - Closure evidence:
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java:168-221`
    proves normal non-sensitive form elicitation still works and secret-bearing, secret-input-type, and sensitive-field-name questions do not call `createElicitation`.
  - Closure evidence:
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/MCPDocumentationContractTest.java:79-95`
    pins README coverage for MCP form elicitation, URL mode, and secret manager guidance.
  - Closure evidence:
    `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`
    and `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`
    document that secret-marked workflow questions require out-of-band client or operator handling instead of MCP form elicitation.
  - Target: closed for the form-elicitation safety slice. Future URL-mode implementation remains blocked on an SDK surface that exposes URL-mode request fields.

- **MCE-P0-002 Strict Streamable HTTP negotiation**
  - Source: MCP Streamable HTTP clients include explicit `Accept` negotiation for JSON and SSE response types.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java:137-166`
    delegates original GET, POST, and DELETE requests without injecting a fallback `Accept` header.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java:71-84`
    rejects initialize without an `Accept` header.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java:85-93`
    rejects POST initialize with JSON-only `Accept`.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java:95-103`
    rejects POST initialize with SSE-only `Accept`.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java:105-123`
    rejects GET event stream requests with missing or JSON-only `Accept`.
  - Target: closed by `StreamableHttpMCPServletTest`,
    `HttpTransportContractE2ETest#assertRejectInitializeWithoutAcceptHeader`, and
    `HttpTransportContractE2ETest#assertRejectInitializeWithUnsupportedAcceptHeader`.

- **MCE-P0-003 Remote HTTP origin policy**
  - Source: MCP Streamable HTTP recommends validating `Origin` to prevent DNS rebinding.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
    declares `allowedOrigins`, validates HTTP or HTTPS origin syntax, and requires non-empty origins for non-loopback bindings.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java`
    and `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`
    expose `transport.http.allowedOrigins` with environment placeholder support.
  - Closure evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java`
    selects `LoopbackOriginHeaderConstraint` for loopback bindings and `AllowedOriginHeaderConstraint` for remote bindings.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java`
    accepts configured allowed remote origins and rejects unlisted, missing, malformed, and loopback-only remote origins with HTTP 403.
  - Target: closed by `AllowedOriginHeaderConstraintTest`, `ServerTransportSecurityValidatorFactoryTest`,
    `YamlHttpTransportConfigurationSwapperTest`, `MCPLaunchConfigurationTest`, and `HttpTransportSecurityE2ETest`.

- **MCE-P0-004 Authorization fail-closed gate**
  - Source: MCP Authorization plus OAuth bearer-token and introspection behavior.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java:81-115`
    covers valid token, wrong resource, and insufficient scope.
  - Current evidence: OAuth unit tests cover more branches than product E2E.
  - Target: package 012 owns inactive token, expired token, wrong issuer, introspection failure,
    401/403 challenge, and no-token-passthrough evidence.

- **MCE-P1-001 Input-schema enforcement**
  - Source: MCP tool `inputSchema` is the client-visible argument contract.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java:77`
    now validates arguments through the declared schema before handler execution.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java:93`
    covers required fields and required text values.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java:116`
    covers unknown arguments when `additionalProperties` is false.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java:128`
    covers primitive types, arrays, objects, and declared enum values.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/exception/MCPToolArgumentContractViolationException.java:30`
    represents generic contract violations that remain recoverable by the model.
  - Closure evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPBasicRecoveryPayloadFactory.java:101`
    converts schema violations into stable recovery payloads.
  - Closure evidence:
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java:145`
    covers registry-level invalid type, invalid enum, invalid execution mode, invalid approved steps, and unknown argument cases.
  - Closure evidence:
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java:132`
    covers the SDK-facing `CallToolResult` error path.
  - Closure evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java:319`
    covers HTTP invalid input and stable recovery fields.

- **MCE-P1-002 Output-schema strictness**
  - Source: MCP structured output should conform to declared `outputSchema`.
  - Current evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java:127-132`
    validates structured output before creating the call result.
  - Current evidence:
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java:132-159`
    covers invalid output and descriptor examples.
  - Target: package 014 owns remaining descriptor conformance and accepted public contract work.

- **MCE-P1-003 Canonical enum casing**
  - Source: descriptor schema and public API consistency.
  - Current evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArguments.java:133-183`
    parses integer arguments with defaults and bounded checks, while enum-like list validation is handler-specific.
  - Current evidence:
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java:66-139`
    covers descriptor dispatch and required arguments, but not one canonical public casing policy.
  - Target: package 013 owns descriptor field standardization and enum casing.

- **MCE-P1-004 Lifecycle initialized evidence**
  - Source: MCP lifecycle requires an initialized notification after successful initialize unless a sourced transport-specific exception applies.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/AbstractProcessMCPStdioInteractionClient.java:117`
    sends `notifications/initialized`.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPHttpInteractionClient.java:49-70`
    and `AbstractHttpProgrammaticRuntimeE2ETest.java:49-52` initialize HTTP sessions without the notification.
  - Target: HTTP helper lifecycle contract, HTTP E2E contract, and a sourced exception note if the SDK transport handles it internally.

- **MCE-P1-005 Positive completion coverage**
  - Source: MCP completion returns successful suggestions for supported argument, prompt, and resource-reference targets.
  - Current evidence:
    `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactoryTest.java:49-154`
    covers positive unit-level providers and negative cases.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionH2RuntimeSmokeE2ETest.java:97-100`
    has one H2 schema completion smoke.
  - Target E2E: metadata table, column, index, and sequence completion in `HttpTransportContractE2ETest`
    or `MetadataDiscoveryE2ETest`; algorithm completion in encrypt and mask workflow E2E; plan-id completion in workflow approval E2E.

- **MCE-P1-006 Resource URI encoding boundaries**
  - Source: MCP resources use URI templates, and resource-not-found should be surfaced as protocol errors.
  - Current evidence:
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/resource/MCPUriTemplateUtils.java:83-90`
    percent-encodes URI variables.
  - Current evidence:
    `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/uri/MCPUriPattern.java:127-140`
    decodes URI variables.
  - Current evidence:
    `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/uri/MCPUriPatternTest.java:73-78`
    and `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolServiceTest.java:196-203`
    already cover encoded value parsing at unit scope.
  - Target E2E: `MetadataDiscoveryE2ETest` product-level encoded spaces, encoded slashes,
    reserved characters, malformed encoding, missing variables, and unsupported resources.

- **MCE-P1-007 Session and transaction isolation**
  - Source: Streamable HTTP sessions and DELETE lifecycle behavior.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSessionLifecycleE2ETest.java:39-79`
    covers delete and missing-session behavior.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java:87-98`
    covers cross-database transaction switching in one session.
  - Target: two-client HTTP isolation, transaction cleanup after DELETE, workflow approval cleanup,
    and completion-plan isolation across sessions.

- **MCE-P1-008 Registry manifest schema**
  - Source: MCP Registry publishes server metadata through `server.json`; the official schema URL is dated `2025-12-11`.
  - Current evidence:
    `mcp/server.json:2` already points at
    `https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json`.
  - Current evidence:
    `.github/workflows/resources/scripts/prepare-mcp-server-json.py:40-45`
    rewrites version and identifier fields, but does not validate against the official schema.
  - Current evidence:
    `.github/workflows/jdk21-subchain-ci.yml:137-159` and `.github/workflows/mcp-build.yml:105-127`
    validate selected metadata fields, but not full schema conformance or publication metadata.
  - Target: release-gate schema validation in both workflows, plus script-level coverage for rewrite behavior.

- **MCE-P2-001 Optional MCP capabilities**
  - Source: MCP optional capability docs for logging, progress, cancellation, roots, sampling, subscriptions, and resource list changes.
  - Current evidence:
    `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java:84`
    enables resources, tools, prompts, and completions only.
  - Current evidence:
    `mcp/README.md:160-205` describes descriptor-driven discovery without an explicit unsupported-capability matrix.
  - Target: capability payload evidence and README scope table.

- **MCE-P2-002 ShardingSphere feature breadth**
  - Source: ShardingSphere MCP product scope.
  - Current evidence:
    `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java:77-84`
    builds catalog payload from descriptors and protocol availability.
  - Target: supported, unsupported, or future classification for sharding, readwrite-splitting,
    shadow, traffic, database discovery, mode governance, and observability.

- **MCE-P2-003 Prompt/resource catalog clarity**
  - Source: MCP prompt and resource objects.
  - Current evidence: package 015 owns prompt/catalog labeling and ResourceLink contracts.
  - Target: tracked only in 016 unless package 015 reassigns documentation evidence.

- **MCE-P2-004 Error recovery stability**
  - Source: MCP separates JSON-RPC protocol errors from tool-result errors.
  - Current evidence:
    `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportRecoveryE2ETest.java`
    is the E2E row to downscope toward stable structured fields.
  - Target: package 015 owns canonical error channels; package 016 owns E2E assertion discipline.

## Missing-Test Target Split

- Remote origin allowlist:
  - Unit: `ServerTransportSecurityValidatorFactoryTest` and origin-constraint tests.
  - Configuration: `YamlHttpTransportConfigurationSwapperTest`.
  - E2E: `HttpTransportSecurityE2ETest`.

- Input-schema rejection:
  - Unit: `MCPToolArgumentContractTest` or `ToolHandlerRegistryTest`.
  - Bootstrap: `MCPToolSpecificationFactoryTest`.
  - E2E smoke: `HttpTransportContractE2ETest`.

- Positive completion:
  - Unit: existing completion provider and `MCPCompletionSpecificationFactoryTest` coverage.
  - E2E metadata: `HttpTransportContractE2ETest`, `MetadataDiscoveryE2ETest`, or `ProductionH2RuntimeSmokeE2ETest`.
  - E2E algorithm/workflow: `HttpProductionProxyEncryptWorkflowE2ETest`,
    `HttpProductionProxyMaskWorkflowE2ETest`, and workflow approval E2E.

- Registry manifest:
  - Script: `.github/workflows/resources/scripts/prepare-mcp-server-json.py` rewrite coverage.
  - Release gate: `.github/workflows/jdk21-subchain-ci.yml` and `.github/workflows/mcp-build.yml`.
  - Runtime distribution smoke only when package startup or plugin discovery evidence is needed.

## Reanalysis Decisions

- Registry schema date is now split from protocol baseline.
  `mcp/server.json` using `2025-12-11` is correct registry metadata evidence, not a protocol-version defect.
- Resource URI encoding is not an implementation-missing finding.
  Unit coverage exists; package 016 keeps only the product E2E boundary gap.
- Helper-unit tests under `test/e2e/mcp` are preserved as `KEEP-SUPPORT` unless a narrower harness-support module exists.
  They should not be deleted directly and should not be counted as product-path E2E release evidence.
- Golden and recovery tests remain `REDUCE-CANDIDATE` only where their preserving contract evidence is named.
