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

# Tasks: MCP Descriptor Schema Compliance

**Input**: Design documents from `.specify/specs/018-mcp-descriptor-schema-compliance/`
**Prerequisites**: `spec.md`, `plan.md`, `checklists/requirements.md`
**Tests**: Required for every Java, YAML descriptor, descriptor validation, payload serialization, or SDK mapping change.
**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names the primary file or package path.
- Every coding task must start with a branch/path coverage checklist and finish with scoped tests plus style checks.

## Non-Negotiable Invariants

- Package 017 remains annotation-only.
- Existing descriptor output is unchanged when new optional fields are absent.
- Official MCP fields are emitted through official MCP schema surfaces.
- ShardingSphere runtime and extension metadata never appears under MCP `annotations`.
- Descriptor YAML remains developer-authored and strictly validated at startup.

---

## Phase 1: Setup and Source Evidence

- [x] T001 Confirm this package is manually maintained on branch `001-shardingsphere-mcp` without branch-changing commands.
  Path: repository root
- [x] T002 [P] Record MCP `2025-11-25` source references for `Resource`, `ResourceTemplate`, `Tool`, `ToolExecution`, `Icon`, and `_meta`.
  Path: `.specify/specs/018-mcp-descriptor-schema-compliance/spec.md`
- [x] T003 [P] Define API/support/bootstrap ownership boundaries for official descriptor fields.
  Path: `.specify/specs/018-mcp-descriptor-schema-compliance/plan.md`

**Checkpoint**: Future implementation can start from a source-backed descriptor contract.

---

## Phase 2: Descriptor Field Inventory

- [ ] T010 [P] Inventory current raw YAML validator gaps for official resource fields.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidator.java`
- [ ] T011 [P] Inventory current raw YAML validator gaps for official tool fields.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidator.java`
- [ ] T012 [P] Verify MCP Java SDK `1.1.2` support for selected `icons`, `size`, `execution`, and `_meta` fields before adding API fields.
  Path: `mcp/bootstrap/pom.xml`

**Checkpoint**: Implementation scope is confirmed against the local SDK surface.

---

## Phase 3: API and YAML Model Semantics

**Goal**: Java API and YAML DTO models can represent selected official non-annotation MCP descriptor fields without changing behavior when fields are absent.
**Independent Test**: API and YAML swapper tests prove absent optional fields remain absent while explicit fields are preserved.

### Tests

- [ ] T020 [P] [US1] Add resource descriptor tests for omitted and explicit resource `size`, resource `icons`, and resource-template `icons`.
  Path: `mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/resource/descriptor/`
- [ ] T021 [P] [US2] Add tool descriptor tests for omitted and explicit `icons` and `execution.taskSupport`.
  Path: `mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`

### Implementation

- [ ] T022 [US1] Add selected official resource descriptor fields to API and YAML DTOs.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/`, `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/`
- [ ] T023 [US2] Add selected official tool descriptor fields to API and YAML DTOs.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`, `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/`
- [ ] T024 [US1,US2] Update YAML swapping to preserve optional-field presence.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogYamlSwapper.java`

**Checkpoint**: Descriptor models represent official optional fields without defaulting absent values.

---

## Phase 4: Validation

**Goal**: Descriptor validation accepts valid official fields and rejects malformed official fields before server startup.
**Independent Test**: Raw YAML and semantic catalog validation tests cover valid, omitted, invalid, and unknown values.

### Tests

- [ ] T030 [P] [US1] Add raw YAML validator tests for resource `size` and `icons`.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidatorTest.java`
- [ ] T031 [P] [US2] Add raw YAML validator tests for tool `icons` and `execution.taskSupport`.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidatorTest.java`
- [ ] T032 [P] [US1,US2] Add semantic validator tests for non-negative resource size and legal `taskSupport` values.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`

### Implementation

- [ ] T033 [US1,US2] Extend raw YAML key and type validation for selected official fields.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorYamlKeyValidator.java`
- [ ] T034 [US1,US2] Extend semantic catalog validation for selected official field values.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`

**Checkpoint**: Startup validation distinguishes MCP schema validity from ShardingSphere descriptor policy.

---

## Phase 5: Payload and SDK Mapping

**Goal**: Official MCP outputs emit official descriptor fields through official schema surfaces only when present.
**Independent Test**: Payload and SDK factory tests inspect selected fields and omission behavior.

### Tests

- [ ] T040 [P] [US1,US3] Add descriptor catalog payload tests for selected resource official fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T041 [P] [US2,US3] Add descriptor catalog payload tests for selected tool official fields.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [ ] T042 [P] [US1] Add resource specification factory tests for selected resource official fields.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/`
- [ ] T043 [P] [US2] Add tool specification factory tests for selected tool official fields.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`

### Implementation

- [ ] T044 [US1,US2] Update descriptor catalog payload serialization for selected official fields.
  Path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`
- [ ] T045 [US1] Update resource SDK mapping for selected resource official fields.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java`
- [ ] T046 [US2] Update tool SDK mapping for selected tool official fields.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`

**Checkpoint**: Official descriptor fields reach MCP clients through official protocol surfaces.

---

## Phase 6: Documentation and Verification

- [ ] T050 [US3] Update README descriptor guidance to distinguish official MCP fields from ShardingSphere descriptor-only metadata.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`
- [ ] T051 Run scoped API/support/bootstrap tests for descriptor models, validation, payloads, and SDK mapping.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest=<changed-tests> test`
- [ ] T052 Run scoped Checkstyle for touched MCP modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap -DskipTests -DskipITs -Pcheck checkstyle:check`
- [ ] T053 Run scoped Spotless check for touched MCP modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/bootstrap -DskipTests -DskipITs -Pcheck spotless:check`
- [ ] T054 Run static searches proving selected official fields are not emitted through annotations or ShardingSphere-only metadata.
  Path: repository root
- [ ] T055 Run `git diff --check` for touched package files.
  Path: repository root
