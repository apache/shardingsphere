# 100 Percent Implementation Baseline: MCP AI-Native Polish

**Date**: 2026-05-06
**Branch observed**: `001-shardingsphere-mcp`
**Scope**: Re-baseline the existing 009 Speckit package so "100%" means a reviewable, implemented MCP surface, not an unbounded wishlist.

## Completion Definition

This increment is complete when every retained task is closed in one of these ways:

- **Implemented and verified**: production code, descriptor contracts, docs, and tests exist.
- **Design-closed**: the task is a design task and this package defines the contract clearly enough for implementation.
- **Rejected by constraint**: the task would introduce a planner, graph, vector search, cross-session memory, approval-token system, RBAC, or default live-model CI.
- **Intentionally omitted**: the task was reviewed and rejected because it would add a concept with low model-comfort value.

Closed tasks must not preserve old and new model-facing contracts in parallel. Removed payload fields, tests, and docs must stay removed.

## Current Evidence

The current codebase already satisfies more of the original backlog than the stale checkboxes showed:

- Capabilities already expose `surface_summary`, `next_action_contract`, `common_flows`, payload contracts, fingerprints, completion hints, and typed resource navigation.
- `execute_update` preview already emits `response_mode`, `result_kind`, `preview_semantics`, `affected_rows_estimated=false`, ordered `next_actions`, `depends_on`, typed `resources_to_read`, and `argument_provenance`.
- Workflow apply and validation already emit ordered next actions, manual follow-up guidance, redacted artifacts, and current-session plan read-back through `shardingsphere://workflows/{plan_id}`.
- `search_metadata` already exposes typed `resource`, `parent_resource`, `next_resources`, `empty_state`, `search_context`, encoded resource URIs, invalid page-token recovery, and blank-query scoped listing.
- SQL execution already applies `max_rows` and `timeout_ms` defaults and bounds, exposes `applied_max_rows`, `applied_timeout_ms`, `truncated`, `row_object_status`, and safe `row_objects`.
- Runtime status already exists at `shardingsphere://runtime`, and startup/README docs cover token state, placeholders, STDIO/HTTP examples, and first checks.
- Opt-in LLM usability metrics already include next-action-follow and approval-violation rates without default live-model CI.

## Retained Workstreams for True 100%

### A. Speckit Status and Negative Contract Checks

Goal: make the Speckit status match current code and prevent old contracts from reappearing.

Tasks:

- Mark implemented task rows in `tasks.md` so completion tracking reports the current 100% closure state.
- Add deterministic negative assertions or documentation searches for removed prose-only questions, URI-only resource lists, old `complete_argument`, and prose-only bounds.
- Keep the breaking cleanup evidence in `breaking-cleanup-analysis.md`; do not use it as a live contract.

Verification:

- `rg` finds no old canonical fields in production MCP code: `pending_questions`, `resource_uri`, `parent_uri`, `next_resource_uris`, `read_resources_first`, `empty_reason`, or `not_found_reason`.
- Scoped MCP tests and Checkstyle pass.

### B. Protocol-Native Resource Links

Goal: use SDK-supported `ResourceLink` content where it improves native MCP clients, while keeping JSON `structuredContent` canonical.

Implementation target:

- Add resource-link content to tool results that already return typed resource hints, such as `resources_to_read`, `resource`, `parent_resource`, and `next_resources`.
- Do not add old URI-only fields for clients that ignore links.
- Keep links additive and bounded: no semantic link expansion and no hidden resource reads.

Verification:

- Bootstrap transport tests assert `CallToolResult.content` contains `ResourceLink` entries when structured payload contains resource hints.
- Existing structured JSON payload tests remain unchanged except for added link assertions.

Decision:

- This is implementable because MCP Java SDK 1.1.0 exposes `McpSchema.ResourceLink`.
- User confirmed on 2026-05-06 that protocol-native `ResourceLink` content is part of the 100% target.
- Use the conservative additive form: transport links are extra content, while JSON `structuredContent` remains canonical.

### C. MCP-Native Elicitation

Goal: mirror structured clarification questions through SDK-supported elicitation only when the client advertises the capability.

Implementation target:

- Thread `McpSyncServerExchange` capability information only through the transport layer; do not leak SDK types into core workflow services.
- For missing workflow inputs, build an `ElicitRequest` from canonical `clarification_questions`.
- If the client does not advertise elicitation, return the existing structured payload without adding prose-only fallback fields.

Verification:

- Transport tests cover client supports elicitation, client does not support elicitation, and user rejects or cancels elicitation.
- Workflow payload tests still assert `clarification_questions` as the canonical contract.

Decision:

- This can block a tool call while asking the client/user. Confirm that this interactive behavior is desired for the 100% target.
- User confirmed on 2026-05-06 that MCP-native elicitation should be implemented as real interactive transport behavior.
- Keep structured JSON `clarification_questions` as the canonical non-interactive fallback.

### D. SQL and Metadata Continuation Comfort

Goal: make truncated, paginated, broad, duplicate, or ambiguous results tell the model the safest next step.

Implementation target:

- Add truncation-specific `next_actions` for SQL result sets. If continuation is not safely possible, ask for a narrower query instead of implying pagination.
- Add pagination-specific `next_actions` for metadata search results that include `next_page_token`.
- Add duplicate or ambiguous-hit hints when a name appears across databases, schemas, or object types.
- Keep all narrowing deterministic; no semantic ranking or best-guess selection.

Verification:

- SQL response tests assert truncated result guidance.
- Search tests assert pagination next action and duplicate-hit narrowing guidance.

### E. Resource URI Encoding Cleanup

Goal: centralize path-segment percent encoding and keep decode behavior consistent.

Implementation target:

- Move repeated `URLEncoder.encode(...).replace("+", "%20")` logic into one MCP resource URI utility.
- Use the utility from search result URI generation, runtime hints, execute-update resources, and workflow `resources_to_read`.
- Keep `MCPUriPattern` decoding as the single read-side decode path.

Verification:

- Unit tests cover non-ASCII, spaces, slash-like reserved characters, question marks, and round-trip resource reads.
- `rg "URLEncoder.encode"` should show only the central utility after cleanup.

### F. Runtime Recovery and EXPLAIN Semantics

Goal: finish the small runtime and SQL safety hints without building observability.

Implementation target:

- Add dedicated tests for `missing_jdbc_driver`, `authentication_failed`, `connection_timeout`, and `database_unavailable` recovery categories.
- Omit optional bounded `request_id` so recoverable errors stay category/action based.
- Add explicit EXPLAIN ANALYZE execution-risk wording to database capability payloads or docs where the engine supports it.

Verification:

- Runtime recovery tests cover each category and assert secret-safe messages.
- Capability/docs tests assert conservative EXPLAIN ANALYZE wording.

Decision:

- `request_id` means a server-generated, short-lived correlation value returned with a recoverable error so a user, model, or log reader can refer to that one failure without copying the full error payload.
- If implemented, it must be generated per error response, not persisted, not user-derived, not secret-bearing, and not treated as an approval token, session ID, audit ID, or durable trace system.
- User chose the recommended path on 2026-05-06: omit `request_id`. Recoverable errors stay simpler and rely only on `category`, `field`, `safe_message`, `suggested_arguments`, and `next_actions`.

### G. Proxy Topology and Packaging Checks

Goal: keep workflow setup clear without adding preflight planners.

Implementation target:

- Add a lightweight Proxy-topology hint or recovery path for encrypt/mask workflows when runtime evidence suggests a direct physical database connection.
- Add documentation checks for bind host, bearer token, env placeholders, and no embedded secrets.
- Keep this as docs/recovery/preflight only; do not validate real cluster topology or add migration logic.

Verification:

- Scoped docs or bootstrap tests assert secret-free examples.
- Workflow tests assert Proxy logical-view guidance where the runtime context can identify the risk.

## Explicit Non-Goals That Stay Rejected

- New `list_*` or `describe_*` compatibility tools.
- Planner, graph traversal, vector search, semantic ranking, or cross-session memory.
- Durable approval tokens, RBAC, tenant isolation, or hidden execution.
- Default-CI live-model E2E.
- Parallel old/new payload fields solely for compatibility.

## User Confirmation Closure

1. Protocol-native `ResourceLink` content is accepted and should be implemented conservatively as additive transport content.
2. MCP-native elicitation is accepted and should be implemented as an interactive transport-layer path with structured JSON fallback.
3. The optional bounded `request_id` is intentionally omitted to avoid creating a new trace concept.
