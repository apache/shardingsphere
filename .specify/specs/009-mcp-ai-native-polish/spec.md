# Feature Specification: MCP AI-Native Polish

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-05-05
**Reviewed**: 2026-05-06
**Status**: Draft
**Input**: User request:
"Use Spec Kit to organize the remaining ShardingSphere MCP improvements that make the MCP native, convenient,
comfortable, and clear for large models. Do not switch branches."

## Process Constraints

- This Speckit package MUST stay on the current branch. Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Spec Kit commands.
- Treat `.specify/specs/008-mcp-ai-friendly-lightweight-experience/`, `specs/003-mcp-ai-friendly-guided-interaction/`, and the current MCP code as the baseline.
- This package captures the next small polish increment only. It MUST NOT reopen completed baseline work unless current-code evidence proves drift.
- Do not introduce a planner, graph traversal engine, vector search, cross-session memory, approval-token platform, RBAC platform, or default-CI live-model benchmark.

## Current Baseline

Current ShardingSphere MCP already exposes:

- `shardingsphere://capabilities` as the model-facing public-surface source of truth.
- Capability-level `model_contract`, `next_action_contract`, `common_flows`, `security_hints`, payload contracts, protocol availability, and fingerprints.
- Descriptor-backed tools, resources, resource templates, prompts, completions, annotations, examples, and output schemas.
- `search_metadata` with `resource_uri`, `parent_resource_uri`, `next_resource_uris`, URI derivation status, search context, `match_kind`, `matched_fields`, and `matched_value`.
- SQL response parse hints such as `returned_row_count`, `applied_max_rows`, `applied_timeout_ms`, `truncated`, and `next_actions`.
- Preview-first `execute_update` and `apply_workflow` with `approval_summary`, `approval_question`, side-effect scope, reusable arguments, and user approval requirements.
- Structured recovery for missing arguments, invalid execution mode, wrong SQL tool, invalid object types, invalid page tokens, stale workflow plans, unsupported tools/resources, and unsafe SQL.
- Resource navigation payloads such as `self_uri`, `parent_uri`, `next_resources`, count, pagination wording, and payload contracts.
- Completion diagnostics including missing context, candidate counts, deterministic ranking policy, prefix-first matching, contains fallback, and current-session plan ordering.
- Opt-in LLM usability scenarios and deterministic contract tests for the existing model-facing surface.

The remaining work is not a rebuild. It is a compact backlog for places where a model still has to infer ordering, provenance, bounds, health, localization, or recovery details from nearby context.

The 2026-05-06 sweep adds only concrete, low-design polish gaps found by repeatedly asking whether a large model can use the MCP natively, conveniently, comfortably, and clearly:

- exact recovery targets for workflow calls, especially `apply_workflow.execution_mode`;
- exact missing-field paths for workflow algorithm properties rather than synthetic names;
- output-schema alignment for preview and executed response variants;
- explicit preview-limit wording so models do not treat classification previews as affected-row estimates;
- model-friendly result-row shapes where column names are unambiguous;
- safe continuation hints for truncation, pagination, duplicate search hits, and metadata-introspection SQL;
- centralized percent-encoding behavior for resource URIs already covered by descriptor-backed patterns;
- secret-safe runtime and HTTP configuration comfort that remains documentation/config focused.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Model follows ordered next actions (Priority: P0)

As a model following `next_actions`, I want action order and approval dependencies to be explicit so that I do not call the second action before asking the user or completing a prerequisite.

**Why this priority**: Current actions identify kind, target, arguments, reason, and approval requirement.
The remaining ambiguity is sequence: some response payloads imply "ask user, then call tool", but the dependency is not machine-obvious.

**Independent Test**: Preview side-effecting SQL, workflow apply preview, and recovery payloads.
Assert multi-action responses expose deterministic order or dependency metadata without adding a planner.

**Acceptance Scenarios**:

1. **Given** `execute_update` preview returns `ask_user` and `call_tool`, **When** the model reads the actions, **Then** it can tell the call-tool action waits for explicit user approval.
2. **Given** workflow apply preview returns an approval-gated follow-up, **When** the model prepares the next call, **Then** it reuses server arguments only after the approval action is satisfied.
3. **Given** a recovery response uses `retry_tool`, **When** the model context is compacted, **Then** the response still identifies the target or source tool clearly enough to retry safely.

---

### User Story 2 - Model discovers a compact surface summary (Priority: P0)

As a model entering the MCP surface, I want a tiny `surface_summary` that says what to do first, what public tools exist, and what not to do.
That lets me avoid parsing the full capability catalog for the first hop.

**Why this priority**: Capabilities is rich and accurate. A compact summary improves first-use comfort without adding new tools or duplicating the README.

**Independent Test**: Read `shardingsphere://capabilities` and assert the summary contains first resource, public tools, main flows, safety rule, and legacy/non-goal warnings.

**Acceptance Scenarios**:

1. **Given** a model reads capabilities, **When** it needs the first action, **Then** it sees `shardingsphere://capabilities` and `shardingsphere://databases` as the safe entry path.
2. **Given** a model needs to choose a tool, **When** it reads the summary, **Then** it sees the current public tool names without historical `list_*` or `describe_*` aliases.
3. **Given** a model is about to execute side effects, **When** it reads the summary, **Then** it sees preview-before-approval as the non-bypassable rule.

---

### User Story 3 - Model can traverse resources and completions with fewer joins (Priority: P0)

As a model using resource navigation and argument completion, I want type and completion hints close to the relevant descriptor fields so that I do not have to join multiple catalog sections manually.

**Why this priority**: `resourceNavigation` and `completionTargets` are already present, but the model still has to infer whether a target is a tool, resource, prompt, or completable argument.

**Independent Test**: Read capabilities and assert navigation entries expose source/target kind, and tool fields or parameter metadata identify completion availability where known.

**Acceptance Scenarios**:

1. **Given** `resourceNavigation` points from an algorithm resource to a planning tool, **When** the model reads it,
   **Then** it can distinguish resource-to-tool navigation from resource-to-resource navigation.
2. **Given** a tool field supports completion, **When** the model reads the field descriptor, **Then** it can see that completion is available without scanning all completion targets.
3. **Given** completion has missing context, **When** completion returns diagnostics, **Then** the model receives a next action to read or complete the missing context first.

---

### User Story 4 - Model handles empty and not-found states naturally (Priority: P1)

As a model reading resource, search, or detail responses, I want empty states and not-found states to say why they are empty and what safe next step exists.
That keeps me from treating a zero count as a failure or inventing an object.

**Why this priority**: Count and pagination are now visible. The remaining gap is diagnostic intent: empty because no object exists, unsupported capability, too narrow a filter, or missing context.

**Independent Test**: Read empty list, missing detail, zero-hit search, and unsupported metadata cases; assert each response or recovery gives a bounded reason and safe next action.

**Acceptance Scenarios**:

1. **Given** a resource list is empty, **When** the model reads it, **Then** it sees whether the empty state is valid and whether another resource should be read.
2. **Given** a detail resource returns `found=false`, **When** the model reads it, **Then** it sees the parent URI and a safe search or list suggestion.
3. **Given** `search_metadata` returns zero hits, **When** the model reads the result, **Then** it can distinguish no match from invalid scope or unsupported object type.

---

### User Story 5 - Model preserves server-owned values (Priority: P1)

As a model carrying values between calls, I want the server to mark where important arguments came from.
That keeps me from rewriting normalized SQL, generated workflow identifiers, redacted secrets, or server-derived values.

**Why this priority**: Reusable arguments already exist. Provenance helps the model know what can be copied exactly, what came from the user, and what must not be guessed.

**Independent Test**: Preview SQL and workflow apply.
Assert reusable argument payloads expose a lightweight provenance map or equivalent field for server-normalized, user-provided, generated, and redacted values.

**Acceptance Scenarios**:

1. **Given** `execute_update` preview returns normalized SQL, **When** the model prepares execute mode, **Then** it knows the SQL is server-normalized and should be reused exactly.
2. **Given** a workflow response contains `plan_id`, **When** the model applies or validates, **Then** it knows the `plan_id` is server-generated and current-session scoped.
3. **Given** algorithm properties are redacted, **When** the model summarizes the plan, **Then** it knows the redacted values must not be reconstructed or requested unless missing.

---

### User Story 6 - Model understands runtime health and failures (Priority: P2)

As a model helping a user connect or recover from runtime failures, I want compact health and diagnostic hints that do not expose secrets.
That makes startup, JDBC, and failed-call triage less guessy.

**Why this priority**: First-use docs and startup hints exist. Runtime failure triage can still be more precise without adding an observability platform.

**Independent Test**: Trigger representative configuration, JDBC driver, connection, and authentication failures in scoped tests where possible.
Assert recoveries carry safe category, suggested local fix, and optional trace/request identifier.

**Acceptance Scenarios**:

1. **Given** a JDBC driver is missing, **When** the model reads the error, **Then** it sees a driver/plugin-path recovery rather than a generic unavailable message.
2. **Given** a request fails, **When** logs are needed, **Then** the payload or startup hint provides a bounded request or trace identifier without exposing credentials.
3. **Given** the user wants a health check, **When** the model reads documentation or startup hints, **Then** it can perform one token-safe capabilities read or initialization check.

---

### User Story 7 - Maintainers measure model comfort without making CI heavy (Priority: P2)

As a maintainer, I want lightweight usability metrics that show whether models follow next actions and approval gates so that polish regressions are visible without broad live-model matrices.

**Why this priority**: The opt-in LLM suite already exists. Two focused metrics provide better signal than adding many scenarios.

**Independent Test**: Run the opt-in LLM usability suite locally or in its existing scheduled lane and inspect next-action-follow and approval-violation metrics.

**Acceptance Scenarios**:

1. **Given** a model receives server `next_actions`, **When** the usability report is produced, **Then** it includes whether the trace followed the suggested next action.
2. **Given** a side-effect preview is required, **When** the model attempts execution, **Then** approval violations are counted separately from generic invalid calls.
3. **Given** the default Maven test lane runs, **When** live-model credentials are absent, **Then** these metrics do not run or fail default CI.

---

### User Story 8 - Model stays within safe query and search bounds (Priority: P0)

As a model issuing SQL and metadata search calls, I want explicit row, pagination, and argument bounds so that broad natural-language requests do not accidentally create unbounded work.

**Why this priority**: LLMs often omit limits, repeat broad searches, or pass malformed pagination values.
The MCP should make the safe default obvious and reject unsafe shapes with structured recovery.

**Independent Test**: Execute read-only SQL without `max_rows`, run broad metadata search, pass invalid `page_size` and `page_token`, and assert deterministic defaults, caps, and recovery hints.

**Acceptance Scenarios**:

1. **Given** a model calls `execute_query` without `max_rows`, **When** the query runs, **Then** the response uses a documented default row limit and exposes whether results are truncated.
2. **Given** a model passes a negative or non-numeric `page_token`, **When** `search_metadata` validates arguments, **Then** it returns structured recovery rather than a generic runtime failure.
3. **Given** a model asks to list all metadata with a blank query, **When** the request includes a safe scope,
   **Then** the server treats blank query as list/search-all within that scope or documents why it must narrow the request.
4. **Given** a metadata object name contains spaces, Chinese characters, or reserved URI characters,
   **When** a resource URI is returned or read, **Then** URI encoding and decoding preserve the identifier without guessing.

---

### User Story 9 - Model asks clear questions and survives context loss (Priority: P1)

As a model collecting missing workflow or execution inputs, I want structured clarification fields, language-neutral intent hints,
and read-back resources so that I can ask the user naturally and resume safely.

**Why this priority**: Current fallback questions are usable, but models still translate prose, infer fields,
and lose plan context after compaction. A small structured layer improves native use without adding memory.

**Independent Test**: Trigger missing workflow inputs, use Chinese natural-language evidence, compact/recover by `plan_id`,
and assert field-level questions, localized intent hints, and current-session read-back behavior.

**Acceptance Scenarios**:

1. **Given** workflow planning lacks a required field, **When** the server asks for clarification,
   **Then** the response includes field name, type, allowed values, default, and secret flag where known.
2. **Given** the user describes encryption or masking intent in Chinese,
   **When** the model supplies structured evidence or the server reads common synonyms,
   **Then** the workflow does not rely on English-only keyword guessing.
3. **Given** the model has a `plan_id` but lost surrounding context, **When** it reads the plan status resource,
   **Then** it can recover status, artifacts, safe next actions, and current-session scope.

## Edge Cases

- A response contains multiple next actions where only some are ordered.
- A next action has `requires_user_approval=true` but no user-facing approval summary.
- A resource navigation target is a prompt or tool rather than a resource.
- A completion request is missing database/schema/table context.
- A list response is empty because the feature is supported but no objects exist.
- A detail response is not found but the parent resource is readable.
- Normalized SQL differs from user SQL.
- Workflow `manual-only` returns artifacts that must be executed outside MCP before validation.
- A redacted secret is present in plan/apply output and must not be reconstructed.
- EXPLAIN ANALYZE may execute the query on some engines and should be described conservatively.
- A runtime error includes a SQL state or driver message that must not leak credentials.
- A new usability metric is useful but would require live-model default CI; it must remain opt-in.
- A read-only SQL request omits row limits and would otherwise return a large result.
- A metadata search uses an empty query across all databases and all object types.
- `page_size` is zero, negative, non-numeric, or far above the documented maximum.
- `page_token` is negative, stale, or not an integer.
- A database, schema, table, or column name requires URI percent-encoding.
- The user asks in Chinese for reversible encryption, irreversible hashing, phone masking, or identity-card masking.
- A model has only `plan_id` after context compaction and must recover without cross-session memory.
- A configuration example must mention secrets without embedding real credentials.

## Requirements *(mandatory)*

### Functional Requirements

#### P0 - immediate model-continuation polish

- **FR-001**: Multi-step `next_actions` SHOULD expose ordering or dependency metadata when an action must wait for user approval or another action.
- **FR-002**: `retry_tool` actions SHOULD include the target tool or source tool when the server can know it safely.
- **FR-003**: Ordering metadata MUST NOT become a planner or authorize hidden execution.
- **FR-004**: Capabilities SHOULD expose a compact `surface_summary` with first safe resources, current public tools, main flow names, and side-effect safety rule.
- **FR-005**: `surface_summary` MUST NOT duplicate long README tutorials or mention historical tool aliases as usable.
- **FR-006**: Resource navigation SHOULD expose source and target kinds where they can be inferred statically.
- **FR-007**: Tool fields and prompt/resource arguments SHOULD identify completion availability where descriptor completion targets already support them.
- **FR-008**: Completion diagnostics SHOULD provide a safe next action for missing context when the server can identify it.
- **FR-009**: Read-only SQL execution SHOULD apply a documented default row limit when the model omits `max_rows`.
- **FR-010**: Read-only SQL execution SHOULD expose a documented maximum row cap and structured truncation metadata.
- **FR-011**: Metadata search and pagination arguments SHOULD define minimum, maximum, and default values in schema-visible contracts.
- **FR-012**: Invalid numeric arguments and invalid page tokens SHOULD return structured recovery instead of silently defaulting or surfacing generic runtime errors.
- **FR-013**: `search_metadata.query` SHOULD support a documented blank-query/list-within-scope mode or require a narrowing next action before broad all-database scans.
- **FR-014**: Side-effect execute responses SHOULD preserve approval provenance, such as the preview source, approval source, or equivalent audit-light context, without adding durable approval tokens.
- **FR-015**: Preview suggested arguments SHOULD preserve all safe execution-shaping arguments already supplied by the model, including row and timeout limits where relevant.
- **FR-016**: Tool output schemas SHOULD distinguish preview and executed response variants or expose stable common fields
  that let a model identify the response mode, including `execute_update.execution_mode` behavior.
- **FR-017**: Side-effect previews SHOULD clearly state that preview is a classification and side-effect-scope summary, not a database affected-row estimate.
- **FR-018**: SQL result payloads SHOULD expose model-friendly row objects when returned column names are unique, and a clear fallback status when duplicate or unnamed columns require positional rows.
- **FR-019**: SQL and resource responses SHOULD expose safe continuation `next_actions` when results are truncated, paginated, or require a narrower retry.
- **FR-020**: Wrong-tool recovery for metadata-introspection SQL such as `SHOW TABLES`, `DESCRIBE`, and dialect equivalents
  SHOULD guide models to metadata resources or `search_metadata` when that is safer than SQL execution.
- **FR-021**: `apply_workflow` missing or invalid `execution_mode` recovery SHOULD name `apply_workflow` as the retry target
  and suggest `execution_mode=preview` unless the user already approved execution.
- **FR-022**: Workflow missing-input recovery SHOULD reference exact public input names such as `primary_algorithm_properties`,
  `assisted_query_algorithm_properties`, and `like_query_algorithm_properties` instead of synthetic or internal property paths.
- **FR-023**: Metadata search responses SHOULD expose ambiguity or duplicate-hit hints when multiple matches share a name across databases, schemas, or object types, with safe narrowing suggestions.

#### P1 - response comfort and value provenance

- **FR-101**: Empty list, zero-hit search, and not-found detail responses SHOULD expose a compact empty-state or not-found reason.
- **FR-102**: Not-found detail responses SHOULD include safe parent/list/search follow-up hints when known.
- **FR-103**: Reusable argument payloads SHOULD expose argument provenance such as user-provided, server-normalized, server-generated, server-defaulted, or redacted.
- **FR-104**: SQL success responses SHOULD expose normalized SQL when already produced by statement classification and safe to return.
- **FR-105**: Workflow status guidance SHOULD document manual-only follow-up: ask the user to confirm external execution before validation.
- **FR-106**: Algorithm and workflow outputs SHOULD standardize redaction markers for sensitive fields.
- **FR-107**: Database capabilities SHOULD conservatively explain EXPLAIN ANALYZE execution-risk semantics where known.
- **FR-108**: Unsupported tool/resource recovery MAY include deterministic prefix/template candidates, but MUST NOT use semantic search.
- **FR-109**: Clarification responses SHOULD expose structured field-level questions with field name, input type, allowed values, default, and secret sensitivity where known.
- **FR-110**: The server SHOULD use MCP-native elicitation when the active SDK/client path supports it, while preserving the current fallback fields for clients without elicitation.
- **FR-111**: Natural-language workflow intent handling SHOULD support common Chinese synonyms for encrypt, mask, reversible,
  irreversible, equality query, fuzzy query, phone, identity card, and email.
- **FR-112**: Descriptors and prompts SHOULD tell models to provide structured intent evidence for non-English user requests instead of depending on prose keyword matching.
- **FR-113**: Current-session workflow plans SHOULD be readable through a plan-status resource or equivalent read-back contract by `plan_id`.
- **FR-114**: Resource URI generation and matching SHOULD support percent-encoding and decoding for non-ASCII and reserved identifier characters.
- **FR-115**: Completion SHOULD auto-resolve single-schema context where safe or return a next action that asks the model to complete/read schema first.
- **FR-116**: Runtime status SHOULD be available through a compact, secret-free resource or capability section
  that lists configured logical databases, feature availability, transport, and safe first checks.

#### P2 - diagnostics and opt-in quality signal

- **FR-201**: JDBC driver, authentication, connection timeout, and database-unavailable errors SHOULD return more specific safe recovery categories where the exception type permits.
- **FR-202**: Error payloads MAY include a bounded request or trace identifier if it can be generated without exposing secrets or creating persistent state.
- **FR-203**: Startup or README docs SHOULD include one short token-safe health-check shape.
- **FR-204**: Opt-in LLM usability reports SHOULD add next-action-follow and approval-violation metrics.
- **FR-205**: New usability metrics MUST remain outside default CI and MUST NOT require live-model credentials in normal module tests.
- **FR-206**: Runtime configuration SHOULD support environment-variable placeholders for access tokens and JDBC credentials, with safe errors for unresolved placeholders.
- **FR-207**: Documentation SHOULD provide minimal client configuration examples for STDIO and HTTP without leaking secrets or branch-specific release commitments.
- **FR-208**: HTTP authentication failures SHOULD return a clearer bearer-token hint without exposing configured token values.
- **FR-209**: Server identity metadata SHOULD use a machine-friendly server name and keep the human-readable name in display fields where the protocol path allows it.
- **FR-210**: Workflow apply MAY expose a lightweight preflight validation step only when it reuses existing validation paths and does not become a separate planner.

### Non-Goals

- Do not add `list_*` or `describe_*` compatibility tools.
- Do not add a planner, graph traversal, vector search, model-call ranking, cross-session memory, or user behavior learning.
- Do not add preview tokens, approval tokens, durable approval records, RBAC, or tenant isolation in this increment.
- Do not make real-model E2E part of default CI.
- Do not change encrypt or mask DistSQL semantics, generated naming, data migration, or workflow execution behavior unless a later implementation task proves it is required.

### Key Entities

- **Ordered Next Action**: A next-action entry with optional order or dependency metadata.
- **Surface Summary**: A compact capability-level first-hop summary.
- **Navigation Type Hint**: Metadata that distinguishes tool, resource, prompt, and completion targets.
- **Empty State Hint**: A compact reason and safe follow-up for zero-result responses.
- **Argument Provenance**: Metadata describing whether a reusable argument came from the user, server normalization, server generation, defaulting, or redaction.
- **Runtime Recovery Hint**: Safe error metadata for startup, JDBC, connection, and authentication failures.
- **Usability Follow Metric**: Opt-in measurement of whether model traces follow next actions and approval gates.
- **Input Bound Contract**: Schema-visible defaults, minimums, maximums, and recovery rules for row limits, page sizes, page tokens, and timeouts.
- **Clarification Question**: Field-level missing-input request with type, choices, default, sensitivity, and localization-friendly key.
- **Workflow Status Snapshot**: Current-session read-back view for a workflow plan, including status, artifacts, next actions, and scope.
- **Encoded Resource Identifier**: Percent-encoded resource path segment that preserves logical database, schema, table, and column identifiers.
- **Runtime Status Summary**: Secret-free operational summary for configured databases, feature loading, transport, and first health checks.
- **Response Mode Contract**: Stable payload markers that tell a model whether a response is preview, executed, manual-only, validation, recovery, or terminal.
- **Result Row View**: Optional object-row representation for SQL results when column labels are unique, plus fallback metadata when positional rows must be preserved.
- **Recovery Target Hint**: Exact tool/resource/argument target metadata for retry and repair actions, especially after context compaction.
- **Ambiguity Hint**: Search or lookup metadata that tells a model a result set needs scope narrowing instead of name guessing.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A model can determine whether a follow-up action is blocked on user approval without relying on prose order.
- **SC-002**: A model can choose the first MCP call and identify the public tool set from a compact capabilities summary.
- **SC-003**: A model can use navigation and completion hints without manually joining multiple catalog sections.
- **SC-004**: Empty, not-found, and zero-hit responses tell the model what happened and what safe next step exists.
- **SC-005**: Reusable arguments preserve server-owned values and redacted fields without model reconstruction.
- **SC-006**: Runtime failures and usability reports produce better diagnostics without adding default live-model CI or heavy systems.
- **SC-007**: Broad SQL and metadata requests have documented safe defaults, caps, and structured recovery for invalid inputs.
- **SC-008**: A model can ask missing-input questions from structured fields and can recover a current-session workflow plan after context loss.
- **SC-009**: Non-ASCII identifiers, Chinese workflow intent, and secret-bearing runtime configuration are handled without guessing or leaking sensitive data.
- **SC-010**: A model can recover from missing workflow arguments, missing execution mode, and metadata-introspection SQL without guessing the retry tool or public argument name.
- **SC-011**: A model can tell preview, execute, manual-only, validation, recovery, truncation, and pagination states from structured fields rather than prose alone.

## Assumptions

- The active branch remains `001-shardingsphere-mcp`.
- Current MCP baseline from 008 and the current code remains valid and descriptor-backed.
- This package is a requirements backlog, not an instruction to implement everything in one change.
- Future implementation will use existing descriptor, capabilities, response, completion, and recovery mechanisms before adding new abstractions.
