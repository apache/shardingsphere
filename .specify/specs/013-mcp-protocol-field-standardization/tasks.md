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

# Tasks: MCP Protocol Field Standardization

**Input**: Design documents from `.specify/specs/013-mcp-protocol-field-standardization/`
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/mcp-descriptor-fields.md`, `source-evidence-resource-tool-fields.md`,
`phase-2-implementation-design.md`
**Tests**: Required for every Java, YAML, descriptor contract, transport adapter, and safety behavior change.

**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story this task supports.
- Every implementation task names the primary file or package path.

## Non-Negotiable Invariants

- `mcp/api` exposes stable MCP-facing contracts and official descriptor DTOs only.
- `mcp/api` must not depend on MCP Java SDK classes, YAML models, catalog builders, or transport adapters.
- Fixed resources and resource templates are separate public descriptor object types.
- Tool descriptors expose `inputSchema`, not public `fields` or `inputFields`.
- `mcp/support` owns YAML loading, descriptor catalog building, validation, and internal registries.
- `mcp/core` owns dispatch, runtime safety, request scope, and handler execution.
- `mcp/bootstrap` owns MCP Java SDK and wire-level adapter behavior only.
- Internal source YAML sections use the `internal` prefix and are never official MCP descriptor sections.
- Exposed ShardingSphere metadata uses only `org.apache.shardingsphere/`.
- Old descriptor field names are not accepted for compatibility.
- Business payload fields are not official descriptor fields.

---

## Phase 1: Setup and Boundary Inventory

- [x] T001 Record the implementation-time field drift inventory for resource, resource template, tool, prompt, completion, navigation, and catalog payload surfaces.
  Path: `.specify/specs/013-mcp-protocol-field-standardization/source-evidence-resource-tool-fields.md`
- [x] T002 [P] Confirm all source descriptor YAML files that must be migrated.
  Paths: `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [x] T003 [P] Confirm all tests currently asserting old descriptor behavior.
  Paths: `mcp/api/src/test/java/`, `mcp/support/src/test/java/`, `mcp/bootstrap/src/test/java/`
- [x] T004 [P] Update `mcp/bootstrap/pom.xml` dependency planning notes for MCP Java SDK `1.1.2` before code changes.

**Checkpoint**: All old public fields and affected files are known before implementation starts.

---

## Phase 2: Pre-Implementation Gates and Foundational Descriptor Model Changes

- [ ] T009 [US1] Upgrade MCP Java SDK dependencies from `1.1.0` to `1.1.2` before adapter feasibility tests.
  Path: `mcp/bootstrap/pom.xml`
- [ ] T016 [US1] Add bootstrap adapter feasibility tests for SDK gap fields through actual `resources/list`, `resources/templates/list`, `tools/list`, and `prompts/list` serialization seams, using representative official descriptor fixtures or minimal DTOs.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T017 [US3] Define strict descriptor YAML key validation before typed binding can drop unknown fields.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T018 [US4] Define the internal tool argument contract and documented runtime-enforced subset derived from official `inputSchema`.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/`
- [ ] T019 [US4] Define the `outputSchema` conformance strategy for tool `structuredContent`.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`
- [ ] T010 [US1] Split fixed resource and resource template descriptor models.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/`
- [ ] T011 [US1] Add official shared descriptor primitives for icons, Java/YAML `meta`, and resource annotations.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/`,
  `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`
- [ ] T012 [US1] Replace public tool `fields` with official `inputSchema` and add tool `execution.taskSupport`.
  Path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`
- [ ] T013 [US2] Add ShardingSphere metadata key constants and internal descriptor support for derived guidance.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T014 [US2] Define internal URI variable, prompt binding, completion registry, and navigation registry ownership.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T015 [US1] Move official prompt and prompt argument descriptor DTOs to `mcp/api` and keep template binding internal.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/prompt/descriptor/`,
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`

**Checkpoint**: SDK gap handling, strict YAML loading, and schema runtime boundaries are proven before public descriptor DTOs rely on them. Public descriptor DTOs express the official MCP contract; internal concepts have typed owners.

---

## Phase 3: User Story 1 - Official MCP Descriptor Shapes

**Goal**: MCP clients see only official descriptor top-level fields.
**Independent Test**: Catalog and bootstrap descriptor tests pass without old fields or dual-use resource identity.

### Tests for User Story 1

- [ ] T020 [P] [US1] Update API descriptor tests for fixed resource, resource template, tool `inputSchema`, icons, and execution.
  Path: `mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/`
- [ ] T021 [P] [US1] Update support loader tests to assert standardized YAML creates official descriptor objects.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T022 [P] [US1] Update bootstrap resource and tool specification tests for official descriptor mapping.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`

### Implementation for User Story 1

- [ ] T023 [US1] Update YAML descriptor models for `resources`, `resourceTemplates`, `tools`, `prompts`, and prompt arguments.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/`
- [ ] T024 [US1] Update YAML swappers to create split resource descriptors and direct tool `inputSchema`.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogYamlSwapper.java`
- [ ] T025 [US1] Update descriptor catalog payload builder to emit only official top-level fields for descriptor objects.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`
- [ ] T026 [US1] Update bootstrap resource and tool factories to consume official descriptor DTOs.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`

**Checkpoint**: Resource, resource template, tool, prompt, and prompt argument descriptors are independently standard.

---

## Phase 4: User Story 2 - Custom Field Ownership

**Goal**: Every non-official datum is internal configuration, namespaced `meta`, business payload, or deleted.
**Independent Test**: Field disposition tests prove custom fields have one target and cannot appear as descriptor fields.

### Tests for User Story 2

- [ ] T030 [P] [US2] Add tests for namespaced ShardingSphere metadata and internal-only runtime control fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T031 [P] [US2] Add tests proving prompt template bindings, completion targets, and navigation are not official descriptor fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T032 [P] [US2] Update transport payload tests for namespaced ResourceLink and result metadata.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`

### Implementation for User Story 2

- [ ] T033 [US2] Move resource `parameters`, `resourceKind`, `objectScope`, `feature`, relationships, and ordering guidance out of official fields.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`,
  `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T034 [US2] Move tool `workflowRole` and similar runtime-control data out of public `meta`.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/`
- [ ] T035 [US2] Internalize prompt `templateResource` and prompt argument completion hints under `internalPromptTemplateBindings`.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T036 [US2] Rename completion targets and resource navigation to `internalCompletionTargets` and `internalReferenceNavigation`.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T037 [US2] Namespace ResourceLink, elicitation, and result metadata keys when they remain exposed.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`

**Checkpoint**: No non-official field remains ambiguous or visible as an official descriptor field.

---

## Phase 5: User Story 3 - Fail-Fast Descriptor Validation

**Goal**: Descriptor loading rejects protocol drift before publication or transport mapping.
**Independent Test**: Negative descriptor fixtures fail with actionable validation messages.

### Tests for User Story 3

- [ ] T040 [P] [US3] Add negative YAML fixtures or test inputs for banned resource fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T041 [P] [US3] Add negative YAML fixtures or test inputs for banned tool fields and annotation `returnDirect`.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T042 [P] [US3] Add tests for un-namespaced `meta`, invalid annotation audience, invalid priority, and invalid `taskSupport`.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T043 [P] [US3] Add tests for `inputSchema` structural validation, the supported runtime-enforced schema subset, and `outputSchema` structural validation.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T048 [P] [US3] Add tests for strict raw YAML key rejection and local tool name policy.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation for User Story 3

- [ ] T044 [US3] Add top-level field whitelist validation per official descriptor object type.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
- [ ] T045 [US3] Make YAML loading detect raw unknown fields before typed binding can silently accept or drop them.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogYamlLoader.java`,
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/`
- [ ] T046 [US3] Validate namespaced metadata and reject un-namespaced exposed extension keys.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
- [ ] T047 [US3] Validate official annotation, icon, execution, and JSON Schema structure plus supported runtime-enforced schema subset during descriptor loading.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`

**Checkpoint**: Old fields and malformed official fields fail at descriptor load time.

---

## Phase 6: User Story 4 - Runtime Safety and Dispatch Boundaries

**Goal**: Runtime dispatch and safety remain explicit while descriptors become protocol-aligned.
**Independent Test**: Existing preview, approval, and destructive-tool tests still pass after descriptor cleanup.

### Tests for User Story 4

- [ ] T050 [P] [US4] Update resource registry tests for split fixed resource and resource template routing.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/`
- [ ] T051 [P] [US4] Update tool registry tests for argument validation through the compiled argument contract and `outputSchema` conformance for `structuredContent`.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/`
- [ ] T052 [P] [US4] Preserve workflow preview, approval, and validation tests after `workflowRole` leaves public `meta`.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/workflow/`

### Implementation for User Story 4

- [ ] T053 [US4] Replace resource registry dependence on `MCPResourceDescriptor.isTemplated()` with an internal route model.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/ResourceHandlerRegistry.java`
- [ ] T054 [US4] Replace tool registry dependence on `fields` with a compiled internal argument contract derived from `inputSchema`.
  Path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistry.java`
- [ ] T055 [US4] Remove transport-layer dependence on public `workflowRole` and descriptor `fields` during elicitation.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java`
- [ ] T056 [US4] Keep runtime approval and destructive-operation policy in typed internal configuration.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/security/`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`
- [ ] T057 [US4] Validate tool `structuredContent` against declared `outputSchema` before exposing the result.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`

**Checkpoint**: Runtime behavior is protected by internal contracts, not by public descriptor quirks.

---

## Phase 7: SDK Adapter and Descriptor YAML Migration

- [ ] T061 Add adapter support for official `icons` and tool `execution` fields missing from SDK `1.1.2`.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [ ] T062 Migrate core descriptor YAML to official fields.
  Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T063 Migrate support descriptor YAML to official fields.
  Path: `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T064 Migrate encrypt feature descriptor YAML to official fields.
  Path: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T065 Migrate mask feature descriptor YAML to official fields.
  Path: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [ ] T066 Remove dead descriptor helper classes and compatibility shims after YAML and code migration.
  Paths: `mcp/api/src/main/java/`, `mcp/support/src/main/java/`, `mcp/core/src/main/java/`, `mcp/bootstrap/src/main/java/`

---

## Phase 8: Verification and Documentation

- [ ] T070 Run `./mvnw -pl mcp/api -DskipITs -Dspotless.skip=true test`.
- [ ] T071 Run `./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true test`.
- [ ] T072 Run `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true test`.
- [ ] T073 Run `./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true test`.
- [ ] T074 Run `./mvnw -pl mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true test`.
- [ ] T075 Run scoped Checkstyle with `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -Pcheck checkstyle:check`.
- [ ] T076 Run `git diff --check`.
- [ ] T077 Run descriptor drift search for banned fields in source YAML and Java catalog output code.
  Command: `rg -n "parameters:|resourceKind:|objectScope:|feature:|relatedTools:|relatedResources:|useBefore:|fields:|inputFields|returnDirect|templateResource" mcp`
- [ ] T078 Update this task list and handoff notes with commands, exit codes, remaining risks, and any intentionally deferred work.

## Dependencies and Execution Order

- Phase 1 blocks all implementation because the affected surfaces must be known first.
- Phase 2 blocks user story implementation because SDK gap handling, strict YAML loading, schema runtime boundaries, descriptor DTOs, and internal ownership seams must exist first.
- T009 blocks T016 because adapter feasibility must be tested against the selected SDK version.
- T017, T018, and T019 block descriptor DTO implementation because the DTO shape must not rely on unproven loading or runtime schema behavior.
- T016 blocks T026, T061, and any claim that final transport output supports adapter-filled fields because SDK gap handling needs a proven wire strategy before adapter implementation.
- T017 blocks T045 because strict YAML loading must have a chosen raw-key validation strategy before implementation.
- T018 blocks T054 because runtime argument validation must have one compiled contract design.
- T019 blocks T057 because output schema validation needs one declared conformance strategy before implementation.
- User Story 1 and User Story 2 can proceed in parallel after Phase 2 if API and support write scopes are kept separate.
- User Story 3 depends on the YAML model and descriptor catalog shape from User Story 1.
- User Story 4 depends on the public descriptor changes from User Story 1 and the internal ownership decisions from User Story 2.
- Phase 7 depends on the descriptor, validator, and runtime boundary work.
- Phase 8 runs last and gates handoff.

## Parallel Execution Examples

- T020, T021, and T022 can run in parallel because they touch API, support, and bootstrap tests.
- T030, T031, and T032 can run in parallel if metadata, prompt/completion/navigation, and transport payload tests remain separated.
- T040 through T043 and T048 can run in parallel because they cover distinct validator failure categories.
- T062 through T065 can run in parallel after the loader and validator support the standardized YAML shape.

## Implementation Strategy

1. Freeze the public API contract first so later modules adapt to one descriptor model.
2. Move custom ShardingSphere data into internal owners or namespaced metadata.
3. Make descriptor validation the fail-fast boundary.
4. Adapt core dispatch and bootstrap SDK mapping after the contract is stable.
5. Migrate YAML and run scoped module gates before handoff.
