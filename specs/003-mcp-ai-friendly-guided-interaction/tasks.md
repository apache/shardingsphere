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

# Tasks: AI-Friendly MCP Lightweight Requirements

**Input**: `spec.md`, `requirements.md`, and `plan.md`
**Tests**: Required only when implementation changes begin. This pass organizes requirements and does not implement runtime behavior.
**Constraint**: Do not switch branches. Do not execute branch-changing Spec Kit commands, `git switch`, or `git checkout`.

## Format: `[ID] [P?] [Priority] Description`

- **[P]**: Can be done in parallel after prerequisites are complete.
- **[Priority]**: P0, P1, or P2 from `spec.md`.
- These tasks are a lightweight backlog, not a mandate to build everything in one change.

## Phase 1: Requirements Alignment

- [x] T001 Confirm the current branch without switching branches.
- [x] T002 Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md` before editing requirements.
- [x] T003 Compare `specs/003-mcp-ai-friendly-guided-interaction/` with `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md`.
- [x] T004 Remove over-designed active scope from Spec Kit requirements: broad golden transcript suites, default real-model E2E,
  model-confusion matrices, sampling/progress/logging/roots work, graph/planner/memory/vector systems, and broad tool matrices.
- [x] T005 Keep `requirements.md` synchronized with the active lightweight `docs/mcp` requirement baseline.
- [x] T006 Update `spec.md`, `plan.md`, and `tasks.md` so the Spec Kit directory points to the same lightweight scope.

## Phase 2: P0 Public Surface Clarity

- [ ] T007 [P0] Inspect `mcp/README.md`, `mcp/README_ZH.md`, descriptor YAML, and `shardingsphere://capabilities` for public surface drift.
- [ ] T008 [P0] Update server instructions so models start from `shardingsphere://capabilities` and resource-first discovery.
- [ ] T009 [P0] Mark historical PRD/design content as non-current where it still presents early tool matrices as if active.
- [ ] T010 [P0] Add a focused capabilities shape check for resources, resource templates, tools, prompts, completion targets, navigation, protocol availability, and fingerprints.
- [ ] T011 [P0] Add descriptor lint for empty or placeholder descriptions, missing side-effect/approval wording, missing enum values, and missing key output schema fields.

## Phase 3: P0 Next Actions and Safe Continuation

- [ ] T012 [P0] Inventory existing `next_actions`, `recommended_next_tool`, `suggested_next_tool`, approval, and workflow guidance fields.
- [ ] T013 [P0] Standardize new guidance on `next_actions` while preserving existing compatibility fields where already exposed.
- [ ] T014 [P0] Ensure `execute_update` preview responses include reusable execute arguments only behind explicit user approval.
- [ ] T015 [P0] Ensure `apply_workflow` preview responses include reusable apply arguments and approval requirements.
- [ ] T016 [P0] Add tests that assert no next action suggests guessed secrets, hidden physical objects, or side-effect execution without approval.

## Phase 4: P0 Metadata Search to Resource URI

- [ ] T017 [P0] Verify `search_metadata` URI derivation for database, schema, table, column, index, view, and sequence objects.
- [ ] T018 [P0] Add or adjust `resource_uri`, `parent_resource_uri`, and `next_resource_uris` fields only where descriptor-backed derivation is safe.
- [ ] T019 [P0] Return derivation status and reason when URI derivation is unsafe or unsupported.
- [ ] T020 [P0] Add focused tests for successful derivation and non-guessing failure paths.

## Phase 5: P0 Output Schema and Recovery Accuracy

- [ ] T021 [P0] Compare descriptor output schemas with actual payloads for `search_metadata`, `execute_query`, `execute_update`,
  `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow`, and `validate_workflow`.
- [ ] T022 [P0] Correct schema fields, enum values, required fields, and nested object shapes where they drift.
- [ ] T023 [P0] Expand recovery for missing `database`, missing `execution_mode`, wrong SQL tool, unknown tool/resource, and stale workflow `plan_id`.
- [ ] T024 [P0] Keep wrong-tool SQL recovery on preview-first `execute_update` and preserve user approval.
- [ ] T025 [P0] Add focused unit tests for each recovery branch.

## Phase 6: P1 Resource Navigation and Examples

- [ ] T026 [P1] Add `self_uri` to list/detail resource responses where the current resource URI is known.
- [ ] T027 [P1] Add `parent_uri`, `count`, and `next_resources` where they are safe and public.
- [ ] T028 [P1] Keep resource navigation lightweight and avoid a runtime graph traversal service.
- [ ] T029 [P1] Add compact static examples for `execute_update` preview, workflow planning, `apply_workflow` preview, and `validate_workflow`.
- [ ] T030 [P1] Verify examples are secret-free and do not use production identifiers or environment-specific paths.

## Phase 7: P1 Completion and Algorithm Templates

- [ ] T031 [P1] Add deterministic contains fallback when prefix completion has no result.
- [ ] T032 [P1] Prefer context-scoped table and column candidates when database/schema/table context is supplied.
- [ ] T033 [P1] Keep current-session `plan_id` completion ordered by eligible recent plans.
- [ ] T034 [P1] Verify completion does not use vector search, model calls, cross-session history, or user behavior learning.
- [ ] T035 [P1] Expose encrypt and mask algorithm required properties, optional properties, defaults, secret flags, and capability hints through algorithm resources.
- [ ] T036 [P1] Clarify `approved_steps` accepted values and preview-to-execute reuse guidance.

## Phase 8: P2 First-Use Experience

- [ ] T037 [P2] Improve HTTP startup hints with endpoint, config path, log path, token hint, and runtime database count.
- [ ] T038 [P2] Keep STDIO stdout reserved for MCP protocol and document stderr/file logging expectations.
- [ ] T039 [P2] Add first-use client configuration examples for common MCP clients.
- [ ] T040 [P2] Document troubleshooting for Java version, JDBC driver, HTTP token, STDIO log pollution, empty tool/resource lists, and workflow topology mistakes.
- [ ] T041 [P2] Normalize count and pagination wording on large resource lists.

## Phase 9: P2 Opt-In LLM Usability

- [ ] T042 [P2] Add a small opt-in scenario for side-effecting SQL requiring preview before execute.
- [ ] T043 [P2] Add a small opt-in scenario for metadata search returning a readable detail resource URI.
- [ ] T044 [P2] Add a small opt-in scenario for workflow order: plan, apply preview, execute or manual artifact, then validate.
- [ ] T045 [P2] Keep all live model credentials and real-model calls outside default CI.

## Explicitly Deferred

- [ ] D001 Do not add normalized golden transcript mega suites in this increment.
- [ ] D002 Do not add broad real-model E2E expansion or default-CI live model tests.
- [ ] D003 Do not add model-confusion matrices before focused recovery tests prove the gap.
- [ ] D004 Do not add MCP-native sampling, progress, logging, or roots work without a separate SDK-supported requirement.
- [ ] D005 Do not add metadata freshness semantics, config environment variable interpolation, current-session workflow list resources,
  broad tool matrices, planner, graph engine, vector search, cross-session memory, RBAC platform, or hidden execution shortcuts.

## Verification

For this requirements-only pass:

- [x] V001 Run `git diff --check`.

For later implementation:

- [ ] V002 Run scoped module tests for touched MCP modules.
- [ ] V003 Run scoped Checkstyle/Spotless gates when Java or descriptor code changes.
- [ ] V004 Record exact commands, exit codes, skipped checks, and remaining risks in the handoff.
