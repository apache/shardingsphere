# Implementation Plan: MCP AI-Native Polish

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-05-05 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/.specify/specs/009-mcp-ai-native-polish/spec.md`
**Note**: This plan is prepared manually in Spec Kit style without running branch-changing commands.

## Summary

Organize the next ShardingSphere MCP model-usability polish increment around compact capability contracts, short static common flows,
clearer metadata search context, less inferential SQL/workflow outputs, and server-owned approval summaries.
The work is deliberately small: reuse descriptors, capabilities, response builders, recovery conversion, existing tests, and opt-in LLM scenarios.

## Technical Context

**Language/Version**: Java 21 for MCP modules
**Primary Dependencies**: Existing ShardingSphere MCP API/support/core/bootstrap, descriptor YAML, MCP Java SDK integration, JUnit 5, Mockito
**Storage**: No new durable storage; use existing runtime metadata and current-session workflow context only
**Testing**: Module-scoped JUnit tests, descriptor/catalog tests, capabilities shape tests, opt-in LLM E2E only when explicitly enabled
**Target Platform**: ShardingSphere MCP standalone runtime over HTTP and STDIO
**Project Type**: Java backend protocol surface plus descriptor and documentation assets
**Performance Goals**: No model calls, no vector indexes, no background graph traversal; added metadata should be built from already-available descriptors or response values
**Constraints**:

- Do not switch or create git branches.
- Keep current resource-first discovery.
- Keep side-effecting operations preview-first and user-approved.
- Do not add a planner, graph engine, vector search, cross-session memory, approval-token platform, RBAC platform, or default-CI live-model suite.
- Keep changes small enough for independent review slices.

**Scale/Scope**:

- Primary paths: `mcp/support`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`, `test/e2e/mcp`.
- Descriptor and docs paths: `mcp/**/META-INF/shardingsphere-mcp/descriptors`, `mcp/README.md`, `mcp/README_ZH.md`, `docs/mcp`.
- Speckit paths: `.specify/specs/009-mcp-ai-native-polish/`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

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
|-- quickstart.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

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
Tool response polish belongs in `mcp/core` and workflow support helpers. Transport-facing parity and completion behavior belong in `mcp/bootstrap` or `test/e2e/mcp`.

## Phase 0: Research

- Inspect current `shardingsphere://capabilities` payload construction before adding next-action contracts or common flows.
- Inspect all current `next_actions` producers before documenting action kinds.
- Inspect `search_metadata` matching and pagination code before adding search context or match explanation fields.
- Inspect URI derivation rules before deciding whether to encode safe names or return `not_safe_to_derive`.
- Inspect numeric argument parsing for `page_size`, `max_rows`, and `timeout_ms` before deciding recovery versus applied-default metadata.
- Inspect `execute_query`, `execute_update`, workflow planning, workflow apply, and validation payloads before adding count, status, item-shape, or approval-summary fields.
- Inspect descriptor lint and capabilities tests before adding new checks.
- Inspect opt-in LLM E2E scenario support before adding intent-only discovery scenarios.

## Phase 1: Design

- Define the capability-level `next_action_contract` shape.
- Define compact `common_flows` with short, descriptor-backed steps.
- Define search response additions: applied context, per-hit match explanation, and unsafe URI behavior.
- Define output parse hints for SQL and workflow payloads.
- Define approval-summary wording rules that preserve user approval boundaries.
- Define focused tests without broad snapshots.

## Phase 2: Implementation Strategy

1. Add deterministic tests for the new capabilities sections before changing payload shape.
2. Add `next_action_contract` and `common_flows` from descriptor-known tools/resources.
3. Add `search_metadata` search context and match explanation while preserving current pagination behavior.
4. Add output parse hints and schema updates for SQL/workflow payloads.
5. Add approval summaries after preview payloads already contain all required facts.
6. Add optional parity and opt-in LLM usability checks only after deterministic tests are stable.

## Complexity Tracking

No constitution violations are expected.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |
