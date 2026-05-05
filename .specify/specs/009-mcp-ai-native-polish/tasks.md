# Tasks: MCP AI-Native Polish

**Input**: Design documents from `/.specify/specs/009-mcp-ai-native-polish/`
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `quickstart.md`
**Tests**: Required for new model-facing contracts. Real-model E2E remains opt-in.

**Organization**: Tasks are grouped by user story to enable independent review and implementation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in implementation tasks

## Non-Negotiable Constraints

- Do not switch, create, or check out git branches.
- Preserve resource-first metadata discovery.
- Preserve preview-before-execute for side-effecting operations.
- Keep live-model E2E opt-in.
- Do not introduce planner, graph, vector, memory, approval-token, RBAC, or default-CI live-model systems.

## Phase 1: Setup and Evidence

- [ ] T001 Confirm current branch with `git branch --show-current` and record no branch switch.
- [ ] T002 Inspect `.specify/specs/008-mcp-ai-friendly-lightweight-experience/` and confirm 009 is a follow-up, not a replacement.
- [ ] T003 [P] Inspect `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java` for capabilities payload construction.
- [ ] T004 [P] Inspect current `next_actions` producers under `mcp/core` and `mcp/support`.
- [ ] T005 [P] Inspect `search_metadata` request parsing, matching, pagination, and URI derivation under `mcp/core`.
- [ ] T006 [P] Inspect SQL and workflow output schemas under descriptor YAML files.
- [ ] T007 Record implementation notes: current behavior, affected paths, verification map, non-goals, and rollback boundary.

## Phase 2: Foundation

- [ ] T008 Define the `next_action_contract` payload shape in capabilities.
- [ ] T009 Define the static `common_flows` payload shape in capabilities.
- [ ] T010 Define search context and match explanation fields for `search_metadata`.
- [ ] T011 Define output parse hint fields and schema updates for SQL and workflow payloads.
- [ ] T012 Define approval-summary wording rules for preview responses.

**Checkpoint**: Foundation accepted before production code changes.

## Phase 3: User Story 1 - Model understands the next-action contract (Priority: P0)

**Goal**: Capabilities documents action kinds and required fields.
**Independent Test**: Capabilities contract test asserts the contract section and action-kind fields.

### Tests for User Story 1

- [ ] T013 [P] [US1] Add or update capabilities tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/`.
- [ ] T014 [P] [US1] Add descriptor/catalog validation for action kinds if the contract is descriptor-backed.

### Implementation for User Story 1

- [ ] T015 [US1] Add `next_action_contract` or equivalent section in `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`.
- [ ] T016 [US1] Ensure contract wording preserves preview-before-approval and does not imply hidden execution.

**Checkpoint**: A model can learn next-action required fields from capabilities.

## Phase 4: User Story 2 - Model follows compact common flows (Priority: P0)

**Goal**: Capabilities exposes short descriptor-backed flows.
**Independent Test**: Capabilities contract test validates flow IDs and referenced tools/resources.

### Tests for User Story 2

- [ ] T017 [P] [US2] Add tests that every common-flow tool reference exists in descriptor-backed tools.
- [ ] T018 [P] [US2] Add tests that every common-flow resource reference exists in descriptor-backed resources or templates.

### Implementation for User Story 2

- [ ] T019 [US2] Add compact `common_flows` in `MCPDescriptorCatalog`.
- [ ] T020 [US2] Keep README as the long-form source and capabilities as the short-form model source.

**Checkpoint**: A model can pick first hops from capabilities alone.

## Phase 5: User Story 3 - Model chooses metadata results more safely (Priority: P0)

**Goal**: Search results expose applied scope and match explanation.
**Independent Test**: `search_metadata` tests assert context, match kind, matched fields, and unsafe URI behavior.

### Tests for User Story 3

- [ ] T021 [P] [US3] Add tests in `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/`.
- [ ] T022 [P] [US3] Add unsafe-name URI derivation tests for names containing path or query delimiters.

### Implementation for User Story 3

- [ ] T023 [US3] Extend `MetadataSearchResult` or response construction with applied search context.
- [ ] T024 [US3] Extend `MetadataSearchHit` or related payload with match explanation fields.
- [ ] T025 [US3] Tighten URI derivation to avoid unsafe guessed values.

**Checkpoint**: Metadata search no longer forces the model to infer result quality or scope.

## Phase 6: User Story 4 - Model parses tool outputs with less inference (Priority: P1)

**Goal**: SQL and workflow outputs expose count, applied-limit, status, and item-shape metadata.
**Independent Test**: Descriptor/schema and payload tests assert parse hints.

### Tests for User Story 4

- [ ] T026 [P] [US4] Add `execute_query` payload tests under `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/database/tool/response/`.
- [ ] T027 [P] [US4] Add workflow schema tests under `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`.
- [ ] T028 [P] [US4] Add workflow payload tests under existing workflow service tests.

### Implementation for User Story 4

- [ ] T029 [US4] Add SQL output parse hints where values are already known.
- [ ] T030 [US4] Update descriptor output schemas for workflow status values and nested item shapes.
- [ ] T031 [US4] Add normalized or reusable request summaries to planning responses when safe.

**Checkpoint**: Models can parse SQL and workflow responses with less inference.

## Phase 7: User Story 5 - Model asks for approval with server-owned wording (Priority: P1)

**Goal**: Preview responses provide concise approval summaries.
**Independent Test**: Preview tests assert summaries preserve side-effect and approval semantics.

### Tests for User Story 5

- [ ] T032 [P] [US5] Add `execute_update` preview tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [ ] T033 [P] [US5] Add workflow apply preview tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`.

### Implementation for User Story 5

- [ ] T034 [US5] Add approval summary or approval question to `execute_update` preview.
- [ ] T035 [US5] Add approval summary or approval question to `apply_workflow` preview.

**Checkpoint**: Models can ask users for approval using server-owned wording.

## Phase 8: User Story 6 - Maintainers protect the polish increment (Priority: P1)

**Goal**: Focused deterministic guards protect new model-facing contracts.
**Independent Test**: Module-scoped tests pass without live-model credentials.

- [ ] T036 [P] [US6] Add parity checks between descriptor-backed tools and capabilities tools.
- [ ] T037 [P] [US6] Add focused descriptor lint for new common-flow and action-contract references.
- [ ] T038 [P] [US6] Keep opt-in LLM scenario updates outside default CI.

## Phase 9: Verification

- [ ] T039 Run `git diff --check` for documentation-only changes.
- [ ] T040 Run scoped MCP module tests after Java or descriptor changes.
- [ ] T041 Run scoped Checkstyle after Java changes.
- [ ] T042 Confirm no branch switch occurred with `git branch --show-current`.

## Dependencies and Execution Order

- Phase 1 blocks all implementation.
- Phase 2 blocks user-story work.
- User Stories 1 and 2 should land before broader output polish.
- User Story 3 can proceed in parallel with User Stories 1 and 2 after foundation.
- User Stories 4 and 5 can proceed after current output shapes are verified.
- User Story 6 should land with or immediately after each model-facing contract change.

## Parallel Opportunities

- T003 through T006 can run in parallel.
- T013 and T014 can run in parallel.
- T017 and T018 can run in parallel.
- T021 and T022 can run in parallel.
- T026 through T028 can run in parallel.
- T032 and T033 can run in parallel.
