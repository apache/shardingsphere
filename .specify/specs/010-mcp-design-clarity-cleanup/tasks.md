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

# Tasks: MCP Design Clarity Cleanup

**Input**: Design documents from `.specify/specs/010-mcp-design-clarity-cleanup/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `scorecard.md`, `roadmap-100.md`, `checklists/requirements.md`
**Tests**: Required for Java or E2E changes; documentation-only updates require branch verification and `git diff --check`.

## Phase 1: Setup and Safety

- [x] T001 Confirm current branch remains `001-shardingsphere-mcp` with `git branch --show-current`.
- [x] T002 Confirm no implementation task uses `git switch`, `git checkout`, branch creation scripts, or generated `target/` files.
- [x] T003 Record touched modules before implementation so Maven and Checkstyle checks can stay scoped.
- [x] T004 Review unrelated dirty files and avoid reverting user changes.
- [x] T005 Review `scorecard.md` before implementation and map each slice to the design, readability, or convenience gate it advances.
- [x] T006 Confirm no implementation task relies on JAVADOC, ordinary comments, README text, or handoff prose to explain unclear code.

## Phase 2: Production 100-Point Gates

- [x] T010 [US1] Remove bootstrap feature coupling before broader core cleanup.
  Target path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`.
- [x] T011 [US1] Analyze `StatementClassifier` and decide parser-backed classification or explicit helper split.
  Target path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/StatementClassifier.java`.
- [x] T012 [US1] Refactor `StatementClassifier` so tokenizer, CTE handling, and target-object extraction are not hidden inside one mixed-responsibility class.
- [x] T013 [US1] Add or update focused tests for statement classification safety and routing behavior.
- [x] T014 [US1] Split `MCPDescriptorCatalogLoader` into loader, swapper, validator, and legacy rejection responsibilities.
  Target path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java`.
- [x] T015 [US1] Move model-facing catalog guidance out of `MCPDescriptorCatalog` or isolate it behind a named payload builder.
  Target path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`.
- [x] T016 [US1] Replace message-prefix recovery logic in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java` with structured recovery hints or providers.
- [x] T017 [US1] Split `SearchMetadataToolService` by collection, ranking, pagination, and response assembly where it improves readability.
  Target path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`.
- [x] T018 [US1] Move completion candidate production out of `MCPCompletionSpecificationFactory`.
  Target path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactory.java`.
- [x] T019 [US1] Move legacy meta parsing and cleanup out of `MCPResourceDescriptor` so API value objects carry canonical state only.
  Target path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/MCPResourceDescriptor.java`.

Completed production score path:

1. T018 closes the completion/transport boundary gap.
2. T020 through T023 make recurring public payload contracts discoverable.
3. T017 reduces metadata search reading cost.
4. T011 through T013 reduce SQL classification risk after recovery behavior is stable.

## Phase 3: Public Contract and Transport Cleanup

- [x] T020 [US2] Identify repeated public payload keys across `mcp/api`, `mcp/core`, `mcp/support`, `mcp/bootstrap`, and descriptor YAML files.
- [x] T021 [US2] Add small typed payload models or centralized factories/constants for `next_actions`, recovery hints, resource hints, and workflow apply payloads.
- [x] T022 [US2] Delete legacy aliases from model-facing output, including recovery top-level `target_tool` style fields, and update descriptor/runtime contract tests to the canonical shape.
- [x] T023 [US2] Make planning-tool elicitation and argument merge behavior descriptor-driven or feature-module owned.
- [x] T024 [US2] Add simple testable construction paths for `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/service/EncryptWorkflowPlanningService.java`.
- [x] T025 [US2] Add simple testable construction paths for `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/tool/service/MaskWorkflowPlanningService.java`.
- [x] T026 [US2] Replace normal collaborator field reflection in touched encrypt/mask tests with constructor-based setup.
- [x] T027 [US2] Make intentional public contract changes visible through canonical field names, contract tests, and focused assertions.
- [x] T028 [US2] Align direct static and constructor mocking in touched tests with repository mocking guidance.
- [x] T029 [US2] Replace common long positional `MCPToolValueDefinition` construction with small named factories or equivalent helper methods.
  Target path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/MCPToolValueDefinition.java`.
- [x] T030 [US2] Rename or split URI template expansion APIs so missing-variable behavior is explicit instead of hidden behind an empty-string sentinel.
  Target path: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/MCPUriTemplateUtils.java`.

## Phase 4: E2E 100-Point Gates

- [x] T050 [US3] Add named assertion helpers for common MCP payload shapes under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp`.
- [x] T051 [US3] Replace repeated nested `Map<String, Object>` casts in touched E2E tests with assertion helpers.
- [x] T052 [US3] Split `ProductionH2RuntimeSmokeE2ETest` by product surface.
  Target path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionH2RuntimeSmokeE2ETest.java`.
- [x] T053 [US3] Split `LLMMCPConversationRunner` by model loop, tool bridge, final-answer verifier, safety validator, artifacts, and scoring.
  Target path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPConversationRunner.java`.
- [x] T054 [US3] Reuse MCP tool descriptors or list responses instead of manually reconstructing tool schemas in the E2E runner.
- [x] T055 [US3] Separate generic runtime setup from LLM scenario fixtures in H2 and MySQL runtime support.
- [x] T056 [US3] Re-audit manual wait/retry loops and keep distinct loops local where a shared helper would add abstraction without reducing reading cost.
- [x] T057 [US3] Keep live LLM usability tests opt-in and outside default CI.

Completed E2E score path:

1. T050 and T051 remove the largest readability cost first.
2. T052 splits smoke failures by product surface.
3. T053 and T054 split the opt-in LLM path without changing default CI.
4. T055 and T056 finish fixture and readiness clarity.

## Phase 5: Verification and Handoff

- [x] T070 Run `git diff --check`.
- [x] T071 Run scoped Maven tests for touched MCP modules.
- [x] T072 Run scoped Checkstyle with `-Pcheck` for touched Java modules.
- [x] T073 Run scoped E2E tests when `test/e2e/mcp` behavior changes.
- [x] T074 Re-run `git branch --show-current` and confirm it is still `001-shardingsphere-mcp`.
- [x] T075 Update `scorecard.md` with the current full-scope baseline, target gates, and score evidence.
- [x] T076 Update `specs/006-mcp-design-clarity-cleanup/requirements.md` if implementation decisions change the requirement inventory.
- [x] T077 Update `scorecard.md` with final 100/100 gate status after implementation completes.

## Dependencies & Execution Order

- Phase 1 blocks all implementation.
- Start with T018, then T050 and T051.
- T010 and T023 should be addressed after descriptor and completion boundaries are clearer.
- E2E cleanup is in scope for this feature and should start once canonical payload helpers are available.
- T019, T029, and T030 must stay completed before the `mcp/api` line item can
  be treated as full credit inside the production score.
- Verification tasks must run after each implementation slice, not only at the end.
