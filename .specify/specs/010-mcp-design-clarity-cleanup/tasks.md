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
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `checklists/requirements.md`
**Tests**: Required for Java or E2E changes; documentation-only updates require branch verification and `git diff --check`.

## Phase 1: Setup and Safety

- [ ] T001 Confirm current branch remains `001-shardingsphere-mcp` with `git branch --show-current`.
- [ ] T002 Confirm no implementation task uses `git switch`, `git checkout`, branch creation scripts, or generated `target/` files.
- [ ] T003 Record touched modules before implementation so Maven and Checkstyle checks can stay scoped.
- [ ] T004 Review unrelated dirty files and avoid reverting user changes.

## Phase 2: Core Responsibility Boundaries

- [ ] T010 [US1] Remove bootstrap feature coupling before broader core cleanup.
  Target path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`.
- [ ] T011 [US1] Analyze `StatementClassifier` and decide parser-backed classification or explicit helper split.
  Target path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/StatementClassifier.java`.
- [ ] T012 [US1] Refactor `StatementClassifier` so tokenizer, CTE handling, and target-object extraction are not hidden inside one mixed-responsibility class.
- [ ] T013 [US1] Add or update focused tests for statement classification safety and routing behavior.
- [ ] T014 [US1] Split `MCPDescriptorCatalogLoader` into loader, swapper, validator, and legacy cleanup responsibilities.
  Target path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java`.
- [ ] T015 [US1] Move model-facing catalog guidance out of `MCPDescriptorCatalog` or isolate it behind a named payload builder.
  Target path: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`.
- [ ] T016 [US1] Replace message-prefix recovery logic in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java` with structured recovery hints or providers.
- [ ] T017 [US1] Split `SearchMetadataToolService` by collection, ranking, pagination, and response assembly where it improves readability.
  Target path: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`.
- [ ] T018 [US1] Move completion candidate production out of `MCPCompletionSpecificationFactory`.
  Target path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactory.java`.

## Phase 3: Public Contract and Transport Cleanup

- [ ] T020 [US2] Identify repeated public payload keys across `mcp/api`, `mcp/core`, `mcp/support`, `mcp/bootstrap`, and descriptor YAML files.
- [ ] T021 [US2] Add small typed payload models or centralized factories/constants for `next_actions`, recovery hints, resource hints, and workflow apply payloads.
- [ ] T022 [US2] Delete legacy aliases from model-facing output and update descriptor/runtime contract tests to the canonical shape.
- [ ] T023 [US2] Make planning-tool elicitation and argument merge behavior descriptor-driven or feature-module owned.
- [ ] T024 [US2] Add simple testable construction paths for `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/features/encrypt/EncryptWorkflowPlanningService.java`.
- [ ] T025 [US2] Add simple testable construction paths for `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/features/mask/MaskWorkflowPlanningService.java`.
- [ ] T026 [US2] Replace normal collaborator field reflection in touched encrypt/mask tests with constructor-based setup.
- [ ] T027 [US2] Document any intentional public contract changes caused by removing legacy aliases.
- [ ] T028 [US2] Align direct static and constructor mocking in touched tests with repository mocking guidance.

## Phase 4: E2E Clarity Cleanup

- [ ] T030 [US3] Split `LLMMCPConversationRunner` by model loop, tool bridge, final-answer verifier, safety validator, and artifacts.
  Target path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPConversationRunner.java`.
- [ ] T031 [US3] Reuse MCP tool descriptors or list responses instead of manually reconstructing tool schemas in the E2E runner.
- [ ] T032 [US3] Add named assertion helpers for common MCP payload shapes under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp`.
- [ ] T033 [US3] Replace repeated nested `Map<String, Object>` casts in touched E2E tests with the assertion helpers.
- [ ] T034 [US3] Split `ProductionH2RuntimeSmokeE2ETest` by product surface when touched.
- [ ] T035 [US3] Separate generic runtime setup from LLM scenario fixtures in H2 and MySQL runtime support.
- [ ] T036 [US3] Centralize manual wait/retry loops in a small E2E helper.
- [ ] T037 [US3] Keep live LLM usability tests opt-in and outside default CI.

## Phase 5: Verification and Handoff

- [ ] T040 Run `git diff --check`.
- [ ] T041 Run scoped Maven tests for touched MCP modules.
- [ ] T042 Run scoped Checkstyle with `-Pcheck` for touched Java modules.
- [ ] T043 Run scoped E2E tests when `test/e2e/mcp` behavior changes.
- [ ] T044 Re-run `git branch --show-current` and confirm it is still `001-shardingsphere-mcp`.
- [ ] T045 Update `specs/006-mcp-design-clarity-cleanup/requirements.md` if implementation decisions change the requirement inventory.

## Dependencies & Execution Order

- Phase 1 blocks all implementation.
- Start with T010, then T020 through T023, before broader core cleanup.
- E2E cleanup is in scope for this feature and should start once canonical payload helpers are available.
- Verification tasks must run after each implementation slice, not only at the end.
