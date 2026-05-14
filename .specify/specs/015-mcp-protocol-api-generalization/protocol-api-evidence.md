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

# MCP Protocol API Evidence

## Scope

This ledger records the current non-general MCP protocol-layer APIs, the evidence behind each finding, and the target disposition before implementation starts.

The package remains on branch `001-shardingsphere-mcp`.
No branch-changing Speckit command is allowed for this package.

## Source Baseline

- MCP base protocol and JSON-RPC result/error semantics: https://modelcontextprotocol.io/specification/2025-11-25/basic
- MCP lifecycle and capability negotiation: https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle
- MCP tools, structured content, output schema, ResourceLink, and tool execution error semantics: https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- MCP resources and resource contents: https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- MCP prompts: https://modelcontextprotocol.io/specification/2025-11-25/server/prompts
- MCP completion: https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion
- MCP list pagination: https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/pagination

Draft MCP changes, including `server/discover`, stateless requests, and `Mcp-Method` or `Mcp-Name` headers, are future compatibility risk only.

## Findings

### 1. Capabilities Resource Is Useful But Over-Labeled

Current evidence:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPModelFirstContractPayloadBuilder.java` labels `shardingsphere://capabilities` as `public_surface_source`.
- The same builder exposes `protocol_fields` containing ShardingSphere catalog fields such as `supportedResources`, `completionTargets`, `resourceNavigation`, and `protocolAvailability`.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java` emits these sections as a custom catalog payload.

Disposition:

- Keep `shardingsphere://capabilities`.
- Relabel it as ShardingSphere catalog guidance.
- Make official MCP initialization and list methods authoritative for protocol discovery.

### 2. Completion Routing Is Not Provider-Driven

Current evidence:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/completion/MCPCompletionService.java` routes by hardcoded argument names.
  These include `database`, `schema`, `table`, `column`, `index`, `sequence`, and `plan_id`.
- The same service routes encrypt and mask algorithms with `descriptor.getReference().contains("encrypt")`.
- `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/MCPHandlerProvider.java` exposes resource and tool handlers only.
- `mcp/features/encrypt` and `mcp/features/mask` depend on `mcp-support`, not `mcp-core`.

Disposition:

- Define the completion provider SPI or descriptor contract in `mcp/support` for this package.
- Use `mcp/api` only if a later design proves the provider signature can stay on pure API DTOs without descriptor or support-context dependency.
- Keep metadata and workflow runtime providers in `mcp/core`.
- Let feature-owned algorithm providers live with feature modules or be declared through feature descriptors.
- Do not add `shardingsphere-mcp-core` as a feature-module dependency for completion providers.
- Remove feature-name containment checks from generic completion dispatch.

### 3. Protocol Error Semantics Need An SDK Behavior Preflight

Current evidence:

- MCP Java SDK `1.1.2` provides `io.modelcontextprotocol.spec.McpError`, JSON-RPC error records, and SDK-side handlers.
  The SDK covers unknown tools, unknown resources, invalid prompts, and invalid completion references.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/MCPToolController.java` catches all exceptions and converts them into `MCPErrorResponse`.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/MCPResourceController.java` catches all exceptions and wraps them as successful resource content with `response_kind=error`.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java` always maps controller payloads into `ReadResourceResult`.

Disposition:

- First add bootstrap tests proving actual wire behavior for completely unknown tools and resources under MCP Java SDK `1.1.2` over both Streamable HTTP and STDIO.
- These tests must assert JSON-RPC `error` presence and normal `result` absence for SDK no-match cases.
- Then fix only the ShardingSphere-owned gaps, such as matched templates with missing handlers or controller-direct unsupported dispatch.
- Cover matched-template handler miss and controller-direct unsupported dispatch with unit tests when unit tests can fully exercise the internal boundary.
  Add E2E only when unit coverage cannot prove the behavior or when transport mapping changes.
- Keep SDK-native protocol error codes unless the preflight proves a ShardingSphere-owned adapter gap.
- Keep supported tool business failures as `CallToolResult.isError(true)` with actionable structured content.

Preflight result recorded on 2026-05-14:

- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/AbstractMCPWireBehaviorTest.java` sends raw JSON-RPC frames over Streamable HTTP and SDK STDIO transport.
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceUnknownWireBehaviorTest.java`
  verifies `resources/read` with `unsupported://resource` returns a JSON-RPC error for both transports.
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolUnknownWireBehaviorTest.java`
  verifies `tools/call` with `unsupported_tool` returns a JSON-RPC error for both transports.
- The tests assert JSON-RPC version and request ID preservation, `error.code` and `error.message` presence, normal `result` absence, resource `contents` absence, and no tool `isError` wrapper.
- Verification command:
  `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=MCPToolUnknownWireBehaviorTest,MCPResourceUnknownWireBehaviorTest -Dsurefire.failIfNoSpecifiedTests=false test`

## Adjacent Package Owner Map

- 013 MCP Protocol Field Standardization owns descriptor shape, YAML field names, official descriptor metadata placement, ResourceLink metadata naming,
  and internal naming for completion or navigation descriptor fields.
- 014 MCP Standard and E2E Hardening owns accepted public tool naming, tool annotations, output schema runtime validation, E2E packaging/distribution checks,
  default metadata pagination size, and pagination boundary tests.
- 015 MCP Protocol API Generalization owns discovery versus ShardingSphere catalog labeling, provider-driven completion dispatch, protocol error versus business error channels,
  application pagination labeling, explicit ResourceLink generation contracts, and feature planner API cleanup.

015 implementation must reference 013 or 014 when touching their owned areas and record why any additional work is necessary.

### 4. Application Pagination Should Not Be Renamed By Default

Current evidence:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/protocol/response/MCPItemsResponse.java` emits `has_more`, `next_page_token`, and `continuation_mode`.
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml` documents application pagination fields for the metadata search tool.
  These include `page_size`, `page_token`, `next_page_token`, `has_more`, and `continuation_mode`.
- Official MCP list pagination uses `cursor` and `nextCursor` for protocol list methods.

Rename option:

- Pros: field names can visually align with official MCP list pagination, reducing confusion for clients that look only at names.
- Cons: this would create a breaking tool payload change and force descriptor, handler, README, and client updates.
  It may also falsely imply that a tool result field is the same contract as MCP list pagination.
- Risk: a renamed business field such as `nextCursor` inside `structuredContent` could be mistaken for MCP list response `nextCursor`.

Retain-and-label option:

- Pros: avoids breaking existing ShardingSphere tool clients, preserves domain payload semantics, and cleanly separates MCP protocol list pagination from tool-specific application pagination.
- Cons: requires explicit documentation and schema wording so clients do not infer official MCP semantics from `has_more` or `next_page_token`.
- Risk: unclear wording can leave the original ambiguity in place.

Disposition:

- Retain current application pagination field names for existing tool payloads.
- Update descriptors, README content, and tests to label them as ShardingSphere application pagination.
- State explicitly that these fields are not MCP list `cursor` or `nextCursor`.
- Use MCP `cursor` and `nextCursor` only for official MCP list methods or any future protocol-list adapter.
- Reconsider a breaking rename only if the user explicitly requests a public API cleanup window.

### 5. ResourceLink Emission Uses An Implicit Payload Protocol

Current evidence:

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java` recursively scans arbitrary payload maps.
- Any map containing `uri` and `resource_kind` can become a `ResourceLink`.
- Ordering is coupled to field names such as `resources_to_read`, `resource`, `parent_resource`, and `next_resources`.

Disposition:

- Keep MCP `ResourceLink`.
- Replace recursive arbitrary map scanning with an explicit ResourceLink provider or resource-link contract.
- Preserve documented ordering and link limit with focused transport tests.

### 6. Generic Descriptor Validation Contains Public Tool Branches

Current evidence:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java` hardcodes required output fields for public tool names.
  These include `database_gateway_search_metadata`, `database_gateway_execute_query`, `database_gateway_execute_update`, workflow tools, and planner tools.
- The same validator hardcodes `database_gateway_search_metadata` output item fields.

Disposition:

- Keep generic descriptor validation generic.
- Move tool-specific output-shape checks into descriptor schemas, descriptor fixtures, or feature-owned validators.
- Keep side-effect and approval invariants as generic policy checks where possible.

### 7. Feature Planner Inputs Leak Cross-Feature Semantics

Current evidence:

- `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml` exposes mask planner fields with encryption-oriented names.
  These include `requires_decrypt`, `requires_equality_filter`, and `requires_like_query`.

Disposition:

- Rename or remove encryption-specific mask planner input fields.
- Preserve genuinely feature-neutral fields only when their names and descriptions are valid for both features.

## Accepted User Decisions

- Speckit 015 documentation may be updated before production implementation.
- Completion provider SPI should follow the recommended `mcp/support` location for this package.
- Protocol error work must start with MCP Java SDK `1.1.2` behavior verification over both Streamable HTTP and STDIO.
- Matched-template handler miss does not need E2E when unit tests fully cover the behavior; add E2E only when unit coverage is insufficient.
- Fresh-context doubt-driven review is explicitly allowed.
- Application pagination rename remains an explained design decision; current recommendation is retain-and-label.
- 013, 014, and 015 ownership boundaries may be recorded to prevent duplicate implementation.
