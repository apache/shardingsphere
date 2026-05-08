# Continuous Optimization 100 Closure: MCP AI-Native Polish

**Date**: 2026-05-08
**Branch observed**: `001-shardingsphere-mcp`
**Branch rule**: Do not switch, create, or check out branches.
**Purpose**: Convert the sustained "can this still be improved without over-design?" review into a bounded SpecKit closure plan.

## Score Meaning

This document does not replace `implementation-baseline-100.md`.

- The existing 009 baseline defines the original accepted 100 percent delivery scope.
- This addendum defines the stricter continuous-optimization 100 percent scope.
- Continuous-optimization 100 means the remaining concrete, non-over-designed model-comfort gaps are closed by code, descriptors, tests, or live-model evidence.
- If live Qwen validation is blocked by an invalid or missing key, deterministic code completion can still be complete, but continuous-optimization scoring must not claim a live 100.

## Decision Rules

- Prefer the smallest contract that removes real model guessing.
- Keep JSON `structuredContent` canonical.
- Keep protocol-native `ResourceLink` content additive and bounded.
- Keep live-model E2E opt-in and outside default CI.
- Do not reopen rejected system-sized designs: planner, graph traversal, vector search, semantic ranking, cross-session memory, approval-token platforms, RBAC, or default-CI live-model suites.
- Do not add optional result or trace concepts unless a deterministic test or live transcript shows a real model-comfort gap.

## Hard Closure Requirements

### CO-P0-01: Direct metadata list hard cap

Direct metadata list resources MUST avoid returning unbounded `items` payloads.

Acceptance:

- Non-detail metadata resources return at most the direct-list cap when the loaded item count is larger than the cap.
- The payload exposes `total_count`, `returned_count`, `truncated`, and safe `next_actions`.
- Detail resources continue to return detail payloads without list capping.
- `search_metadata` remains the preferred broad discovery path.

Rationale:

- Current broad metadata list guidance explains the risk, but the full list is already present in the payload.
- A model-friendly resource must prevent context expansion, not only warn after expansion.

### CO-P0-02: Blank all-database search guard

`search_metadata` MUST avoid expanding an empty query across every database and every object type.

Acceptance:

- When `query`, `database`, and `object_types` are all empty, the tool returns compact database or scope navigation and narrowing `next_actions`.
- Cross-database search with a non-empty `query` continues to work.
- Database-scoped blank query continues to support list-within-scope behavior.
- Existing invalid `schema` without `database` recovery remains unchanged.

Rationale:

- This closes the unhandled broadest search subcase without removing useful scoped blank-query discovery.

### CO-P0-03: Bounded ResourceLink content

Transport-created `ResourceLink` content MUST be capped and prioritized.

Acceptance:

- The transport emits ResourceLinks for the most useful hints first: `resources_to_read`, direct `resource`, `parent_resource`, then `next_resources` and item-local hints.
- The number of ResourceLinks in `CallToolResult.content` is bounded by one fixed in-code limit.
- `structuredContent` still contains the canonical full JSON payload.
- Link-count metadata such as emitted and omitted counts is exposed through transport result `meta`, not by mutating canonical payloads.

Rationale:

- Native MCP clients benefit from `ResourceLink`, but recursive link extraction can expand content size on large search results.

### CO-P0-04: Global metadata ambiguity

Metadata ambiguity SHOULD be computed from all filtered candidates before pagination when those candidates are already available.

Acceptance:

- Search metadata payloads expose `total_match_count`.
- Duplicate-name ambiguity is based on the full filtered candidate list, not only the current page.
- Ambiguity payloads still include `ambiguous`, `ambiguous_by`, `candidate_count`, `duplicated_names`, `narrowing_arguments`, and safe `next_actions`.
- No semantic ranking, best-guess selection, or model-ranked metadata explorer is introduced.

Rationale:

- Current page-local ambiguity can miss duplicate names split across pages.

### CO-P0-05: Live Qwen full-score evidence

The opt-in LLM usability suite SHOULD produce one valid Qwen run with overall score 100 before claiming continuous-optimization 100.

Acceptance:

- The run uses the existing `llm-e2e` lane and the configured OpenAI-compatible Qwen endpoint.
- The produced scorecard has `overallScore=100.0` and `fullScore=true`.
- If the local model service is unavailable, returns a readiness failure, or has not pulled `qwen3:1.7b`, the report records the validation blocker and does not claim live 100.
- Any non-100 transcript drives only targeted fixes; no speculative feature expansion is allowed.

Rationale:

- Continuous-optimization 100 is a real-model usability claim, so deterministic tests are necessary but not sufficient when the local Ollama `qwen3:1.7b` service is ready.

## High-Value Closure Requirements

### CO-P1-01: Runtime capability fingerprint reference

Runtime status SHOULD help a model detect public-surface drift without rereading the full capability catalog.

Acceptance:

- `shardingsphere://runtime` includes a compact capability fingerprint or descriptor-catalog fingerprint when available.
- It also keeps a typed resource hint to `shardingsphere://capabilities`.
- Runtime status remains secret-free and compact.

### CO-P1-02: Manual artifact summary

Manual-only workflow responses SHOULD expose a compact summary of exported manual artifacts.

Acceptance:

- The summary includes artifact counts by category, whether external execution is required, whether user confirmation is required, and the recommended validation action.
- It does not duplicate full DDL or DistSQL bodies already present in artifact fields.
- Sensitive values remain masked.

### CO-P1-03: Context-loss recovery E2E

LLM E2E SHOULD prove a model can recover after partial context loss using the existing MCP surface.

Acceptance:

- A scenario simulates a model retaining only a current-session `plan_id` or a compact runtime/status hint.
- The model must recover by reading `shardingsphere://workflows/{plan_id}`, `shardingsphere://runtime`, or typed resource hints.
- The protocol remains unchanged unless the transcript proves a missing field.

### CO-P1-04: Descriptor anti-regression checks

Descriptor loading SHOULD fail fast when new MCP descriptors omit model-critical metadata.

Acceptance:

- Checks cover model-visible output fields, `next_actions`, resource hints, completion targets, examples, annotations, and side-effect metadata where applicable.
- The check remains deterministic and descriptor-level.
- It does not call a model.

## Explicitly Covered Or Rejected

- Search ranking explanation is already covered by `match_kind`, `matched_fields`, and `matched_value`; no new ranking feature is required.
- SQL compact output or `result_format` is watch-only until a transcript proves token pressure beyond current `row_object_status`, `row_objects`, and truncation guidance.
- Optional `request_id` remains omitted unless local triage needs a bounded, secret-free correlation value.
- No planner, semantic search, vector index, approval-token system, RBAC, or default-CI live-model benchmark is part of this addendum.

## Continuous 100 Gate

Continuous-optimization 100 can be claimed only when all of the following are true:

1. CO-P0-01 through CO-P0-05 are implemented or validated with evidence.
2. CO-P1-01 through CO-P1-04 are implemented with deterministic tests or explicitly proven unnecessary by the final transcript review.
3. Scoped MCP tests and Checkstyle pass for touched Java modules.
4. `git diff --check` passes.
5. Branch verification still reports `001-shardingsphere-mcp`.
6. A final repeated-question review finds no remaining concrete, non-over-designed gap that would materially improve native, convenient, comfortable, and clear model usage.

If the local Ollama service is unavailable or `qwen3:1.7b` is not pulled, report deterministic completion separately from live continuous-optimization scoring.

## 2026-05-08 Closure Record

### Implemented

- CO-P0-01 through CO-P0-04 are implemented with deterministic tests.
- CO-P1-01 through CO-P1-04 are implemented with deterministic tests or descriptor-level checks.
- Qwen live validation is scoped to the local Ollama `qwen3:1.7b` E2E stack; cloud-hosted Qwen credential failures are not accepted as this gate's result.
- The LLM suite now fails fast on non-retryable readiness failures and performs a model readiness preflight before preparing runtime fixtures.

### Deterministic Verification

- Scoped MCP and LLM unit tests passed for descriptor loading, metadata resources/search, runtime status, workflows, transport ResourceLinks, feature handlers, and LLM scenario orchestration.
- Scoped Checkstyle passed for `mcp/core`, `mcp/bootstrap`, and `test/e2e/mcp`.
- The final hygiene gate is `git diff --check` plus branch verification on `001-shardingsphere-mcp`.

### Final Repeated-Question Review

- Remaining concrete model-comfort gaps from this addendum are closed in code, descriptors, tests, or recorded as external live-validation blocker.
- No planner, semantic search, vector index, cross-session memory, approval-token system, RBAC, or default-CI live-model benchmark is introduced.
- The only unfinished evidence is a successful Qwen live scorecard against the local Ollama `qwen3:1.7b` stack.
