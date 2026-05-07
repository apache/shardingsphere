# Tasks: MCP AI-Native Polish

**Input**: Design documents from `/.specify/specs/009-mcp-ai-native-polish/`
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `current-behavior-analysis.md`, `breaking-cleanup-analysis.md`,
`implementation-baseline-100.md`, `final-requirement-inventory.md`, `implementation-task-breakdown.md`, `quickstart.md`
**Tests**: Required for new model-facing contracts. Real-model E2E remains opt-in.

**Organization**: Tasks are grouped by user story to enable independent review and implementation.

**2026-05-06 Re-baseline**: The original checklist lagged behind current code. `implementation-baseline-100.md` is the authoritative 100% scope and records the few confirmation-gated items.
Unchecked rows after this re-baseline are intentionally open. They represent either a small remaining implementation gap or a protocol behavior decision that needs explicit user confirmation.
`final-requirement-inventory.md` is the final repeated-question inventory for retained, rejected, and baseline-preserving requirements.
`implementation-task-breakdown.md` is the accepted-default execution queue for the next code slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **Story**: Which user story this task belongs to
- Include exact file paths in implementation tasks

## Non-Negotiable Constraints

- Do not switch, create, or check out git branches.
- Preserve resource-first metadata discovery.
- Preserve preview-before-execute for side-effecting operations.
- Keep live-model E2E opt-in.
- Do not introduce planner, graph, vector, memory, approval-token, RBAC, or default-CI live-model systems.

## Closure Interpretation

- Optional bounded `request_id` is intentionally omitted because recoverable errors already expose `category`, `field`, `suggested_arguments`, and `next_actions`.
- Lightweight Proxy preflight is closed as a topology hint, not a new planner or live topology validator.
- Protocol-native `ResourceLink` content is additive transport content; JSON `structuredContent` remains canonical.
- MCP-native elicitation is interactive only when the client advertises form elicitation; structured JSON clarification remains the fallback.

## Phase 1: Setup and Evidence

- [x] T001 Confirm current branch is `001-shardingsphere-mcp` without switching branches.
- [x] T002 Inspect `.specify/specs/008-mcp-ai-friendly-lightweight-experience/` and confirm 009 is a follow-up, not a replacement.
- [x] T003 [P] Inspect `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java` for current capabilities payload construction.
- [x] T004 [P] Inspect current `next_actions` producers and recovery conversion under `mcp/core` and `mcp/support`.
- [x] T005 [P] Inspect `search_metadata` matching, pagination, URI derivation, and search context under `mcp/core`.
- [x] T006 [P] Inspect SQL and workflow output schemas under descriptor YAML files.
- [x] T007 Record current behavior, affected paths, verification map, non-goals, rollback boundary, and breaking cleanup matrix.

## Phase 2: Foundation Design

- [x] T008 Define ordered next-action/dependency metadata for approval-gated multi-action responses.
- [x] T009 Define compact `surface_summary` payload shape in capabilities.
- [x] T010 Define navigation source/target type hints and completion-availability hints.
- [x] T011 Define empty-state and not-found hint fields for list/detail/search responses.
- [x] T012 Define argument provenance and redaction marker vocabulary.
- [x] T013 Define safe runtime recovery categories and optional request/trace identifier constraints.
- [x] T014 Define opt-in next-action-follow and approval-violation usability metrics.

**Checkpoint**: Foundation accepted before production code changes.

## Phase 3: User Story 1 - Model follows ordered next actions (Priority: P0)

**Goal**: Multi-action responses expose order or approval dependency.
**Independent Test**: Preview and recovery tests assert dependency metadata without hidden execution.

### Tests for User Story 1

- [x] T015 [P] [US1] Add `execute_update` preview tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [x] T016 [P] [US1] Add workflow apply preview tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`.
- [x] T017 [P] [US1] Add recovery tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/`.

### Implementation for User Story 1

- [x] T018 [US1] Add order/dependency metadata to multi-action `next_actions` producers in `mcp/core` and workflow support helpers.
- [x] T019 [US1] Add target or source tool metadata to `retry_tool` recovery actions when known.
- [x] T020 [US1] Update descriptor output schemas where the new next-action fields are schema-visible.

**Checkpoint**: A model can tell when a follow-up action waits for user approval.

## Phase 4: User Story 2 - Model discovers a compact surface summary (Priority: P0)

**Goal**: Capabilities exposes a tiny first-hop summary.
**Independent Test**: Capabilities contract test asserts `surface_summary`.

### Tests for User Story 2

- [x] T021 [P] [US2] Add capabilities tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/`.
- [x] T022 [P] [US2] Add descriptor/catalog checks if summary references are validated centrally.

### Implementation for User Story 2

- [x] T023 [US2] Add `surface_summary` in `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`.
- [x] T024 [US2] Ensure summary references only current public tools and does not expose historical aliases.

**Checkpoint**: A model can identify first resources, public tools, and safety rules from a compact summary.

## Phase 5: User Story 3 - Model can traverse resources and completions with fewer joins (Priority: P0)

**Goal**: Navigation and completion metadata are easier to consume locally.
**Independent Test**: Capabilities and completion tests assert type hints and next actions for missing context.

### Tests for User Story 3

- [x] T025 [P] [US3] Add capabilities tests for `resourceNavigation` source/target type hints.
- [x] T026 [P] [US3] Add completion tests under `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/`.
- [x] T027 [P] [US3] Add tool-field or prompt/resource argument descriptor tests where completion hints are exposed.

### Implementation for User Story 3

- [x] T028 [US3] Add source and target kind hints to capability navigation payloads.
- [x] T029 [US3] Add completion availability hints to descriptor-derived tool fields or argument payloads.
- [x] T030 [US3] Add `next_actions` to completion diagnostics when missing context can be identified.

**Checkpoint**: A model can traverse navigation and completion without manually joining catalog sections.

## Phase 6: User Story 4 - Model handles empty and not-found states naturally (Priority: P1)

**Goal**: Empty and not-found responses explain what happened and the safe next step.
**Independent Test**: Resource/search tests assert empty-state fields.

### Tests for User Story 4

- [x] T031 [P] [US4] Add resource list/detail tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/`.
- [x] T032 [P] [US4] Add `search_metadata` zero-hit tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/`.

### Implementation for User Story 4

- [x] T033 [US4] Add compact empty-state hints to list and search payloads where the server can know the reason.
- [x] T034 [US4] Add not-found parent/list/search follow-up hints to detail resource payloads.
- [x] T035 [US4] Update resource payload contracts in capabilities if the new fields are model-visible stable fields.

**Checkpoint**: A model no longer treats zero results as an unexplained failure.

## Phase 7: User Story 5 - Model preserves server-owned values (Priority: P1)

**Goal**: Reusable arguments and sensitive values expose provenance and redaction semantics.
**Independent Test**: SQL/workflow payload tests assert provenance and redaction markers.

### Tests for User Story 5

- [x] T036 [P] [US5] Add SQL preview/success tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [x] T037 [P] [US5] Add workflow planning/apply tests under `mcp/features/encrypt`, `mcp/features/mask`, or shared workflow tests.
- [x] T038 [P] [US5] Add descriptor schema tests for provenance/redaction fields.

### Implementation for User Story 5

- [x] T039 [US5] Add argument provenance metadata to reusable SQL and workflow argument payloads.
- [x] T040 [US5] Add normalized SQL to success payloads when already classified and safe.
- [x] T041 [US5] Standardize redaction markers for sensitive algorithm/workflow properties.
- [x] T042 [US5] Add manual-only follow-up guidance that asks the user to confirm external execution before validation.
- [x] T043 [US5] Clarify EXPLAIN ANALYZE execution-risk semantics in database capabilities where known.

**Checkpoint**: Models copy server-owned values safely and do not reconstruct redacted data.

## Phase 8: User Story 6 - Model understands runtime health and failures (Priority: P2)

**Goal**: Runtime failures are easier to triage without leaking secrets.

- [x] T044 [P] [US6] Add safe JDBC driver/authentication/connection recovery tests where exception types can be simulated.
- [x] T045 [US6] Add specific recovery categories in `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`.
- [x] T046 [US6] Omit optional request/trace identifier after conservative review; keep recoverable errors category/action based.
- [x] T047 [US6] Add a short token-safe health-check shape to `mcp/README.md` and `mcp/README_ZH.md` if documentation changes are included.

## Phase 9: User Story 7 - Maintainers measure model comfort without making CI heavy (Priority: P2)

**Goal**: Opt-in usability reports measure next-action following and approval violations.

- [x] T048 [P] [US7] Add next-action-follow metric to `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/`.
- [x] T049 [P] [US7] Add approval-violation metric using existing unsafe SQL/workflow trace checks.
- [x] T050 [US7] Keep live-model metrics behind existing opt-in configuration and outside default CI.

## Phase 10: User Story 8 - Model stays within safe query and search bounds (Priority: P0)

**Goal**: SQL and metadata calls expose safe defaults, caps, strict argument recovery, and URI handling.
**Independent Test**: Query/search tests assert bounded defaults, invalid pagination recovery, blank-query behavior, and encoded identifiers.

### Tests for User Story 8

- [x] T055 [P] [US8] Add `execute_query` default/cap/truncation tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`.
- [x] T056 [P] [US8] Add metadata search argument-bound and invalid-token tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/`.
- [x] T057 [P] [US8] Add resource URI encoding/decoding tests under `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/`.
- [x] T058 [P] [US8] Add descriptor/schema tests for documented row, timeout, page-size, and page-token bounds.

### Implementation for User Story 8

- [x] T059 [US8] Add documented default and maximum row-limit behavior for read-only SQL responses.
- [x] T060 [US8] Add strict numeric validation and structured recovery for invalid `page_size`, `page_token`, row-limit, and timeout arguments.
- [x] T061 [US8] Align `search_metadata.query` contract with blank-query/list-within-scope behavior or add narrowing next actions for unsafe broad searches.
- [x] T062 [US8] Add percent-encoding and decoding for descriptor-backed resource URI path variables.
- [x] T063 [US8] Preserve safe execution-shaping arguments in preview `suggested_arguments`.
- [x] T064 [US8] Align preview/executed output schemas so models can identify response mode without inference.

## Phase 11: User Story 9 - Model asks clear questions and survives context loss (Priority: P1)

**Goal**: Missing-input questions, non-English intent, and current-session workflow plan recovery are structured.
**Independent Test**: Workflow tests assert field-level questions, Chinese synonym coverage, and read-back by `plan_id`.

### Tests for User Story 9

- [x] T065 [P] [US9] Add workflow clarification payload tests for field, type, allowed values, default, and secret hints.
- [x] T066 [P] [US9] Add Chinese encrypt/mask intent evidence tests in feature workflow modules.
- [x] T067 [P] [US9] Add current-session workflow status read-back tests for valid and stale `plan_id`.
- [x] T068 [P] [US9] Add completion tests for single-schema auto-resolution or schema-first next action.

### Implementation for User Story 9

- [x] T069 [US9] Replace prose-only missing-input handling with canonical structured clarification fields; keep only generated display text where useful.
- [x] T070 [US9] Add interactive MCP-native elicitation integration where the active SDK/client path supports it; keep structured JSON clarification as fallback.
- [x] T071 [US9] Add deterministic common Chinese synonym handling or required structured intent evidence guidance.
- [x] T072 [US9] Add current-session workflow plan-status resource or equivalent read-back contract.
- [x] T073 [US9] Add single-schema completion auto-resolution or explicit schema-completion next action.

## Phase 12: Runtime and Packaging Comfort Additions (Priority: P2)

- [x] T074 [P] Add runtime-status resource or capability section with secret-free database, feature, transport, and first-check information.
- [x] T075 [P] Add env-placeholder configuration support or docs for access tokens and JDBC credentials with safe unresolved-placeholder errors.
- [x] T076 [P] Add clearer HTTP bearer-token failure hints without exposing token values.
- [x] T077 [P] Align server identity metadata with machine-friendly and human-readable names where supported.
- [x] T078 [P] Add minimal STDIO and HTTP client configuration examples to `mcp/README.md` and `mcp/README_ZH.md`.
- [x] T079 [P] Evaluate lightweight workflow preflight validation and close as Proxy-topology hint only, reusing existing workflow payloads and staying out of planner scope.

## Phase 13: Concrete 2026-05-06 Gap Closure (No Over-Design)

- [x] T080 [P] Add `apply_workflow` missing/invalid `execution_mode` recovery tests that assert `target_tool=apply_workflow` and safe `execution_mode=preview` guidance.
- [x] T081 [P] Add workflow missing-property tests that assert public argument paths such as `primary_algorithm_properties`,
  `assisted_query_algorithm_properties`, and `like_query_algorithm_properties`.
- [x] T082 [P] Add `execute_update` descriptor/schema tests that distinguish preview and executed response variants or stable response-mode fields.
- [x] T083 [P] Add preview semantics tests that assert side-effect previews do not imply affected-row estimates.
- [x] T084 [P] Add SQL result-shape tests for unique-column `row_objects` and duplicate-column unavailable status while preserving positional rows.
- [x] T085 [P] Add truncation and pagination continuation tests that assert safe `next_actions`.
- [x] T086 [P] Add metadata-introspection SQL recovery tests for `SHOW TABLES`, `DESCRIBE`, and representative dialect equivalents.
- [x] T087 [P] Add metadata duplicate/ambiguous-hit tests that assert narrowing hints instead of guessed best matches.
- [x] T088 [P] Centralize URI encoding tests for search result URIs, resource navigation, and workflow `resources_to_read` values containing non-ASCII or reserved characters.
- [x] T089 [P] Add Docker/HTTP documentation checks for bind host, bearer token, and env-placeholder examples without embedded secrets.
- [x] T090 [P] Add Proxy-topology recovery or preflight tests for encrypt/mask workflows when the runtime appears connected to a physical database instead of Proxy's logical view.
- [x] T091 [P] Add `complete_argument` next-action tests that assert completion reference, context arguments, missing context, and safe resume target where known.
- [x] T092 [P] Add resource-read error payload tests that assert `response_kind=error`, `error_code`, original URI, and recovery `next_actions`.
- [x] T093 [P] Add typed resource-hint tests for workflow `resources_to_read`, metadata `next_resources`, and runtime first-check resources.
- [x] T094 [P] Add descriptor/schema tests for machine-readable default, minimum, maximum, examples, and pattern fields where descriptors already know them.
- [x] T095 [P] Add transport tests for additive MCP resource links while keeping canonical `structuredContent`.
- [x] T096 Update `complete_argument` producers to include completion reference, context arguments, and resume-target metadata without adding a helper tool.
- [x] T097 Add resource-read error payload shape for recoverable resource failures; keep messages secret-safe and avoid tool-style assumptions.
- [x] T098 Add typed resource hints and remove URI-only compatibility fields, obsolete tests, and stale descriptor/docs references unless a protocol field requires a URI string.
- [x] T099 Extend descriptor/schema metadata only for values already known by descriptors; avoid duplicating validation rules in prose and code.
- [x] T100 Add protocol-native resource links as additive transport content; keep canonical URIs in JSON `structuredContent`.
- [x] T101 Update affected descriptors, README sections, capability contracts, and payload-contract docs only for the concrete fields proven by T080-T100.
- [x] T102 Delete replaced compatibility helpers, legacy payload-shape tests, and documentation that teaches removed URI-only or prose-only contracts.
- [x] T103 Add negative shape assertions and documentation searches for removed prose-only questions, URI-only resource lists, old `complete_argument`, and prose-only bounds.

## Phase 14: Verification

- [x] T104 Run `git diff --check` for documentation-only changes.
- [x] T105 Run scoped MCP module tests after Java or descriptor changes.
- [x] T106 Run scoped Checkstyle after Java changes.
- [x] T107 Confirm no branch switch occurred with `git branch --show-current`.

## Phase 15: Accepted-Default Implementation Queue

Use `implementation-task-breakdown.md` for the next executable task queue.

- [x] T108 [P0-A] Implement centralized descriptor-backed resource URI encoding.
- [x] T109 [P0-B] Implement SQL truncation, metadata pagination, and ambiguity follow-up guidance.
- [x] T110 [P0-C] Implement exact recovery target and public argument-path completeness.
- [x] T111 [P0-D] Align workflow response-mode payloads with SQL response-mode clarity.
- [x] T112 [P0-E] Add cross-cutting next-action sequencing and negative contract checks.
- [x] T113 [P1] Implement retained response comfort tasks after P0 is stable.
- [x] T114 [P2] Keep optional diagnostics and packaging work bounded, opt-in, or documentation-focused.

## Dependencies and Execution Order

- Phase 1 blocks all implementation.
- Phase 2 blocks user-story work.
- Phase 15 is the next implementation queue after the accepted defaults.
- User Stories 1, 2, and 3 are P0 and should land before broader response polish.
- User Stories 4 and 5 can proceed after current response shapes are verified.
- User Story 8 is P0 and should land with or before response-shape changes that affect query/search safety.
- User Story 9 is P1 and can proceed after workflow payload baselines are verified.
- User Stories 6, 7, and runtime packaging additions are optional P2 and must not block P0/P1.

## Parallel Opportunities

- T003 through T006 can run in parallel.
- T015 through T017 can run in parallel.
- T021 and T022 can run in parallel.
- T025 through T027 can run in parallel.
- T031 and T032 can run in parallel.
- T036 through T038 can run in parallel.
- T044 and T048/T049 can proceed independently after P0 design is stable.
- T055 through T058 can run in parallel.
- T065 through T068 can run in parallel.
- T074 through T079 can run in parallel after runtime/config baselines are confirmed.
- T080 through T095 can run in parallel because each validates a distinct concrete model-guessing gap.
