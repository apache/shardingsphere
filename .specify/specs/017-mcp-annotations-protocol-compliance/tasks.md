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

# Tasks: MCP Annotations Protocol Compliance

**Input**: Design documents from `.specify/specs/017-mcp-annotations-protocol-compliance/`
**Prerequisites**: `spec.md`, `plan.md`, `annotation-inventory.md`, `checklists/requirements.md`
**Tests**: Required for every Java, YAML descriptor, descriptor validation, payload serialization, or SDK mapping change.
**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names the primary file or package path.
- Every coding task must start with a branch/path coverage checklist and finish with scoped tests plus style checks.

## Non-Negotiable Invariants

- MCP `Annotations` and `ToolAnnotations` remain separate models.
- MCP optional-field semantics are preserved.
- Absent resource priority is not serialized as `0.0`.
- Explicit `priority: 0.0` is serialized as `0.0`.
- Production public tools explicitly declare all four MCP boolean hints in descriptor YAML.
- `MCPToolAnnotations.EMPTY` is distinguishable from an explicitly declared annotation object with MCP default boolean values.
- Tool hints are not runtime safety boundaries.
- ShardingSphere runtime and extension metadata never appears under MCP `annotations`.

---

## Phase 1: Setup and Source Evidence

- [x] T001 Confirm current branch remains `001-shardingsphere-mcp` without running branch-changing commands.
  Path: repository root
- [x] T002 [P] Reconfirm MCP `2025-11-25` source references for `Annotations`, `Role`, `Resource`, `ResourceTemplate`, `Tool`, and `ToolAnnotations`.
  Path: `.specify/specs/017-mcp-annotations-protocol-compliance/spec.md`
- [x] T003 [P] Record an implementation checklist for wrapper-type removal, priority presence, descriptor raw validation, and output omission behavior.
  Path: `.specify/specs/017-mcp-annotations-protocol-compliance/checklists/requirements.md`
- [x] T004 [P] Verify existing descriptor annotations and classify each production resource/tool as empty-allowed or explicit-required.
  Path: `.specify/specs/017-mcp-annotations-protocol-compliance/annotation-inventory.md`

**Checkpoint**: The implementation can start from a source-backed annotation contract.

---

## Phase 2: API Model Semantics

**Goal**: Java API models express MCP annotation semantics without nullable wrapper booleans for tool hints.
**Independent Test**: API unit tests cover `EMPTY`, explicit priority presence, and `isEmpty()` behavior.

### Tests

- [x] T010 [P] [US1] Add API tests proving absent resource priority differs from explicit `0.0`.
  Path: `mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/resource/descriptor/`
- [x] T011 [P] [US2] Add API tests proving `MCPToolAnnotations.EMPTY` uses MCP default effective values and remains empty for output omission.
  Path: `mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`

### Implementation

- [x] T012 [US1] Change `MCPResourceAnnotations` to preserve priority presence with a primitive `double` plus explicit presence, or an equivalent invariant that never serializes absent priority.
  Path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/MCPResourceAnnotations.java`
- [x] T013 [US2] Change `MCPToolAnnotations` boolean hint fields from `Boolean` to primitive `boolean` and set MCP default effective values on `EMPTY`.
  Path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/MCPToolAnnotations.java`
- [x] T014 [US2] Update constructors, getters, annotation-object presence, and `isEmpty()` behavior so test fixtures can still use `EMPTY`
  while loaded descriptors can emit explicitly declared default-valued annotations.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`, `mcp/api/src/test/java/`

**Checkpoint**: API models express resource optionality and tool default hints correctly.

---

## Phase 3: YAML DTO and Raw Descriptor Validation

**Goal**: YAML loading preserves explicit descriptor policy before primitive defaults are applied.
**Independent Test**: YAML validator tests fail on empty maps, unknown keys, missing production tool hints, invalid audience, invalid priority, invalid lastModified, and contradictory hints.

### Tests

- [x] T020 [P] [US3] Add raw YAML validator tests for empty resource annotations and empty tool annotations.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidatorTest.java`
- [x] T021 [P] [US3] Add raw YAML validator tests for invalid resource annotation keys and invalid tool annotation keys.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidatorTest.java`
- [x] T022 [P] [US3] Add catalog validation tests for audience role values, priority range, finite priority, ISO 8601 lastModified, and contradictory tool hints.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [x] T023 [P] [US3] Add loaded-descriptor tests proving every catalog-loaded public tool explicitly declares the four boolean hints.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation

- [x] T024 [US3] Change `YamlMCPToolAnnotations` boolean hint fields to primitive booleans with MCP default values.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/YamlMCPToolAnnotations.java`
- [x] T025 [US1] Change `YamlMCPResourceAnnotations` priority handling to preserve explicit presence.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/YamlMCPResourceAnnotations.java`
- [x] T026 [US3] Extend raw YAML validation to reject empty annotation maps and to enforce explicit loaded-descriptor public-tool boolean hint keys.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidator.java`
- [x] T027 [US3] Extend catalog semantic validation for MCP role values, priority range, ISO 8601 lastModified, and contradictory tool hints.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
- [x] T028 [US1] Update YAML swapping so absent resource priority remains absent and explicit `0.0` remains explicit.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogYamlSwapper.java`

**Checkpoint**: Descriptor loading separates MCP schema validity from ShardingSphere production-tool explicit hint policy.

---

## Phase 4: Output and SDK Mapping

**Goal**: Official MCP responses emit only official annotation fields and preserve optionality.
**Independent Test**: Payload and SDK factory tests inspect resource, resource-template, and tool annotations.

### Tests

- [x] T030 [P] [US4] Add payload builder tests proving empty resource annotations are omitted and explicit `priority: 0.0` is emitted.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [x] T031 [P] [US4] Add resource specification factory tests proving absent priority is passed as absent/null at the SDK boundary.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactoryTest.java`
- [x] T032 [P] [US4] Add tool specification factory tests proving primitive tool hints map to `McpSchema.ToolAnnotations`
  when annotations are explicitly declared and are omitted for `MCPToolAnnotations.EMPTY`.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`

### Implementation

- [x] T033 [US4] Update descriptor payload serialization to use resource priority presence and omit empty annotations.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`
- [x] T034 [US4] Update resource SDK mapping to pass absent priority as `null` and never emit `NaN` or default `0.0`.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java`
- [x] T035 [US4] Update tool SDK mapping to use primitive boolean hint values.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`
- [x] T036 [US4] Verify no ShardingSphere runtime, extension, or side-effect metadata is emitted under MCP `annotations`.
  Paths: `mcp/support/src/main/java/`, `mcp/bootstrap/src/main/java/`

**Checkpoint**: MCP output is schema-exact and does not leak internal descriptor metadata into annotations.

---

## Phase 5: Descriptor Cleanup and Documentation

**Goal**: Production descriptors and docs match the new annotation rules.
**Independent Test**: Descriptor catalog loads and validates all production descriptors with no annotation-policy exceptions.

- [x] T040 [P] [US3] Review current production resources and keep resource annotations only where audience, priority, or lastModified is meaningful.
  Paths: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`, `mcp/features/*/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`
- [x] T041 [P] [US3] Review current production tools and ensure all public tools explicitly declare the four boolean hints.
  Paths: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`,
  `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`,
  `mcp/features/*/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`
- [x] T042 [US4] Update README annotation guidance to distinguish MCP protocol optionality from ShardingSphere production descriptor policy.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`

**Checkpoint**: Descriptor files and docs are consistent with protocol and policy rules.

---

## Phase 6: Verification and Handoff

- [x] T050 Run scoped API/support/bootstrap/core tests for annotation models, descriptor validation, and SDK mapping.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap,mcp/core,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest=<changed-tests> test`
- [x] T051 Run scoped Checkstyle for touched MCP modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap,mcp/core,test/e2e/mcp -DskipTests -DskipITs -Pcheck checkstyle:check`
- [x] T052 Run touched-file Spotless checks or apply formatting when needed.
  Command: `./mvnw -pl <module> -DskipTests -DskipITs -Pcheck -DspotlessFiles=<files> spotless:check`
- [x] T053 Run static searches for removed wrapper-type and nullable-priority patterns.
  Command: `rg "Boolean readOnlyHint|Boolean destructiveHint|Boolean idempotentHint|Boolean openWorldHint|Double priority|null != .*getPriority|getPriority\\(\\) == null" mcp -g '!**/target/**'`
- [x] T054 Run `git diff --check` for touched files.
  Path: repository root
- [x] T055 Update this task list with completed verification evidence before handoff.
  Path: `.specify/specs/017-mcp-annotations-protocol-compliance/tasks.md`

## Verification Evidence

- Scoped annotation test suite with `-am`: passed.
- Scoped annotation test suite without `-am`: passed.
- `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -DskipTests -DskipITs -Pcheck checkstyle:check`: passed.
- `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -DskipTests -DskipITs -Pcheck spotless:check`: passed.
- Static search for removed wrapper/nullable getter patterns and exposed `isDeclared`: no matches.
- `git diff --check -- <package-017-touched-files>`: passed.
