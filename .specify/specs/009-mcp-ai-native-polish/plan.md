# Implementation Plan: MCP AI-Native Polish

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-05-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/.specify/specs/009-mcp-ai-native-polish/spec.md`
**Note**: This plan is prepared manually in Spec Kit style without running branch-changing commands.

## Summary

Organize the next ShardingSphere MCP model-usability polish increment around the small gaps that remain after the completed AI-friendly baseline.
The current surface already has capability contracts, common flows, search match explanations, output parse hints, approval summaries, structured recovery,
completion diagnostics, and opt-in usability tests.
This increment focuses on action ordering, compact surface summary, navigation/completion type hints, empty-state diagnostics, argument provenance,
runtime recovery diagnostics, safe input bounds, structured clarification, current-session workflow read-back, URI encoding, configuration comfort,
opt-in usability metrics, exact recovery targets, response-mode clarity, row parsing comfort, bounded ambiguity hints, executable completion actions,
resource-read error shape, typed resource hints, schema-visible defaults/examples, and optional resource links.

## 100 Percent Re-baseline

The original task checkboxes lagged behind the implementation. Use
[`implementation-baseline-100.md`](./implementation-baseline-100.md) as the authoritative completion baseline for finishing 009.

The target is not to implement every speculative idea mechanically. The target is to close every retained task as implemented, design-closed,
rejected by explicit non-goal, or confirmed by the user where protocol behavior changes.

Use [`final-requirement-inventory.md`](./final-requirement-inventory.md) as the final repeated-question requirement inventory.
It groups remaining work into retained P0/P1/P2 items, current baseline to preserve, explicit non-goals, implementation order, and verification map.
Use [`implementation-task-breakdown.md`](./implementation-task-breakdown.md) as the next implementation queue after the accepted defaults.

User confirmed protocol-native `ResourceLink` content and interactive MCP-native elicitation on 2026-05-06.
The optional bounded `request_id` is intentionally omitted by the recommended conservative decision.

## Technical Context

**Language/Version**: Java 21 for MCP modules
**Primary Dependencies**: Existing ShardingSphere MCP API/support/core/bootstrap/features, descriptor YAML, MCP Java SDK integration, JUnit 5, Mockito
**Storage**: No new durable storage; use existing runtime metadata and current-session workflow context only
**Testing**: Module-scoped JUnit tests, descriptor/catalog tests, capabilities shape tests, recovery tests, opt-in LLM E2E only when explicitly enabled
**Target Platform**: ShardingSphere MCP standalone runtime over HTTP and STDIO
**Project Type**: Java backend protocol surface plus descriptor and documentation assets
**Performance Goals**: No model calls, no vector indexes, no background graph traversal; added metadata should be built from already-available descriptors or response values
**Constraints**:

- Do not switch, create, or check out git branches.
- Keep current resource-first discovery.
- Keep side-effecting operations preview-first and user-approved.
- Do not add a planner, graph engine, vector search, cross-session memory, approval-token platform, RBAC platform, or default-CI live-model suite.
- Keep changes small enough for independent review slices.
- Prefer canonical payload replacement and descriptor/schema clarification over protocol rewrites.
- Prefer one clean canonical payload shape over old/new dual fields. Delete obsolete fields, code, tests, and docs in the same change slice.
- Use additive fields only as a temporary implementation step inside a review slice, not as a long-lived compatibility contract.

**Scale/Scope**:

- Primary paths: `mcp/support`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`, `test/e2e/mcp`.
- Descriptor and docs paths: `mcp/**/META-INF/shardingsphere-mcp/descriptors`, `mcp/README.md`, `mcp/README_ZH.md`, `docs/mcp`.
- Speckit paths: `.specify/specs/009-mcp-ai-native-polish/`.

## Constitution Check

*GATE: Must pass before implementation planning. Re-check after design.*

- **Proxy-first logical abstraction**: Pass. Requirements continue to expose logical databases, tables, columns, resources, and workflow artifacts from the logical view.
- **Explicit operator control**: Pass. Preview and user approval remain mandatory for side-effecting SQL and workflow apply.
- **Minimal safe automation**: Pass. No migration, backfill, rollback orchestration, planner, or hidden execution shortcut is introduced.
- **Deterministic naming and transparent changes**: Pass. This package does not alter derived naming; it improves visibility of existing outputs.
- **Complete verification before completion**: Pass. New contracts require deterministic tests; real-model usability remains opt-in.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/009-mcp-ai-native-polish/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- breaking-cleanup-analysis.md
|-- final-requirement-inventory.md
|-- implementation-task-breakdown.md
|-- quickstart.md
|-- current-behavior-analysis.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code (future implementation)

```text
mcp/
|-- support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/
|-- support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
|-- core/src/main/java/org/apache/shardingsphere/mcp/core/
|-- bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/
|-- features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
|-- features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
|-- README.md
`-- README_ZH.md

test/e2e/mcp/
docs/mcp/
```

**Structure Decision**: Keep implementation in existing MCP modules. Capabilities and descriptor contracts belong in `mcp/support`.
Tool/resource response polish belongs in `mcp/core` and workflow support helpers. Transport-facing completion behavior belongs in `mcp/bootstrap`.
Opt-in usability metrics belong in `test/e2e/mcp`.

## Phase 0: Current Evidence

- Confirm branch with `git branch --show-current`; do not run branch-changing commands.
- Reconfirm 008/003 baseline and current code evidence before implementation.
- Inspect `MCPDescriptorCatalog` for existing `model_contract`, `next_action_contract`, `common_flows`, payload contracts, security hints, and fingerprints.
- Inspect all current `next_actions` producers for action ordering and retry target visibility.
- Inspect completion metadata for missing-context diagnostics and candidate-ranking metadata.
- Inspect resource navigation descriptors and capability payload for type-hint gaps.
- Inspect resource, detail, and search responses for empty-state and not-found diagnostics.
- Inspect SQL/workflow preview and success responses for reusable arguments, normalized SQL, redaction markers, and manual-only follow-up.
- Inspect runtime error conversion for JDBC/config/connection-specific recovery categories.
- Inspect opt-in LLM usability metrics before adding new metrics.
- Inspect SQL row-limit handling, timeout handling, and broad-result truncation.
- Inspect metadata search pagination, blank-query behavior, invalid argument recovery, and URI encoding/decoding.
- Inspect workflow clarification outputs, non-English intent handling, and current-session `plan_id` recovery.
- Inspect runtime configuration loading, server identity metadata, HTTP authentication errors, and first-use documentation.
- Inspect workflow recovery for exact retry target and public missing-field paths.
- Inspect SQL response schemas for preview/executed mode markers, preview-limit wording, and row-object feasibility.
- Inspect metadata-introspection SQL recovery, duplicate search-hit handling, and URI encoding call sites.
- Inspect Docker/HTTP/Proxy-topology setup docs and runtime hints for secret-safe clarity.
- Inspect `complete_argument` actions for completion reference, context argument, and resume-target gaps.
- Inspect resource-read transport behavior for a machine-readable error marker inside resource content.
- Inspect `resources_to_read` and `next_resources` payloads for typed purpose/kind hints.
- Inspect descriptor-derived schema fragments for defaults, numeric bounds, examples, and patterns.
- Inspect tool-result transport support for additive MCP resource links and required canonical JSON fields.
- Inspect breaking cleanup impact in `breaking-cleanup-analysis.md` before any implementation slice that removes old model-facing shapes.

## Phase 1: Design

- Define ordered next-action metadata that is purely descriptive and does not execute anything.
- Define compact `surface_summary` fields in capabilities.
- Define navigation type hints and completion availability hints without duplicating the whole completion catalog.
- Define empty-state and not-found response hints.
- Define argument provenance values and redaction marker expectations.
- Define conservative runtime recovery categories and optional request/trace identifier constraints.
- Define opt-in next-action-follow and approval-violation metrics.
- Define safe SQL and metadata input-bound contracts.
- Define structured clarification questions as the canonical contract and delete prose-only fallback rules.
- Define current-session workflow status read-back.
- Define percent-encoded resource identifier behavior.
- Define secret-safe runtime status and env-placeholder configuration expectations.
- Define exact recovery target and public argument-path rules.
- Define response-mode markers for preview, executed, manual-only, validation, recovery, truncation, and pagination states.
- Define optional result-row object rules that preserve positional rows.
- Define metadata ambiguity and introspection-SQL recovery hints.
- Define executable completion action fields that name the completion reference, context arguments, and resume target.
- Define resource error payload fields for `resources/read` responses.
- Define typed resource hints for `resources_to_read` and `next_resources`.
- Define schema-visible default, bound, example, and pattern rules.
- Define optional resource-link usage as additive transport metadata, not a replacement for JSON payload fields.
- Define deletion boundaries for old URI-only, prose-only, and duplicate payload contracts before changing production code.

## Phase 2: Implementation Strategy

1. Add or update deterministic tests for each new model-facing field before changing payload shape.
2. Add capability-level `surface_summary`, navigation type hints, and completion availability hints.
3. Add ordered next-action/dependency metadata to preview, workflow, and recovery producers.
4. Add empty-state and not-found diagnostics to resource/search payloads.
5. Add argument provenance and redaction markers where values are already known.
6. Improve safe runtime recovery categories and optional trace/request identifiers.
7. Add row/search bounds, strict argument recovery, and URI encoding support.
8. Add structured clarification, Chinese synonym guidance, and current-session workflow read-back.
9. Add secret-safe runtime status, env-placeholder docs/config support, and clearer auth/server identity hints.
10. Add exact workflow recovery targets, response-mode markers, row-object convenience, metadata ambiguity hints, and topology/setup comfort.
11. Add executable completion actions, resource-read error payloads, typed resource hints, schema-visible defaults/examples, and optional resource links.
12. Remove replaced compatibility fields, obsolete helpers, old contract tests, and stale documentation in the same slice as each canonical contract.
13. Add opt-in usability metrics without changing default CI.

## Complexity Tracking

No constitution violations are expected.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |
