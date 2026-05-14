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

# Tasks: MCP Protocol API Generalization

**Input**: Design documents from `.specify/specs/015-mcp-protocol-api-generalization/`
**Prerequisites**: `spec.md`, `plan.md`, `checklists/requirements.md`
**Tests**: Required for every Java, YAML, descriptor contract, protocol adapter, error semantics, and planner schema change.

**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story this task supports.
- Every implementation task names the primary file or package path.

## Non-Negotiable Invariants

- Official MCP discovery through initialization and list methods remains sufficient.
- `shardingsphere://capabilities` is a ShardingSphere catalog resource, not the MCP protocol discovery source of truth.
- Protocol-layer dispatch must not depend on concrete feature names, concrete public tool names, or string containment checks.
- ShardingSphere business payload fields are allowed, but they are not official MCP protocol fields.
- Unsupported resources and unsupported tools use protocol error semantics.
- Supported tool execution failures use `isError: true` with actionable structured feedback.
- Draft MCP changes are tracked as risk only.
- Package 016 owns E2E disposition and release-evidence boundaries; this package owns the follow-up catalog clarity and canonical error-channel implementation.
- Proxy-first scope, explicit operator control, and no-data-migration boundaries remain intact.

---

## Phase 1: Setup and Evidence Lock

- [X] T001 Confirm current branch remains `001-shardingsphere-mcp` without running branch-changing commands.
  Path: repository root
- [X] T002 [P] Record the official MCP `2025-11-25` source references used for protocol error, discovery, tool, resource, prompt, and pagination semantics.
  Path: `.specify/specs/015-mcp-protocol-api-generalization/plan.md`
- [X] T003 [P] Add an implementation evidence ledger for current non-general APIs and their target disposition.
  Path: `.specify/specs/015-mcp-protocol-api-generalization/protocol-api-evidence.md`
- [X] T004 [P] Confirm overlapping requirements in protocol field standardization and E2E hardening packages to avoid duplicate implementation work.
  Record the owner map: 013 owns descriptor shape and metadata naming, 014 owns accepted E2E/outputSchema/pagination hardening,
  016 owns E2E disposition and release-evidence boundaries, and 015 owns protocol/domain separation and API generalization gaps.
  The 016 handoff keeps prompt/resource catalog clarity and canonical error channels as implementation work in this package.
  Paths: `.specify/specs/013-mcp-protocol-field-standardization/`, `.specify/specs/014-mcp-standard-and-e2e-hardening/`,
  `.specify/specs/016-mcp-contract-e2e-gap-triage/`
- [X] T005 [P] Verify MCP Java SDK `1.1.2` wire behavior for completely unknown tools and resources before changing ShardingSphere error mapping.
  Transport coverage: both Streamable HTTP and STDIO raw wire responses.
  Assertions: JSON-RPC error is present, normal `result` is absent, resource contents are absent, and tool failures are not wrapped as `CallToolResult.isError`.
  Paths: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`,
  `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/`

**Checkpoint**: The implementation can start with a stable protocol baseline and a non-duplicative scope.

---

## Phase 2: Foundational Protocol and Domain Boundary

- [ ] T010 [US1] Define the ShardingSphere catalog metadata naming and labeling policy for `shardingsphere://capabilities`.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T011 [US3] Define a typed protocol error path for unsupported tools and resources.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T012 [US2] Define a completion provider contract that maps reference type, reference, and argument name to candidate providers.
  The default location is `mcp/support`; use `mcp/api` only if a later design proves the signature can stay on pure API DTOs without descriptor or support-context dependency.
  Feature modules must not depend on `shardingsphere-mcp-core` for completion providers.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/`,
  `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/`
- [ ] T013 [US4] Define the domain payload field policy for `response_mode`, `next_actions`, `recovery`, resource hints, and application pagination.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/protocol/`, `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T014 [US4] Define an explicit ResourceLink provider or contract to replace recursive arbitrary map scanning.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T015 [US2] Define a feature-owned validation extension point for tool-specific descriptor or output-shape checks.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`

**Checkpoint**: Protocol/domain ownership, error semantics, completion dispatch, and ResourceLink generation have typed homes before behavior changes.

---

## Phase 3: User Story 1 - Official Discovery First

**Goal**: Generic MCP clients can discover the server through official MCP list methods.
**Independent Test**: Official list methods expose complete descriptor information without requiring `shardingsphere://capabilities`.

### Tests for User Story 1

- [ ] T020 [P] [US1] Add bootstrap or integration tests proving `tools/list`, `resources/list`, `resources/templates/list`, and `prompts/list` are sufficient for official discovery.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T021 [P] [US1] Add catalog snapshot tests proving custom catalog sections are labeled as ShardingSphere metadata, not protocol fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation for User Story 1

- [ ] T022 [US1] Rename or relabel `protocol_fields` and related catalog sections so they are ShardingSphere catalog metadata.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPModelFirstContractPayloadBuilder.java`
- [ ] T023 [US1] Update capability catalog payload construction to make official MCP list methods authoritative and catalog guidance optional.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`
- [ ] T024 [US1] Update server instructions so they recommend catalog guidance without implying protocol discovery depends on it.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportConstants.java`

**Checkpoint**: The custom catalog remains useful but no longer looks like the MCP discovery protocol.

---

## Phase 4: User Story 2 - Provider-Driven Completion and Validation

**Goal**: Completion and tool-specific validation do not require protocol-layer feature or tool name branches.
**Independent Test**: Representative metadata, workflow, encrypt, and mask completions resolve through providers.

### Tests for User Story 2

- [ ] T030 [P] [US2] Add tests for metadata completion providers covering database, schema, table, column, index, and sequence.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/completion/`
- [ ] T031 [P] [US2] Add tests for workflow plan ID completion through a provider.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/completion/`
- [ ] T032 [P] [US2] Add tests for encrypt and mask algorithm completion without string containment routing.
  Verify feature modules do not add a dependency on `shardingsphere-mcp-core`.
  Paths: `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`,
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/completion/`
- [ ] T033 [P] [US2] Add descriptor validator tests proving generic validation no longer hardcodes public tool names.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation for User Story 2

- [ ] T034 [US2] Extract metadata completion logic into provider classes.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/completion/`
- [ ] T035 [US2] Extract workflow plan ID completion logic into a provider.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/completion/`
- [ ] T036 [US2] Extract algorithm completion logic into descriptor or feature-aware providers.
  Feature providers must not require `mcp/features/*` to depend on `shardingsphere-mcp-core`.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/`,
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/`,
  `mcp/features/encrypt/src/main/java/`, `mcp/features/mask/src/main/java/`
- [ ] T037 [US2] Remove hardcoded tool-specific required field branches from the generic descriptor validator.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
- [ ] T038 [US2] Move tool-specific output-shape checks into descriptor schema tests or feature-owned validators.
  Paths: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`, `mcp/features/*/src/test/java/`

**Checkpoint**: Adding a new feature completion target or tool output contract does not require editing generic protocol-layer branches.

---

## Phase 5: User Story 3 - Protocol Errors and Tool Execution Errors

**Goal**: Unsupported protocol targets and supported business failures use different MCP channels.
**Independent Test**: Unsupported resource, unsupported tool, malformed request, and supported business validation cases return the expected channel.

### Tests for User Story 3

- [ ] T040 [P] [US3] Add wire-level resource read tests that separate SDK no-match protocol errors from ShardingSphere matched-template handler misses.
  Transport coverage: both Streamable HTTP and STDIO raw wire responses for SDK no-match.
  Assertions: unsupported no-match resource returns JSON-RPC error, no normal `result`, and no successful resource contents.
  Matched-template handler miss coverage: unit test first; add E2E only if unit tests cannot fully cover the internal boundary or affected transport mapping.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/`
- [ ] T041 [P] [US3] Add wire-level tool call tests that separate SDK unknown-tool protocol errors from ShardingSphere controller-direct unsupported dispatch.
  Transport coverage: both Streamable HTTP and STDIO raw wire responses for SDK unknown-tool.
  Assertions: unsupported unknown tool returns JSON-RPC error, no normal `result`, and no `CallToolResult.isError` wrapper.
  Controller-direct unsupported dispatch coverage: unit test first; add E2E only if unit tests cannot fully cover the internal boundary or affected transport mapping.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`
- [ ] T042 [P] [US3] Add supported tool business failure tests proving `isError: true` and actionable structured content.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`

### Implementation for User Story 3

- [ ] T043 [US3] Change resource dispatch so unsupported resource URIs surface as protocol errors.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/MCPResourceController.java`
- [ ] T044 [US3] Change tool dispatch so unsupported tool names surface as protocol errors.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/MCPToolController.java`
- [ ] T045 [US3] Update bootstrap transport mapping to emit JSON-RPC protocol errors where required by MCP.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T046 [US3] Keep supported business execution failures mapped to `CallToolResult.isError(true)`.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`

**Checkpoint**: Clients can distinguish protocol failure from model-correctable tool execution failure.

---

## Phase 6: User Story 4 - Domain Payload Boundaries

**Goal**: ShardingSphere payload fields are explicit domain contracts, not MCP protocol fields.
**Independent Test**: Descriptor descriptions, README snippets, and schema tests classify all custom fields correctly.

### Tests for User Story 4

- [ ] T050 [P] [US4] Add tests or snapshots proving application pagination fields are documented as domain payload fields.
  Paths: `mcp/support/src/test/java/`, `mcp/core/src/test/java/`, `mcp/README.md`, `mcp/README_ZH.md`
- [ ] T051 [P] [US4] Add ResourceLink provider tests with explicit ordering and limits.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`

### Implementation for User Story 4

- [ ] T052 [US4] Update payload schema descriptions that mention `next_page_token`, `has_more`, or `continuation_mode` to call them application pagination.
  Descriptions must state that these fields are not MCP list `cursor` or `nextCursor`.
  Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`
- [ ] T053 [US4] Update shared payload response helpers to keep business fields domain-scoped in names and documentation.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/protocol/`
- [ ] T054 [US4] Replace recursive ResourceLink scanning with the explicit ResourceLink provider or contract.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`
- [ ] T055 [US4] Update README documentation for custom catalog, business payload fields, and application pagination.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`

**Checkpoint**: Custom business payloads remain useful without being confused with MCP protocol structures.

---

## Phase 7: User Story 5 - Feature Planner Schema Cleanup

**Goal**: Planner schemas and prompts expose feature-appropriate APIs.
**Independent Test**: Encrypt and mask planner descriptors list only feature-neutral or feature-specific fields with clear prompt names.

### Tests for User Story 5

- [ ] T060 [P] [US5] Add descriptor tests proving mask planner input fields are mask-specific or feature-neutral.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T061 [P] [US5] Add prompt descriptor tests proving prompt names are user-facing and related tools are linked through metadata.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation for User Story 5

- [ ] T062 [US5] Remove or rename encryption-specific intent fields from mask planner descriptors and handlers.
  Paths: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`, `mcp/features/mask/src/main/java/`
- [ ] T063 [US5] Rename feature prompt descriptors to user-facing guidance names while preserving related-tool metadata.
  Paths: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`, `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`
- [ ] T064 [US5] Update prompt rendering and completion references affected by prompt renames.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/completion/`

**Checkpoint**: Feature planner APIs no longer leak unrelated feature semantics.

---

## Phase 8: Verification and Documentation

- [ ] T070 Run module-scoped tests for touched MCP modules.
  Command: `./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test`
- [ ] T071 Run scoped Checkstyle or `-Pcheck` validation for touched MCP modules.
  Command: `./mvnw -pl mcp -am -Pcheck -DskipITs checkstyle:check`
- [ ] T072 Run descriptor-focused tests for changed descriptor and YAML behavior.
  Command: `./mvnw -pl mcp/support,mcp/core,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest='*Descriptor*,*Completion*,*SpecificationFactory*' -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] T073 Update Speckit evidence and checklist after implementation.
  Paths: `.specify/specs/015-mcp-protocol-api-generalization/`, `mcp/README.md`, `mcp/README_ZH.md`

## Dependencies and Execution Order

- Phase 1 must complete before implementation.
- Phase 2 blocks all user stories.
- T005 blocks T011, T040, T041, T043, T044, and T045.
- User Stories 1, 2, and 3 are P1 and should be implemented before P2 planner and documentation cleanup.
- User Stories 4 and 5 can proceed in parallel after Phase 2 if write scopes are disjoint.
- Phase 8 closes the package only after tests and documentation evidence are recorded.
