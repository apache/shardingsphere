# Tasks: MCP AI-Friendly Lightweight Experience

**Input**: Design documents from `/.specify/specs/008-mcp-ai-friendly-lightweight-experience/`
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `quickstart.md`
**Tests**: Required for descriptor lint, common recovery, capabilities contract, and side-effect preview behavior. Real-model E2E remains opt-in.

**Organization**: Tasks are grouped by user story so each improvement can land in small reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Non-Negotiable Constraints

- Do not switch, create, or check out git branches.
- Preserve resource-first metadata discovery.
- Preserve preview-before-execute for side-effecting operations.
- Keep real-model E2E opt-in.
- Do not introduce vector search, cross-session memory, hidden planner calls, or a full authorization platform.

---

## Phase 1: Setup and inventory

- [ ] T001 Confirm the current branch with `git branch --show-current` and record that no branch switching is required.
- [ ] T002 Inventory current public tools from descriptor YAML under `mcp/**/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`.
- [ ] T003 [P] Inventory public tool lists and first-use paths in `mcp/README.md`, `mcp/README_ZH.md`, and `docs/mcp/`.
- [ ] T004 [P] Inventory current response guidance fields in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/`
  and `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/`.

---

## Phase 2: Foundational guidance contracts

- [ ] T005 Define the preferred next-action field vocabulary in
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java`,
  or a small shared helper if reuse is needed.
- [ ] T006 Define the minimal recovery envelope expectations in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`.
- [ ] T007 [P] Define compact example placement in descriptor YAML or README sections without adding a new example framework.

**Checkpoint**: Guidance shape is agreed before descriptor, response, and test updates proceed.

---

## Phase 3: User Story 1 - Model sees the current MCP surface consistently (Priority: P1)

**Goal**: Keep README, descriptors, and capabilities aligned with the current resource-first public surface.
**Independent Test**: Compare README public tools with descriptor-backed capabilities.

### Tests for User Story 1

- [ ] T008 [P] [US1] Add or update a descriptor/catalog test under
  `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/` to expose the descriptor-backed public tool names for assertions.
- [ ] T009 [P] [US1] Add a documentation consistency check if practical, or document the manual verification in `quickstart.md`.

### Implementation for User Story 1

- [ ] T010 [US1] Update `mcp/README.md` so the public tool list matches descriptor-backed tools.
- [ ] T011 [US1] Update `mcp/README_ZH.md` so the public tool list matches descriptor-backed tools.
- [ ] T012 [US1] Add status notes to historical docs under `docs/mcp/` when old tool lists could be mistaken for the current contract.

**Checkpoint**: A reader can identify the current MCP public surface without being pulled toward obsolete tool names.

---

## Phase 4: User Story 2 - Model safely handles side-effecting actions (Priority: P1)

**Goal**: Make SQL preview and workflow preview easier for models to follow safely.
**Independent Test**: Preview responses expose approval, side-effect scope, and reusable follow-up arguments.

### Tests for User Story 2

- [ ] T013 [P] [US2] Update
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandlerTest.java`
  to assert reusable `suggested_arguments` and structured approval guidance.
- [ ] T014 [P] [US2] Update workflow apply tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`
  or `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/workflow/` to assert preview next-action shape.

### Implementation for User Story 2

- [ ] T015 [US2] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java` so preview guidance uses the agreed next-action vocabulary.
- [ ] T016 [US2] Update workflow preview guidance in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java` and related workflow guidance helpers.

**Checkpoint**: Models can preview side effects and reuse safe follow-up arguments without reconstructing SQL or workflow state.

---

## Phase 5: User Story 3 - Model repairs common mistakes from structured recovery (Priority: P1)

**Goal**: Make five common model mistakes recoverable from structured fields.
**Independent Test**: Trigger each common mistake and assert recovery metadata.

### Tests for User Story 3

- [ ] T017 [P] [US3] Update `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java` for missing database and missing execution mode recovery.
- [ ] T018 [P] [US3] Add wrong SQL tool recovery tests preserving preview and approval semantics.
- [ ] T019 [P] [US3] Add unknown resource/tool and unavailable plan id recovery tests.

### Implementation for User Story 3

- [ ] T020 [US3] Update `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java` with the minimal recovery categories and safe suggestions.
- [ ] T021 [US3] Ensure recovery suggested arguments contain only server-known or user-provided values.

**Checkpoint**: Common model mistakes recover without guessing hidden values.

---

## Phase 6: User Story 4 - Model follows short task paths and examples (Priority: P2)

**Goal**: Add compact paths and examples without expanding runtime behavior.
**Independent Test**: Inspect README, prompts, and descriptors for concise paths and examples.

### Tests for User Story 4

- [ ] T022 [P] [US4] Add descriptor lint or prompt-template tests for presence of short stop/ask-user guidance where the prompt owns a workflow path.
- [ ] T023 [P] [US4] Add descriptor example checks if examples are stored in descriptor metadata.

### Implementation for User Story 4

- [ ] T024 [US4] Update `mcp/README.md` with short metadata, safe SQL, and workflow paths.
- [ ] T025 [US4] Update `mcp/README_ZH.md` with the same short paths.
- [ ] T026 [US4] Add compact examples for `execute_update` preview, `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow` preview, and `validate_workflow` in descriptor metadata or README sections.
- [ ] T027 [US4] Ensure examples are static, secret-free, and not tied to production database names.

**Checkpoint**: Models and integrators can learn the common flows without reading long design documents.

---

## Phase 7: User Story 5 - Maintainers catch model-surface regressions early (Priority: P2)

**Goal**: Add minimal deterministic regression guards for model-facing metadata.
**Independent Test**: Run descriptor lint and a capabilities contract test locally.

### Tests for User Story 5

- [ ] T028 [P] [US5] Add descriptor lint tests under `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T029 [P] [US5] Add a lightweight capabilities contract test under
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/`
  or `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`.
- [ ] T030 [P] [US5] Add navigation reference validation for descriptor-owned navigation entries.

### Implementation for User Story 5

- [ ] T031 [US5] Implement minimal descriptor lint using existing descriptor catalog loading code in `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T032 [US5] Keep capabilities contract assertions small: section presence, basic field shape, and fingerprint presence only.

**Checkpoint**: Obvious model-facing surface regressions fail deterministic tests.

---

## Phase 8: P1/P2 follow-ups

- [ ] T033 [P] Add optional current-session workflow plan summary resource or tool only if recovery still requires too much guessing after P1 recovery work.
- [ ] T034 [P] Add completion context-priority tests in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/` without model calls or cross-session learning.
- [ ] T035 [P] Add metadata freshness hints only if the existing metadata context can expose them without introducing active refresh.
- [ ] T036 [P] Add troubleshooting documentation for Java version, missing JDBC driver, HTTP token, STDIO stdout logging, and empty discovery results.
- [ ] T037 [P] Extend opt-in LLM usability scenarios in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/` for side-effect preview and workflow order.

---

## Phase 9: Verification

- [ ] T038 Run scoped deterministic tests:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`.
- [ ] T039 Run scoped Checkstyle:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests checkstyle:check`.
- [ ] T040 Run scoped Spotless check:
  `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests spotless:check`.
- [ ] T041 Verify opt-in LLM E2E remains excluded from default CI.
- [ ] T042 Confirm no branch switch occurred with `git branch --show-current`.

## Dependencies and Execution Order

- Phase 1 blocks all later work.
- Phase 2 should finish before changing response shapes.
- User Story 1 can land independently and should happen early to clarify the current contract.
- User Story 2 and User Story 3 can proceed in parallel after Phase 2 if write scopes stay separate.
- User Story 4 can proceed after the preferred example placement is chosen.
- User Story 5 can proceed after descriptor lint shape is agreed.
- P1/P2 follow-ups should not block P0/P1 user-story completion.

## Parallel Opportunities

- T002, T003, and T004 can run in parallel.
- T008 and T009 can run in parallel.
- T013 and T014 can run in parallel.
- T017, T018, and T019 can run in parallel.
- T022 and T023 can run in parallel.
- T028, T029, and T030 can run in parallel.
- T033 through T037 are independent follow-ups after core guidance contracts stabilize.
