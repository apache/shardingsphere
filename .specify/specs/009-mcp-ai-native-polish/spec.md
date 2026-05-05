# Feature Specification: MCP AI-Native Polish

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-05-05
**Status**: Draft
**Input**: User description:
"Use Spec Kit to organize the remaining ShardingSphere MCP improvements that make the MCP native, convenient,
comfortable, and clear for large models. Do not switch branches."

## Process Constraints

- This Speckit package MUST be prepared on the current branch only.
- Do not run `git switch`, `git checkout`, branch creation scripts, or other branch-changing commands.
- Treat `.specify/specs/008-mcp-ai-friendly-lightweight-experience/` and `specs/003-mcp-ai-friendly-guided-interaction/` as the completed baseline.
- This package captures the next small polish increment only. It MUST NOT reopen the completed P0/P1/P2 baseline unless implementation evidence proves drift.
- Do not introduce a planner, graph traversal engine, vector search, cross-session memory, approval-token platform, RBAC platform, or default-CI live-model benchmark.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Model understands the next-action contract (Priority: P0)

As a model using ShardingSphere MCP, I want `shardingsphere://capabilities` to explain the reusable `next_actions` contract so that I can follow responses without learning each tool's custom prose.

**Why this priority**: The current surface already returns `next_actions`, but the action vocabulary is spread across descriptors, response payloads, and recovery paths.
A compact contract in capabilities reduces guessing without adding a planner.

**Independent Test**: Read `shardingsphere://capabilities` and assert a `next_action_contract` or equivalent section documents each action kind and required fields.

**Acceptance Scenarios**:

1. **Given** a model reads capabilities, **When** it sees `call_tool`, **Then** it knows `target_tool`, `required_arguments`, `reason`, and `requires_user_approval` are the key fields.
2. **Given** a model reads capabilities, **When** it sees `read_resource`, **Then** it knows `target_resource`, `reason`, and `requires_user_approval` are the key fields.
3. **Given** a model reads capabilities, **When** it sees `ask_user`, `retry_tool`, `complete_argument`, or `stop`, **Then** it can identify the required fields without inspecting every tool schema.

---

### User Story 2 - Model follows compact common flows (Priority: P0)

As a model or client integrator, I want capabilities to include short common task flows so that I can start with metadata, SQL, and workflow operations without reading the full README.

**Why this priority**: README is useful for humans, but models often begin from protocol-visible resources.
Short static flows give the model a reliable first hop while preserving resource-first discovery.

**Independent Test**: Read capabilities and assert compact flow recipes exist for metadata inspection, read-only SQL, side-effecting SQL preview, encrypt workflow, and mask workflow.

**Acceptance Scenarios**:

1. **Given** a model needs metadata, **When** it reads common flows, **Then** it sees `shardingsphere://capabilities` -> `shardingsphere://databases` or `search_metadata` -> detail resource.
2. **Given** a model needs read-only SQL, **When** it reads common flows, **Then** it sees `execute_query` only for one `SELECT` or `EXPLAIN ANALYZE`.
3. **Given** a model needs side-effecting SQL, **When** it reads common flows, **Then** it sees `execute_update` with `execution_mode=preview` before user approval and execution.
4. **Given** a model needs encrypt or mask workflow, **When** it reads common flows, **Then** it sees plan -> apply preview -> approved apply or manual artifact -> validate.

---

### User Story 3 - Model chooses metadata results more safely (Priority: P0)

As a model searching logical metadata, I want search responses to explain why each item matched and what search scope was applied so that I do not pick the wrong object from a large result set.

**Why this priority**: Direct `resource_uri` hints already reduce URI construction errors. The remaining ambiguity is result ranking and search context.

**Independent Test**: Call `search_metadata` with exact, prefix, and contains-style queries and assert returned payloads expose match reason and applied search scope.

**Acceptance Scenarios**:

1. **Given** `search_metadata` returns multiple hits, **When** the model reads each item, **Then** it can see `match_kind` and `matched_fields` or equivalent metadata.
2. **Given** a paged search returns results, **When** the model continues pagination, **Then** it can see the applied query, object type filter, page size, and page token context.
3. **Given** an object name cannot be safely encoded in a resource URI, **When** search returns the hit, **Then** it omits guessed URI values and explains `not_safe_to_derive`.

---

### User Story 4 - Model parses tool outputs with less inference (Priority: P1)

As a model reading SQL and workflow outputs, I want small count, status, enum, and item-shape fields to be explicit so that I do not infer them from arrays or prose.

**Why this priority**: The existing output schemas are much clearer than before, but a few high-frequency outputs still force models to infer counts, status vocabularies, or nested shapes.

**Independent Test**: Verify descriptor schemas and representative payloads for SQL and workflow tools expose explicit row counts, applied limits, status enums, and documented nested item shapes.

**Acceptance Scenarios**:

1. **Given** `execute_query` returns rows, **When** the model reads the payload, **Then** it sees `returned_row_count`, applied `max_rows`, and applied timeout information when available.
2. **Given** workflow tools return statuses, **When** the model reads output schema, **Then** common status values are documented as enum-like values or examples.
3. **Given** workflow tools return nested arrays or packages, **When** the model reads output schema,
   **Then** `issues`, `step_results`, `preview_artifacts`, `manual_artifact_package`, and `mismatches` have documented item shapes.

---

### User Story 5 - Model asks for approval with server-owned wording (Priority: P1)

As a model previewing side effects, I want the server to provide a concise approval summary or approval question so that I can ask the user without under-describing risk.

**Why this priority**: The model can currently use `approval_guidance`, side-effect scope, and suggested arguments. A short approval summary reduces unsafe paraphrasing while avoiding approval tokens.

**Independent Test**: Preview side-effecting SQL and workflow apply, then assert the response provides a user-facing approval summary or question built from server-known values.

**Acceptance Scenarios**:

1. **Given** `execute_update` preview returns side-effect scope, **When** the model prepares the user prompt, **Then** it can reuse a server-provided approval summary.
2. **Given** workflow apply preview returns artifacts, **When** the model asks for approval,
   **Then** it can reuse a server-provided summary that distinguishes rule metadata, physical structure, and physical data when known.

---

### User Story 6 - Maintainers protect the polish increment (Priority: P1)

As a maintainer, I want focused deterministic tests for the polish fields so that new model-facing contracts do not silently drift.

**Why this priority**: These improvements are small but model-visible. They should be protected by shape tests rather than broad golden transcripts.

**Independent Test**: Run module-scoped descriptor/capabilities/search/schema tests without live-model credentials.

**Acceptance Scenarios**:

1. **Given** capabilities drops the next-action contract, **When** the contract test runs, **Then** it fails with the missing section.
2. **Given** a common flow references an unknown tool or resource, **When** descriptor or capabilities validation runs, **Then** it fails.
3. **Given** a new workflow status appears in payloads but not schema/examples, **When** focused schema tests run, **Then** they identify the drift.

## Edge Cases

- A model receives a response with an unknown `action_kind`.
- A common flow references a feature module that is not present in the runtime.
- A `search_metadata` hit name contains `/`, whitespace, `?`, `#`, or other characters that require URI encoding.
- A user-provided `page_size`, `max_rows`, or `timeout_ms` is the wrong type or out of expected range.
- A SQL query returns zero rows, making `returned_row_count` useful but not evidence of failure.
- A workflow reaches an existing status that is valid in code but absent from descriptor examples.
- A manual artifact package contains several artifact kinds, but only some require user action outside MCP.
- A capability flow is useful but too long; it should move to README rather than capabilities.

## Requirements *(mandatory)*

### Functional Requirements

#### P0 - next increment entry points

- **FR-001**: Capabilities SHOULD expose a compact `next_action_contract` or equivalent model-facing section.
- **FR-002**: The next-action contract SHOULD document `read_resource`, `call_tool`, `ask_user`, `retry_tool`, `complete_argument`, and `stop`.
- **FR-003**: The next-action contract MUST NOT imply a planner, hidden tool execution, or automatic user approval.
- **FR-004**: Capabilities SHOULD expose compact `common_flows` for metadata inspection, read-only SQL, side-effecting SQL preview, encrypt workflow, and mask workflow.
- **FR-005**: Common flows MUST reference only descriptor-backed tools and resources.
- **FR-006**: Common flows MUST stay short and static; long tutorials belong in README, not capabilities.
- **FR-007**: `search_metadata` responses SHOULD expose applied search context such as query, object type filter, page size, and page token.
- **FR-008**: `search_metadata` hits SHOULD expose `match_kind` and `matched_fields` or equivalent fields when the server can compute them cheaply.
- **FR-009**: URI derivation MUST avoid guessed URI values for unsafe names; unsafe names SHOULD return `not_safe_to_derive` with a reason.
- **FR-010**: Numeric argument handling for high-frequency fields SHOULD avoid silent confusing defaults by exposing applied defaults or structured recovery.

#### P1 - output and approval clarity

- **FR-101**: `execute_query` SHOULD expose explicit result metadata such as returned row count and applied row or timeout limits when available.
- **FR-102**: Workflow output schemas SHOULD document common status values with enum-like values or compact examples.
- **FR-103**: Workflow output schemas SHOULD document item shapes for high-use nested arrays and packages.
- **FR-104**: Planning responses SHOULD expose a normalized or reusable request summary for server-resolved database, schema, table, column, algorithm, and plan identifiers when safe.
- **FR-105**: `execute_update` preview SHOULD expose a short server-owned approval summary or approval question.
- **FR-106**: `apply_workflow` preview SHOULD expose a short server-owned approval summary or approval question.
- **FR-107**: Algorithm resources SHOULD include minimal property examples where current templates already know safe placeholder values.
- **FR-108**: Recovery for invalid enum, invalid object type, invalid page token, and missing `plan_id` SHOULD include `next_actions` using the same contract as successful responses.

#### P2 - optional comfort checks

- **FR-201**: Add an opt-in LLM usability scenario where the model receives only a user intent and must discover the first hop from capabilities.
- **FR-202**: Add parity checks between `tools/list`, descriptor YAML, and `shardingsphere://capabilities.tools` for model-visible schemas.
- **FR-203**: Add safe SQL/JDBC diagnostic fields such as SQL state class only when they do not expose sensitive environment details.
- **FR-204**: Startup documentation MAY include a one-request health-check shape if it remains short and token-safe.

### Non-Goals

- Do not add `list_*` or `describe_*` compatibility tools.
- Do not add planner, graph traversal, vector search, model-call ranking, cross-session memory, or user behavior learning.
- Do not add preview tokens, approval tokens, durable approval records, RBAC, or tenant isolation in this increment.
- Do not make real-model E2E part of default CI.
- Do not change encrypt or mask DistSQL semantics, generated naming, or workflow execution behavior unless a later implementation task proves it is required.

### Key Entities

- **Next Action Contract**: Capability-level documentation of each supported next-action kind and its required fields.
- **Common Flow**: A short static recipe that maps a task intent to the first few descriptor-backed MCP resources or tools.
- **Search Context**: The query, object type filter, pagination values, and scope actually applied by `search_metadata`.
- **Match Explanation**: Per-hit metadata that describes why a search result matched.
- **Output Parse Hint**: Extra count, status, enum, item-shape, or applied-limit metadata that prevents model inference.
- **Approval Summary**: A server-owned short text or structured field that helps the model ask the user to approve side effects accurately.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A model can read capabilities and identify required fields for each supported `next_actions` kind without consulting README.
- **SC-002**: A model can choose the first MCP call for metadata, read-only SQL, side-effecting SQL, encrypt workflow, and mask workflow from capabilities alone.
- **SC-003**: `search_metadata` responses explain applied search scope and why each hit matched, without returning unsafe guessed URIs.
- **SC-004**: SQL and workflow outputs expose enough count, status, and item-shape metadata for a model to parse them without project-specific inference.
- **SC-005**: Side-effect previews provide a short approval summary that preserves user approval boundaries.
- **SC-006**: All new model-facing polish fields are covered by deterministic tests and require no live-model credentials.

## Assumptions

- The active branch remains `001-shardingsphere-mcp`.
- Current MCP baseline from 008 remains valid and descriptor-backed.
- This package is a requirements backlog, not an instruction to implement everything in one change.
- Future implementation will use existing descriptor, capabilities, response, and recovery mechanisms before adding new abstractions.
