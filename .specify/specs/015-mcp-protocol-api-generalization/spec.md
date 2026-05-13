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

# Feature Specification: MCP Protocol API Generalization

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-13
**Status**: Accepted
**Input**: User requests that the latest source-driven and mcp-builder protocol-layer review be accepted through Speckit, without switching branches.

## Goal

Convert the protocol-layer API review into an implementation-ready Speckit requirement package.
The package captures APIs that are non-standard, non-general, or ShardingSphere-specialized in the current MCP protocol layer.

The target is not another broad product polish pass.
The target is a smaller boundary cleanup.
Official MCP protocol APIs stay official, and ShardingSphere product guidance stays clearly namespaced or domain-owned.
Protocol-layer code stops routing behavior by concrete feature names or tool names.

## Review Baseline

The 2026-05-13 protocol review found these open gaps:

- `shardingsphere://capabilities` currently acts as a second discovery protocol instead of a clearly labeled ShardingSphere catalog resource.
- `MCPCompletionService` hardcodes metadata argument names, workflow plan identifiers, and encrypt/mask algorithm routing.
- `MCPDescriptorCatalogValidator` validates specific public tool names and specific `database_gateway_search_metadata` item fields.
- Resource read failures are returned as successful JSON resource contents with `response_kind=error`.
- `MCPResponse` exposes a generic map payload, while `response_mode` and other ShardingSphere business fields look protocol-adjacent.
- List-like business payloads use `page_token`, `next_page_token`, `has_more`, and `continuation_mode`, which can be confused with MCP pagination.
- Feature prompts reuse tool names, making prompt discovery less user-controlled and less generic.
- Mask planning input carries encryption-oriented intent fields such as `requires_decrypt`, `requires_equality_filter`, and `requires_like_query`.
- ResourceLink emission scans arbitrary payload maps for `uri` plus `resource_kind`, which couples transport behavior to ShardingSphere payload hints.
- The SDK list-response mapper patches official fields missing from the current Java SDK; this is acceptable only as an isolated adapter workaround.

## Source Baseline

Implementation MUST use the stable MCP `2025-11-25` specification as the source baseline:

- Base protocol and JSON-RPC result/error semantics: https://modelcontextprotocol.io/specification/2025-11-25/basic
- Lifecycle and capability negotiation: https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle
- Tools, descriptors, structured content, output schema, ResourceLink, and tool error semantics: https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resources and resource content semantics: https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- Prompts and prompt messages: https://modelcontextprotocol.io/specification/2025-11-25/server/prompts
- MCP pagination for list methods: https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/pagination

Draft MCP changes, including `server/discover`, stateless requests, and `Mcp-Method` or `Mcp-Name` headers, are tracked only as future compatibility risk.
They are not implementation requirements for this package.

## Clarifications

### Session 2026-05-13

- Q: Should this package switch branches or run Speckit branch scripts? -> A: No.
  The package is maintained manually on the current branch.
- Q: Is `shardingsphere://capabilities` forbidden? -> A: No.
  It may remain as a ShardingSphere catalog resource, but it MUST NOT be described as the MCP protocol discovery source of truth.
- Q: Are ShardingSphere business payload fields forbidden? -> A: No.
  They may remain as business payload fields, but they MUST NOT be documented, named, or validated as official MCP protocol fields.
- Q: Should MCP draft behavior be implemented now? -> A: No.
  Draft behavior is only a design risk unless stakeholders explicitly change the target protocol.
- Q: Should implementation keep old specialized APIs for compatibility? -> A: Compatibility aliases are out of scope unless a future requirement explicitly asks for them.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Speckit files are maintained manually because the standard feature creation flow can change branches.
- Existing dirty worktree changes are preserved and must not be reverted.
- Requirement work in this package changes documentation only.
- Implementation tasks must follow `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- Protocol-layer code MUST NOT depend on concrete ShardingSphere feature names, concrete public tool names, or string containment checks for routing.
- Official MCP APIs and ShardingSphere domain guidance MUST have explicit ownership boundaries.
- Runtime safety decisions MUST remain typed and internal.
  Exposed metadata may describe derived behavior but MUST NOT drive execution or approval decisions.
- Side-effecting workflows MUST preserve explicit preview and operator approval boundaries.

## User Scenarios and Testing

### User Story 1 - MCP Clients Use Official Discovery First (Priority: P1)

An MCP client should discover the server through official MCP initialization and list methods.
The optional ShardingSphere capabilities resource should be useful product guidance, not a substitute protocol.

**Why this priority**: A custom catalog that looks like protocol discovery makes clients depend on ShardingSphere-specific fields and weakens interoperability.

**Independent Test**: Initialize the server and compare `tools/list`, `resources/list`, `resources/templates/list`, and `prompts/list` with `shardingsphere://capabilities`.
Official list responses must remain sufficient for protocol discovery.
The capabilities resource must label custom sections as ShardingSphere catalog metadata.

**Acceptance Scenarios**:

1. **Given** a client has initialized the MCP server, **When** it lists tools, resources, templates, and prompts, **Then** it can discover official MCP objects.
   It does not need to read `shardingsphere://capabilities` first.
2. **Given** a client reads `shardingsphere://capabilities`, **When** it inspects custom catalog sections, **Then** those sections are labeled as ShardingSphere catalog metadata.
   They are not labeled as official MCP protocol fields.
3. **Given** server instructions mention `shardingsphere://capabilities`, **When** a generic MCP client ignores that resource, **Then** protocol discovery still works through official MCP methods.

---

### User Story 2 - Completion and Navigation Are Descriptor or SPI Driven (Priority: P1)

A maintainer should be able to add a new MCP feature without editing protocol-layer switch logic for argument names, feature names, or tool names.

**Why this priority**: Completion and navigation are extensibility surfaces. Hardcoded encrypt/mask logic makes the protocol layer a product-specific router.

**Independent Test**: Add a representative feature completion target through descriptor or SPI test fixtures.
Verify completion dispatch resolves candidates without changing `MCPCompletionService` feature-name conditionals.

**Acceptance Scenarios**:

1. **Given** a completion request targets a metadata argument, **When** the request is handled, **Then** metadata completion is resolved by a registered provider.
   It is not resolved by hardcoded argument branches.
2. **Given** a completion request targets an algorithm argument, **When** the reference is encrypt or mask related, **Then** the algorithm source is selected by descriptor or provider metadata.
   It is not selected by `reference.contains("encrypt")`.
3. **Given** a new feature registers completion targets, **When** the server starts, **Then** no protocol-layer Java change is needed to route that feature's completions.

---

### User Story 3 - Protocol Errors and Business Errors Stay Separate (Priority: P1)

A client should be able to distinguish MCP protocol failures from ShardingSphere business or workflow failures.

**Why this priority**: Official MCP uses JSON-RPC errors for protocol failures and tool results with `isError` for tool execution errors.
Returning every failure as domain JSON makes clients treat protocol failures as successful resource content.

**Independent Test**: Call an unsupported tool, an unsupported resource URI, malformed tool arguments, and a business validation failure.
Verify each case uses the required MCP error channel or tool result channel.

**Acceptance Scenarios**:

1. **Given** an unknown resource URI, **When** a client calls `resources/read`, **Then** the server returns a protocol-level error.
   It does not return a successful resource content item containing `response_kind=error`.
2. **Given** an unknown tool name, **When** a client calls `tools/call`, **Then** the server returns a protocol-level error rather than a ShardingSphere recovery payload as a normal tool result.
3. **Given** a supported tool receives business-invalid input, **When** the tool handles the request, **Then** the result uses `isError: true` with actionable structured feedback.

---

### User Story 4 - Business Payload APIs Are Clearly Domain-Owned (Priority: P2)

A model or automation client should know which payload fields are ShardingSphere business contract fields and which fields are MCP protocol fields.

**Why this priority**: Fields such as `response_mode`, `next_actions`, `resource_kind`, `next_page_token`, and workflow guidance are useful.
They should not masquerade as generic MCP constructs.

**Independent Test**: Inspect descriptors, capabilities payloads, README snippets, and schema validation tests.
Verify custom payload fields are documented as ShardingSphere business payload fields and are not named or validated as official MCP descriptor fields.

**Acceptance Scenarios**:

1. **Given** a tool returns `response_mode` or `next_actions`, **When** the payload is documented, **Then** the field is described as a ShardingSphere business payload field.
2. **Given** a list-like tool payload uses `next_page_token`, **When** the field is documented, **Then** it is described as application pagination and not MCP `nextCursor`.
3. **Given** ResourceLink emission uses resource hints, **When** a transport test is reviewed, **Then** the source of resource links is an explicit contract.
   It is not arbitrary recursive map scanning.

---

### User Story 5 - Feature Planner Inputs Do Not Leak Cross-Feature Semantics (Priority: P2)

A feature planner should expose input fields that match the feature's own domain semantics.
Mask planning should not expose encryption-specific flags unless they are renamed into feature-neutral query or sensitivity requirements.

**Why this priority**: Cross-feature copy-forward creates confusing APIs that are difficult for clients and future maintainers to generalize.

**Independent Test**: List encrypt and mask planner input schemas.
Verify shared fields are genuinely feature-neutral and feature-specific fields are scoped to their feature.

**Acceptance Scenarios**:

1. **Given** `database_gateway_plan_mask_rule`, **When** its input schema is listed, **Then** it does not expose encryption-specific evidence fields such as `requires_decrypt`.
2. **Given** encrypt and mask planners share a field, **When** the field is documented, **Then** the field name and description are valid for both features.
3. **Given** a planner prompt and tool are related, **When** prompts are listed, **Then** the prompt name describes user guidance rather than duplicating the tool name.

## Edge Cases

- `database_gateway_` as a service prefix is not itself a protocol violation; the issue is protocol-layer logic that depends on concrete prefixed names.
- `shardingsphere://` is a valid custom URI scheme for MCP resources; the issue is treating the custom catalog payload as official MCP discovery.
- Application-level pagination is allowed in a tool payload, but it must be clearly named and documented as a domain payload contract when it differs from MCP `cursor` and `nextCursor`.
- SDK gap adapters may remain while MCP Java SDK `1.1.2` lacks some official fields, but adapter workarounds must stay isolated in `mcp/bootstrap`.
- Prompt guidance may render a single user message when appropriate, but prompt names should stay user-facing and not merely mirror tool identifiers.

## Requirements

### Functional Requirements

- **MPAG-FR-001**: The requirement package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MPAG-FR-002**: Official MCP discovery through initialization and list methods MUST be sufficient without requiring `shardingsphere://capabilities`.
- **MPAG-FR-003**: `shardingsphere://capabilities` MAY remain, but it MUST label custom sections as ShardingSphere catalog metadata and not as MCP protocol fields.
- **MPAG-FR-004**: Server instructions MAY recommend reading `shardingsphere://capabilities`, but MUST NOT imply that official MCP discovery depends on that resource.
- **MPAG-FR-005**: Protocol-layer completion dispatch MUST be provider-driven or descriptor-driven.
  It MUST NOT hardcode feature names, tool names, or string containment checks such as `reference.contains("encrypt")`.
- **MPAG-FR-006**: Metadata completion for database, schema, table, column, index, and sequence MUST be represented as registered completion provider behavior.
  It MUST NOT be represented as hardcoded branches in the protocol dispatcher.
- **MPAG-FR-007**: Workflow plan ID completion MUST be represented as a registered completion provider behavior.
  It MUST NOT be represented as a special `plan_id` branch in the protocol dispatcher.
- **MPAG-FR-008**: Descriptor validation MUST separate generic MCP/JSON Schema validation from tool-specific business validation.
- **MPAG-FR-009**: Tool-specific output-field validation MUST move out of generic descriptor validation into descriptor-driven schemas, fixture tests, or feature-owned validators.
- **MPAG-FR-010**: Unsupported resource URIs MUST surface through MCP protocol error semantics instead of successful resource contents with `response_kind=error`.
- **MPAG-FR-011**: Unsupported tool names MUST surface through MCP protocol error semantics instead of successful tool results with ShardingSphere recovery payloads.
- **MPAG-FR-012**: Supported tool execution errors SHOULD use `isError: true` and SHOULD include actionable structured feedback for model self-correction.
- **MPAG-FR-013**: ShardingSphere business payload fields such as `response_mode`, `next_actions`, `recovery`, `resource_kind`, and workflow guidance MUST be domain payload fields.
  They MUST be documented and validated as such.
  They MUST NOT be documented or validated as official MCP protocol fields.
- **MPAG-FR-014**: Application pagination fields such as `page_token`, `next_page_token`, `has_more`, and `continuation_mode` MUST be documented as business payload pagination.
  This applies when they differ from MCP list pagination.
- **MPAG-FR-015**: Generic list-like protocol APIs, if added or refactored, MUST use MCP `cursor` and `nextCursor` semantics for MCP list methods.
- **MPAG-FR-016**: ResourceLink emission MUST use an explicit resource-link contract or provider.
  It MUST NOT depend on unconstrained recursive scanning of arbitrary payload maps.
- **MPAG-FR-017**: Mask planner input schemas MUST remove or rename encryption-specific fields so every public input field is either feature-neutral or mask-specific.
- **MPAG-FR-018**: Prompt names MUST describe user-facing prompt guidance and SHOULD NOT duplicate related tool names unless the name remains clear as a prompt.
- **MPAG-FR-019**: SDK gap handling for official MCP fields MUST remain isolated in `mcp/bootstrap` adapters and MUST have contract tests proving the wire output.
- **MPAG-FR-020**: Draft MCP changes such as `server/discover`, stateless requests, and `Mcp-Method` or `Mcp-Name` headers MUST be tracked as future compatibility risk.
  They MUST NOT be implemented under this stable-protocol package.
- **MPAG-FR-021**: Implementation must preserve Proxy-first scope, explicit operator control, and no-data-migration boundaries from `.specify/memory/constitution.md`.
- **MPAG-FR-022**: Every requirement must map to at least one implementation task and one verification gate before coding starts.

### Key Entities

- **Official MCP Protocol Surface**: The server capabilities, list methods, descriptor objects, tool results, resource results, prompt results, completion results, and JSON-RPC responses.
  These are defined by MCP `2025-11-25`.
- **ShardingSphere Catalog Resource**: The `shardingsphere://capabilities` resource and related ShardingSphere-owned catalog guidance. It is useful model guidance but not official MCP discovery.
- **Completion Provider**: A descriptor-owned or SPI-owned provider that resolves completion candidates for one reference and argument family without protocol-layer feature-name branches.
- **Business Payload Contract**: ShardingSphere-specific tool or resource result content such as workflow steps, recovery actions, SQL execution summaries, resource hints, and application pagination.
- **Protocol Error**: A JSON-RPC error for malformed requests, unsupported protocol operations, unknown tools, unknown resources, or other exceptional protocol conditions.
- **Tool Execution Error**: A tool result with `isError: true` for supported tool calls that fail because of business validation or executable-domain errors.
- **Resource Link Contract**: The explicit rule or provider that determines which business payload entries become MCP `ResourceLink` content blocks.

## Success Criteria

### Measurable Outcomes

- **MPAG-SC-001**: Official MCP list methods expose enough descriptor information for a generic client to discover the server without reading `shardingsphere://capabilities`.
- **MPAG-SC-002**: No protocol-layer completion class contains feature-name string checks for encrypt, mask, or other feature names.
- **MPAG-SC-003**: Generic descriptor validation contains no hardcoded public tool-name branches.
- **MPAG-SC-004**: Unsupported resource and unsupported tool calls have protocol-error tests.
- **MPAG-SC-005**: Supported tool business failures have `isError: true` tests with actionable structured feedback.
- **MPAG-SC-006**: Capabilities resource snapshots label custom sections as ShardingSphere catalog metadata.
- **MPAG-SC-007**: Business pagination docs and schema descriptions no longer imply MCP `nextCursor` semantics.
- **MPAG-SC-008**: Mask planner input schema contains no encryption-specific public field names.
- **MPAG-SC-009**: ResourceLink emission tests prove explicit contract/provider behavior instead of arbitrary recursive hint scanning.
- **MPAG-SC-010**: The requirements checklist has no failed items before implementation starts.

## Assumptions

- MCP protocol compatibility targets stable `2025-11-25`.
- The current branch remains `001-shardingsphere-mcp`.
- This package is documentation-only until a later implementation request.
- Existing descriptor field standardization and E2E hardening packages remain valid and may be referenced, but this package owns protocol API generalization gaps.
