# Tasks: MCP AI-Friendly Lightweight Experience

**Input**: Design documents from `/.specify/specs/008-mcp-ai-friendly-lightweight-experience/`
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `quickstart.md`
**Tests**: Required for current-surface alignment, search metadata URI hints, output schema alignment, side-effect preview, common recovery,
descriptor lint, and capabilities contract. Real-model E2E remains opt-in.

**Organization**: Tasks are grouped by user story so each improvement can land in small reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Non-Negotiable Constraints

- Do not switch, create, or check out git branches.
- Preserve resource-first metadata discovery.
- Preserve preview-before-execute for side-effecting operations.
- Finish P0 before P1/P2 comfort work.
- Keep real-model E2E opt-in.
- Do not introduce vector search, cross-session memory, hidden planner calls, or a full authorization platform.

---

## Phase 1: Setup and inventory

- [ ] T001 Confirm the current branch with `git branch --show-current` and record that no branch switching is required.
- [ ] T002 Inventory current public tools from descriptor YAML under `mcp/**/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`.
- [ ] T003 [P] Inventory public tool lists and first-use paths in `mcp/README.md`, `mcp/README_ZH.md`, and `docs/mcp/`.
- [ ] T004 [P] Inventory current response guidance fields in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/`
  and `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/`.
- [ ] T005 [P] Inventory current `search_metadata` payloads and descriptor-backed metadata resource URI patterns.
- [ ] T006 [P] Inventory real payloads and output schemas for `search_metadata`, `execute_query`, `execute_update`, `plan_encrypt_rule`,
  `plan_mask_rule`, `apply_workflow`, and `validate_workflow`.
- [ ] T007 Record Phase 0 analysis notes for P0 items: current behavior evidence, affected paths, verification map, non-goals, and rollback boundary.

---

## Phase 2: Foundational guidance contracts

- [ ] T008 Define the preferred next-action field vocabulary in
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java`,
  or a small shared helper if reuse is needed.
- [ ] T009 Define safe URI derivation rules for metadata search results without adding a parallel `list_*` tool matrix.
- [ ] T010 Define the output schema alignment checklist for the seven core tools.
- [ ] T011 Define the minimal recovery envelope expectations in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`.
- [ ] T012 [P] Define compact example placement in descriptor YAML or README sections without adding a new example framework.

**Checkpoint**: Guidance shape is agreed before descriptor, response, and test updates proceed.

---

## Phase 3: User Story 1 - Model sees the current MCP surface consistently (Priority: P0)

**Goal**: Keep README, descriptors, capabilities, metadata search, and output schemas aligned with the current resource-first public surface.
**Independent Test**: Compare README public tools with descriptor-backed capabilities, direct metadata URI hints, and core tool schemas.

### Tests for User Story 1

- [ ] T013 [P] [US1] Add or update descriptor/catalog tests under
  `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/` to expose descriptor-backed public tool names.
- [x] T014 [P] [US1] Add `search_metadata` tests that assert direct `resource_uri` values only when descriptor-backed derivation is safe.
- [ ] T015 [P] [US1] Add schema alignment checks for the seven core tools without locking large runtime snapshots.
- [ ] T016 [P] [US1] Add a documentation consistency check if practical, or keep manual verification explicit in `quickstart.md`.

### Implementation for User Story 1

- [ ] T017 [US1] Update `mcp/README.md` so the public tool list matches descriptor-backed tools.
- [ ] T018 [US1] Update `mcp/README_ZH.md` so the public tool list matches descriptor-backed tools.
- [ ] T019 [US1] Add status notes to historical docs under `docs/mcp/` when old tool lists could be mistaken for the current contract.
- [x] T020 [US1] Update `search_metadata` response construction to expose safe `resource_uri`, parent URI, and next-hop URI hints.
- [ ] T021 [US1] Align descriptor output schemas with real payloads for the seven core tools.

**Checkpoint**: A reader or model can identify the current MCP public surface and navigate metadata without relying on obsolete tool names or guessed URIs.

---

## Phase 4: User Story 2 - Model safely handles side-effecting actions (Priority: P0)

**Goal**: Make SQL preview and workflow preview easier for models to follow safely.
**Independent Test**: Preview responses expose approval, side-effect scope, and reusable follow-up arguments.

### Tests for User Story 2

- [ ] T022 [P] [US2] Update
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandlerTest.java`
  to assert reusable `suggested_arguments` and structured approval guidance.
- [ ] T023 [P] [US2] Update workflow apply tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`
  or `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/workflow/` to assert preview next-action shape.

### Implementation for User Story 2

- [ ] T024 [US2] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java` so preview guidance uses the agreed next-action vocabulary.
- [ ] T025 [US2] Update workflow preview guidance in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java` and related workflow guidance helpers.

**Checkpoint**: Models can preview side effects and reuse safe follow-up arguments without reconstructing SQL or workflow state.

---

## Phase 5: User Story 3 - Model repairs common mistakes from structured recovery (Priority: P0)

**Goal**: Make five common model mistakes recoverable from structured fields.
**Independent Test**: Trigger each common mistake and assert recovery metadata.

### Tests for User Story 3

- [ ] T026 [P] [US3] Update `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java` for missing database and missing execution mode recovery.
- [ ] T027 [P] [US3] Add wrong SQL tool recovery tests preserving preview and approval semantics.
- [ ] T028 [P] [US3] Add unknown resource/tool and unavailable plan id recovery tests.

### Implementation for User Story 3

- [ ] T029 [US3] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java` with the minimal recovery categories and safe suggestions.
- [ ] T030 [US3] Ensure recovery suggested arguments contain only server-known or user-provided values.

**Checkpoint**: Common model mistakes recover without guessing hidden values.

---

## Phase 6: User Story 5 - Maintainers catch model-surface regressions early (Priority: P0)

**Goal**: Add minimal deterministic regression guards for model-facing metadata.
**Independent Test**: Run descriptor lint and a capabilities contract test locally.

### Tests for User Story 5

- [ ] T031 [P] [US5] Add descriptor lint tests under `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T032 [P] [US5] Add a lightweight capabilities contract test under
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/`
  or `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`.
- [ ] T033 [P] [US5] Add navigation reference validation for descriptor-owned navigation entries.

### Implementation for User Story 5

- [ ] T034 [US5] Implement minimal descriptor lint using existing descriptor catalog loading code in `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T035 [US5] Keep capabilities contract assertions small: section presence, basic field shape, and fingerprint presence only.

**Checkpoint**: Obvious model-facing surface regressions fail deterministic tests.

---

## Phase 7: User Story 4 - Model follows short task paths and examples (Priority: P1)

**Goal**: Add compact paths and examples without expanding runtime behavior.
**Independent Test**: Inspect README, prompts, and descriptors for concise paths and examples.

### Tests for User Story 4

- [ ] T036 [P] [US4] Add descriptor lint or prompt-template tests for short stop/ask-user guidance where the prompt owns a workflow path.
- [ ] T037 [P] [US4] Add descriptor example checks if examples are stored in descriptor metadata.

### Implementation for User Story 4

- [ ] T038 [US4] Update `mcp/README.md` with short metadata, safe SQL, and workflow paths.
- [ ] T039 [US4] Update `mcp/README_ZH.md` with the same short paths.
- [ ] T040 [US4] Add compact examples for `execute_update` preview, `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow` preview, and `validate_workflow`.
- [ ] T041 [US4] Ensure examples are static, secret-free, and not tied to production database names.

**Checkpoint**: Models and integrators can learn the common flows without reading long design documents.

---

## Phase 8: P1/P2 follow-ups

- [ ] T042 [P] Add optional current-session workflow plan summary only if recovery still requires too much guessing after P0 recovery work.
- [ ] T043 [P] Add metadata resource navigation hints without introducing a general graph traversal engine.
- [ ] T044 [P] Add completion context-priority tests in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/` without model calls.
- [ ] T045 [P] Add algorithm property templates for encrypt and mask resources only where descriptor or existing metadata already knows the fields.
- [ ] T046 [P] Add metadata freshness hints only if the existing metadata context can expose them without active refresh.
- [ ] T047 [P] Add startup diagnostics for HTTP endpoint, STDIO logging rules, token expectations, config path, log path, and runtime database count.
- [ ] T048 [P] Add environment-variable reference support for HTTP access token and runtime database password without adding a secret manager.
- [ ] T049 [P] Add troubleshooting documentation and opt-in LLM usability scenarios only after deterministic P0 tests pass.

---

## Phase 9: Verification

- [ ] T050 Run scoped deterministic tests:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`.
- [ ] T051 Run scoped Checkstyle:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests checkstyle:check`.
- [ ] T052 Run scoped Spotless check:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests spotless:check`.
- [ ] T053 Verify opt-in LLM E2E remains excluded from default CI.
- [ ] T054 Confirm no branch switch occurred with `git branch --show-current`.

## Dependencies and Execution Order

- Phase 1 blocks all later work.
- Phase 2 should finish before changing response shapes.
- User Story 1 can land independently and should happen early to clarify the current contract.
- User Story 2 and User Story 3 can proceed in parallel after Phase 2 if write scopes stay separate.
- User Story 5 can proceed after descriptor lint shape is agreed and should finish before P1/P2 comfort work.
- User Story 4 can proceed after the preferred example placement is chosen and P0 contracts are stable.
- P1/P2 follow-ups should not block P0 completion.

## Parallel Opportunities

- T002, T003, T004, T005, and T006 can run in parallel.
- T013, T014, T015, and T016 can run in parallel.
- T022 and T023 can run in parallel.
- T026, T027, and T028 can run in parallel.
- T031, T032, and T033 can run in parallel.
- T036 and T037 can run in parallel.
- T042 through T049 are independent follow-ups after P0 contracts stabilize.
