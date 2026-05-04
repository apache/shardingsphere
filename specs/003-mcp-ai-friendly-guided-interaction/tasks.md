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

# Tasks: AI-Friendly MCP Experience Hardening

**Input**: Design documents from `specs/003-mcp-ai-friendly-guided-interaction/`
**Prerequisites**: `spec.md`, `requirements.md`, `plan.md`, `research.md`, `data-model.md`, `quickstart.md`
**Tests**: Required. This increment exists to harden model-facing contracts and model-experience validation.
**Constraint**: Do not switch branches. These tasks describe future implementation work; they are not executed by this requirements pass.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel after dependencies are complete.
- **[Story]**: User story covered by the task.
- Include exact repository paths in descriptions.

## Phase 1: Setup

- [ ] T001 Confirm branch is still `001-shardingsphere-mcp` with `git branch --show-current`.
- [ ] T002 Re-read `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- [ ] T003 Inspect existing prompt, completion, preview, workflow-session, and E2E trace implementations.
- [ ] T004 Confirm no generated `target/` paths are in scope.
- [ ] T005 Confirm real-model E2E remains outside default CI.

## Phase 2: Documentation Correction

- [ ] T006 [P] [US1] Update prompt and completion documentation in `mcp/README.md`.
- [ ] T007 [P] [US1] Update prompt and completion documentation in `mcp/README_ZH.md`.
- [ ] T008 [P] [US2] Document opt-in real-model E2E expectations in `mcp/README.md`.
- [ ] T009 [P] [US2] Document opt-in real-model E2E expectations in `mcp/README_ZH.md`.
- [ ] T010 Verify README content no longer states prompt or completion support is deferred.

## Phase 3: User Story 1 - Protocol Surface Cannot Regress (Priority: P0)

**Goal**: Add normalized golden tests for the protocol-visible model surface.
**Independent Test**: Golden tests fail when model-facing contract fields are removed.

### Tests for User Story 1

- [ ] T011 [P] [US1] Add transcript normalization helper under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/`.
- [ ] T012 [P] [US1] Add golden fixtures for `initialize`, `tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`, `completion/complete`, and capabilities resource.
- [ ] T013 [US1] Add golden assertion tests under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/`.
- [ ] T014 [US1] Add descriptor contract assertions for prompt, completion, preview enum, annotations, and safety metadata.

### Implementation for User Story 1

- [ ] T015 [US1] Expose any missing stable fields required by golden tests without adding dynamic payload noise.
- [ ] T016 [US1] Keep golden fixtures small and reviewable by excluding large runtime data.

## Phase 4: User Story 2 - Real Models Prove the Surface Is Comfortable (Priority: P0)

**Goal**: Add opt-in real-model scenarios with trace-based assertions and credential redaction.
**Independent Test**: Real-model E2E validates observable MCP interaction traces when explicitly enabled.

### Tests for User Story 2

- [ ] T017 [P] [US2] Add real-model metadata inspection scenario in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`.
- [ ] T018 [P] [US2] Add real-model safe SQL preview scenario in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`.
- [ ] T019 [P] [US2] Add real-model encrypt workflow scenario in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`.
- [ ] T020 [P] [US2] Add real-model mask workflow scenario in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`.
- [ ] T021 [US2] Add unavailable-model skip or unavailable reporting test.

### Implementation for User Story 2

- [ ] T022 [US2] Extend real-model artifact metadata with provider, model identifier, prompt set version, descriptor catalog version, and scenario ID.
- [ ] T023 [US2] Redact API keys, tokens, and credentials from real-model logs and artifacts.
- [ ] T024 [US2] Assert prompt calls, completion calls, resource reads, tool order, schema-valid arguments, preview-before-execution, approval boundary, recovery path, and final validation.
- [ ] T025 [US2] Verify real-model E2E profile remains opt-in and excluded from default CI.

## Phase 5: User Story 3 - Completion Results Prefer Useful Candidates (Priority: P2)

**Goal**: Improve completion ordering with deterministic, context-aware rules only.
**Independent Test**: Completion tests assert exact match, context match, plan recency, and feature-context ranking.

### Tests for User Story 3

- [ ] T026 [P] [US3] Add completion ranking tests in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/`.
- [ ] T027 [P] [US3] Add workflow plan recency completion tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`.
- [ ] T028 [P] [US3] Add feature-specific algorithm ranking tests for encrypt and mask completion references.

### Implementation for User Story 3

- [ ] T029 [US3] Implement exact-prefix-before-broader-prefix ranking in `MCPCompletionSpecificationFactory`.
- [ ] T030 [US3] Implement context-strength ranking for metadata completions.
- [ ] T031 [US3] Implement current-session plan update-time ranking.
- [ ] T032 [US3] Implement feature-reference ranking for algorithm completions.
- [ ] T033 [US3] Verify no cross-session history, vector search, model calls, or user behavior learning was added.

## Phase 6: User Story 4 - Models Repair Errors From Structured Recovery (Priority: P2)

**Goal**: Standardize recoverable error metadata for safe model retries.
**Independent Test**: A model-like caller can repair common failures using structured recovery fields.

### Tests for User Story 4

- [ ] T034 [P] [US4] Add missing argument recovery envelope tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/`.
- [ ] T035 [P] [US4] Add invalid enum and unsupported metadata object recovery tests.
- [ ] T036 [P] [US4] Add unsupported tool and unsupported resource recovery tests.
- [ ] T037 [P] [US4] Add wrong SQL tool recovery tests preserving preview and approval.
- [ ] T038 [P] [US4] Add unavailable workflow plan recovery tests.
- [ ] T039 [US4] Add model-like retry scenario tests in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`.

### Implementation for User Story 4

- [ ] T040 [US4] Standardize recovery envelope fields in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/`.
- [ ] T041 [US4] Add safe `suggested_next_tool`, `suggested_arguments`, `read_resources_first`, and `ask_user_when_uncertain` where computable.
- [ ] T042 [US4] Ensure suggested arguments never contain secrets, guessed values, placeholders, or hidden physical objects.
- [ ] T043 [US4] Ensure unavailable `plan_id` recovery recommends replanning in the current MCP session.

## Phase 7: User Story 5 - Resource Navigation Shows the Next MCP Hop (Priority: P1)

**Goal**: Add lightweight descriptor-owned navigation metadata to capabilities.
**Independent Test**: Navigation entries resolve to existing public MCP identifiers.

### Tests for User Story 5

- [ ] T044 [P] [US5] Add navigation descriptor tests in `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T045 [P] [US5] Add capability payload navigation tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/`.

### Implementation for User Story 5

- [ ] T046 [US5] Add lightweight navigation metadata to descriptor YAML or equivalent descriptor input under `mcp/**/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`.
- [ ] T047 [US5] Add metadata hierarchy navigation entries.
- [ ] T048 [US5] Add feature algorithm to planning navigation entries.
- [ ] T049 [US5] Add workflow plan to apply and validate navigation entries.
- [ ] T050 [US5] Expose descriptor-owned navigation in capability payload without adding a graph engine or traversal tool.

## Phase 8: User Story 6 - Side-Effecting Tools Require Explicit Execution Mode (Priority: P0)

**Goal**: Prevent omitted execution mode from causing side effects.
**Independent Test**: Missing `execution_mode` is rejected and recovery recommends preview.

### Tests for User Story 6

- [ ] T051 [P] [US6] Add missing-mode tests for `execute_update` in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [ ] T052 [P] [US6] Add transport contract tests proving `execute_update` descriptor exposes explicit mode requirements.
- [ ] T053 [US6] Add recovery tests proving missing mode recommends `execution_mode=preview`.

### Implementation for User Story 6

- [ ] T054 [US6] Require explicit `execution_mode` in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [ ] T055 [US6] Update `execute_update` descriptor descriptions and schema in `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`.
- [ ] T056 [US6] Verify no omitted-mode direct execution compatibility remains for model-facing update calls.

## Phase 9: User Story 7 through User Story 10 - P0/P1 Model Guidance Contracts

**Goal**: Add clarification, completion diagnostics, descriptor lint, fingerprints, next-action metadata, prompt stop conditions, and transport contracts.
**Independent Test**: Deterministic tests prove each model-facing guidance field is visible and safe.

### Tests

- [ ] T057 [P] [US7] Add completion diagnostic tests for missing context and ranking reasons in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/`.
- [ ] T058 [P] [US7] Add structured clarification tests for pending questions, missing arguments, and ask-user status.
- [ ] T059 [P] [US8] Add descriptor lint tests under `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T060 [P] [US8] Add capability fingerprint tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/`.
- [ ] T061 [P] [US9] Add shared next-action metadata tests for representative tool outputs, resource outputs, prompt outputs, and recovery envelopes.
- [ ] T062 [P] [US9] Add prompt stop-condition tests for support, encrypt, and mask prompt templates.
- [ ] T063 [P] [US10] Add `prompts/get`, `completion/complete`, capabilities, navigation, and explicit side-effect mode transport contract tests.

### Implementation

- [ ] T064 [US7] Add native elicitation support when the SDK exposes stable APIs, otherwise add structured fallback clarification fields.
- [ ] T065 [US7] Add completion diagnostic metadata while keeping completion values as plain strings.
- [ ] T066 [US8] Implement descriptor lint for descriptions, schemas, enum values, annotations, safety hints, prompt links, completion targets, navigation, and examples.
- [ ] T067 [US8] Add deterministic descriptor catalog, prompt set, navigation, and schema fingerprints to capabilities.
- [ ] T068 [US8] Record fingerprints in golden transcripts and real-model E2E reports.
- [ ] T069 [US9] Standardize next-action metadata fields across outputs and recovery.
- [ ] T070 [US9] Add prompt stop conditions and ask-user conditions to prompt templates.
- [ ] T071 [US10] Ensure transport-level payloads expose prompt, completion diagnostic, fingerprint, navigation, and explicit mode fields.

## Phase 10: User Story 11 - Naming, Pagination, Native Signals, and Roots Feel Native (Priority: P2)

**Goal**: Make second-order protocol surfaces clear and bounded for models.
**Independent Test**: Descriptor lint and contract tests cover names, pagination, sampling, progress, logging, and boundaries.

### Tests for User Story 11

- [ ] T072 [P] [US11] Add naming clarity lint cases for read-only, preview, side-effecting, planning, validation, and lookup actions.
- [ ] T073 [P] [US11] Add pagination contract tests for large result surfaces.
- [ ] T074 [P] [US11] Add progress metadata tests for long-running workflow actions when SDK support exists.
- [ ] T075 [P] [US11] Add sampling and logging boundary tests when SDK support exists.
- [ ] T076 [P] [US11] Add roots or permission-boundary descriptor tests for future file or configuration resources when such resources become public.
- [ ] T077 [P] [US11] Add prompt argument coverage tests.

### Implementation for User Story 11

- [ ] T078 [US11] Rename or clarify ambiguous tool and resource descriptions without changing handler semantics unnecessarily.
- [ ] T079 [US11] Normalize pagination fields for bounded list outputs.
- [ ] T080 [US11] Add native or structured progress metadata where long-running workflows need it.
- [ ] T081 [US11] Add sampling and logging only for concrete workflow need, never for hidden planning, execution, or completion ranking.
- [ ] T082 [US11] Document every prompt argument as completion-backed, resource-backed, user-provided-only, or optional.

## Phase 11: User Story 12 - Examples and Negative Paths Teach Safe Use (Priority: P2)

**Goal**: Give models compact examples and deterministic recovery from common mistakes.
**Independent Test**: Descriptor lint and model-confusion tests validate examples and safe recovery.

### Tests for User Story 12

- [ ] T083 [P] [US12] Add descriptor lint for compact static examples on workflow plan, SQL preview, metadata search, completion diagnostics, and recovery outputs.
- [ ] T084 [P] [US12] Add model-confusion tests for apply-before-plan and execute-before-preview.
- [ ] T085 [P] [US12] Add model-confusion tests for missing execution mode and stale `plan_id`.
- [ ] T086 [P] [US12] Add model-confusion tests for unknown database, ambiguous metadata, invalid enum, and wrong SQL tool.

### Implementation for User Story 12

- [ ] T087 [US12] Add compact secret-free output examples to complex output descriptors or descriptor metadata.
- [ ] T088 [US12] Ensure confusion-path recovery uses structured next-action metadata rather than exact prose.

## Phase 12: Verification

- [ ] T089 Run scoped tests for touched modules:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`.
- [ ] T090 Run scoped Checkstyle:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests checkstyle:check`.
- [ ] T091 Run scoped Spotless:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests spotless:check`.
- [ ] T092 Verify real-model E2E remains opt-in.
- [ ] T093 Verify no branch switch occurred with `git branch --show-current`.

## Dependencies and Execution Order

- Phase 1 setup blocks all implementation work.
- Phase 2 documentation correction should happen before real-model scenario authoring.
- Phase 3 golden tests should land before Phase 5 through Phase 7 changes.
- Phase 4 real-model E2E can proceed after prompt/completion trace assertions are stable.
- Phase 5 completion ranking is independent after golden tests exist.
- Phase 6 structured recovery is independent after existing recovery tests are understood.
- Phase 7 navigation should run after descriptor validation conventions are stable.
- Phase 8 explicit side-effect mode should run before real-model side-effect assertions are finalized.
- Phase 9 guidance contracts should run before Phase 10 and Phase 11 so later tests reuse shared metadata.
- Phase 10 ergonomics checks can run after descriptor lint exists.
- Phase 11 model-confusion tests should run after recovery and next-action metadata are stable.

## Parallel Opportunities

- T006 through T009 can run in parallel.
- T011 through T014 can run in parallel after normalization rules are defined.
- T017 through T020 can run in parallel because they cover separate real-model scenarios.
- T026 through T028 can run in parallel after completion ranking rules are fixed.
- T034 through T038 can run in parallel after the recovery envelope shape is fixed.
- T044 and T045 can run in parallel after navigation shape is fixed.
- T051 through T053 can run in parallel after the explicit mode requirement is accepted.
- T057 through T063 can run in parallel after shared field names are fixed.
- T072 through T077 can run in parallel after descriptor lint infrastructure exists.
- T083 through T086 can run in parallel after recovery categories are stable.
