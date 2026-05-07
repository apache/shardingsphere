# Final Requirement Inventory: MCP AI-Native Polish

**Date**: 2026-05-06
**Branch observed**: `001-shardingsphere-mcp`
**Branch rule**: Current branch only. Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Spec Kit commands.

## Purpose

This document is the final repeated-question pass for 009.
It asks whether `shardingsphere-mcp` can be used by large models in a native, convenient, comfortable, and clear way without over-design.

The answer is intentionally small:

- Keep current descriptor-backed MCP resources, tools, prompts, completions, and structured payloads as the baseline.
- Retain only gaps where a model still has to guess a field, order, retry target, continuation path, ambiguity reason, runtime state, or safety boundary.
- Reject system-sized ideas such as planners, graph traversal, semantic/vector search, cross-session memory, approval-token platforms, RBAC, observability platforms, and default-CI live-model suites.

## Current Baseline To Preserve

- `shardingsphere://capabilities` remains the public surface source of truth.
- Resource-first metadata discovery remains the default path before SQL-style exploration.
- Side-effecting operations remain preview-first and user-approved.
- Descriptor YAML and generated capability payloads remain the canonical place for tool/resource/prompt/completion shape.
- JSON `structuredContent` remains canonical even if protocol-native resource links are added.
- Real-model usability runs remain opt-in and outside default CI.

## Retained P0 Requirements

### RI-P0-01: URI encoding last mile

All descriptor-backed resource URIs that carry database, schema, table, column, rule, or algorithm names MUST use one centralized path-segment encoding helper.

Acceptance:

- Search result URIs, resource navigation, runtime first-check resources, execute-update resources, and workflow `resources_to_read` use the same helper.
- `MCPUriPattern` remains the read-side decode path.
- Non-ASCII characters, spaces, slash-like reserved characters, question marks, and percent signs round-trip without guessing.
- `rg "URLEncoder.encode"` has only the central helper after cleanup.

### RI-P0-02: Continuation guidance for truncation and pagination

When a response is truncated, paginated, or too broad to continue safely, the payload SHOULD include a continuation or narrowing `next_actions` entry.

Acceptance:

- SQL truncation says whether continuation is unsafe and asks for a narrower query when needed.
- Metadata pagination with `has_more=true` or `next_page_token` includes a safe retry action with copied scope and the next token.
- Continuation actions keep `order`, `target_tool` or `target_resource`, and safe arguments when known.

### RI-P0-03: Ambiguity hints for duplicate metadata names

Metadata search and lookup payloads SHOULD identify duplicate or ambiguous hits instead of letting the model pick a best match.

Acceptance:

- Ambiguity payloads include `ambiguous`, `ambiguous_by`, `candidate_count` when cheap, `narrowing_arguments`, and safe `next_actions`.
- Ambiguity is scoped to database, schema, object type, or name dimensions already present in metadata.
- No semantic ranking, best-guess selection, or model-ranked metadata explorer is introduced.

### RI-P0-04: Recovery target completeness

Recoverable tool errors SHOULD include exact public retry targets and argument paths when the server can know them.

Acceptance:

- `apply_workflow.execution_mode` recovery points back to `apply_workflow` and suggests `execution_mode=preview` unless approved execution is already established.
- Invalid `max_rows`, `timeout_ms`, `page_size`, and `page_token` recoveries name the source tool, argument path, allowed bounds, and safe retry arguments.
- Workflow missing-input recovery uses public names such as `primary_algorithm_properties`, `assisted_query_algorithm_properties`, and `like_query_algorithm_properties`.

### RI-P0-05: Workflow response-mode alignment

Workflow plan, apply, validate, manual-only, and recovery responses SHOULD expose stable response-mode fields like SQL execution does.

Acceptance:

- Models can distinguish `preview`, `executed`, `manual_only`, `validation`, `recovery`, and `terminal` without parsing prose.
- Manual-only output structurally states that the user must confirm external execution before validation.
- Preview output does not imply affected-row estimates or hidden execution.

### RI-P0-06: Next-action sequencing contract checks

Multi-action responses SHOULD be protected by deterministic contract tests that require ordering or dependency metadata where sequence matters.

Acceptance:

- Approval-gated follow-up actions depend on an explicit ask-user or approval action.
- `retry_tool`, `call_tool`, `read_resource`, and `complete_argument` actions name source/target metadata where known.
- Sequencing remains descriptive guidance only, not a planner or executor.

## Retained P1 Requirements

### RI-P1-01: EXPLAIN ANALYZE risk hint

Database capability payloads or descriptor docs SHOULD conservatively explain that `EXPLAIN ANALYZE` may execute the query on some engines.

Acceptance:

- The hint is database-capability scoped where engine support is known.
- The hint does not ban normal explain usage or add SQL parsing duplication.

### RI-P1-02: Single-schema completion comfort

Completion SHOULD auto-resolve a single safe schema context or return a schema-first next action.

Acceptance:

- When exactly one schema is available, the payload marks the value as `server_defaulted` or equivalent.
- When multiple schemas exist, completion asks the model to complete or read schema before table/column completion.

### RI-P1-03: Runtime status enrichment

Runtime status SHOULD remain compact and secret-free while exposing enough first-use state for model triage.

Acceptance:

- Include active transport, configured logical database count/names when safe, feature availability, and first-check resources.
- Do not expose JDBC credentials, bearer tokens, raw environment variables, or stack traces.
- Do not become an observability platform.

### RI-P1-04: Proxy topology guidance

Encrypt and mask workflow setup SHOULD warn when the runtime appears to target a physical database instead of ShardingSphere-Proxy's logical view, when that mismatch can be identified safely.

Acceptance:

- The guidance is a recovery hint, preflight hint, or documentation check.
- It does not validate live cluster topology, migrate data, or add a planner.

### RI-P1-05: Empty and not-found follow-up consistency

Empty, zero-hit, and not-found responses SHOULD use a consistent compact shape.

Acceptance:

- Include reason/category, parent/list/search follow-up, and broadened or narrowed retry arguments where safe.
- Keep zero-row SQL as a valid SQL result, not an error.

## Retained P2 Requirements

### RI-P2-01: Bounded request correlation remains optional

Recoverable errors MAY include a short-lived `request_id` only if it has no secrets, persistence, approval meaning, audit meaning, or session identity meaning.

Acceptance:

- If omitted, error recovery remains simpler and relies on `category`, `safe_message`, `suggested_arguments`, and `next_actions`.
- If implemented later, it must be generated per error response and never user-derived.

### RI-P2-02: Approval-violation metric should be trace-aware

The opt-in LLM usability report SHOULD count approval violations from trace behavior, not only from error codes.

Acceptance:

- A side-effect execution attempt before user approval is counted even if the server rejects it through a generic recovery path.
- Metrics stay opt-in and outside default CI.

### RI-P2-03: Token-safe health-check recipe

Documentation SHOULD show one short health-check sequence using existing resources.

Acceptance:

- The recipe is roughly: initialize, read runtime, read capabilities, optionally run a bounded safe query.
- It does not require new endpoints or expose secrets.

### RI-P2-04: HTTP package metadata shape

If OCI or public HTTP packaging becomes official, the package SHOULD expose machine-friendly MCP server identity and token configuration shape.

Acceptance:

- Keep examples secret-free and environment-placeholder based.
- Do not add OAuth, RBAC, or release-specific commitments in this requirement slice.

### RI-P2-05: MCP-native elicitation stays capability-gated

MCP-native elicitation MAY mirror canonical structured clarification questions only when the SDK and client advertise support.

Acceptance:

- Structured JSON clarification remains canonical fallback.
- No prose-only compatibility contract is reintroduced.
- SDK types stay at the transport boundary.

## Explicit Non-Goals

- No `list_*` or `describe_*` compatibility matrix.
- No planner, graph traversal, vector search, semantic ranking, model-call ranking, or cross-session memory.
- No durable preview/approval tokens, RBAC, tenant isolation, production approval workflow, migration/backfill, or rollback orchestration.
- No default-CI live-model benchmark.
- No parallel old/new model-facing payload fields kept solely for compatibility.

## Implementation Slice Order

1. Close P0 URI, continuation, ambiguity, recovery target, response-mode, and sequencing tests first.
2. Add P1 runtime, EXPLAIN, completion, topology, and empty/not-found comfort only after P0 contract tests are stable.
3. Keep P2 optional and documentation/metric focused unless a later implementation task explicitly accepts the tradeoff.
4. For each slice, remove replaced URI-only, prose-only, or duplicate fields in the same change rather than carrying long-lived compatibility shims.

Detailed executable tasks live in `implementation-task-breakdown.md`.

## 2026-05-07 Requirement Sweep

The final repeated-question addendum lives in `requirement-sweep-2026-05-07.md`.

It reconciles the remaining model-comfort questions that are easy to miss after the P0/P1/P2 implementation baseline:

- compact first-hop behavior, direct metadata resource size control, `max_rows=0` semantics, workflow argument conflicts, workflow preview review focus, documentation drift, and field naming contracts;
- single-candidate completion inference, capability-gated elicitation, secret-free runtime readiness, result payload token control, resource-hint parity, SQL/JDBC recovery categories, terminal stop clarity, and deterministic MCP client smoke;
- distribution examples, token-safe health checks, capability fingerprint usability, descriptor authoring lint, and optional request correlation.

This addendum does not reopen rejected system-sized ideas such as planners, graph traversal, vector search, cross-session memory, approval-token platforms, RBAC, or default-CI live-model suites.

## Verification Map

- Documentation-only changes: run `git diff --check`.
- Java or descriptor changes: run scoped MCP tests for the touched modules.
- Java changes: run scoped Checkstyle with `-Pcheck`.
- Payload removals: add negative shape assertions and documentation searches.
- Branch rule: confirm `git branch --show-current` before reporting completion.
