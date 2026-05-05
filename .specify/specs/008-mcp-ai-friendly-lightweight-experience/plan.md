# Implementation Plan: MCP AI-Friendly Lightweight Experience

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-05-04 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/.specify/specs/008-mcp-ai-friendly-lightweight-experience/spec.md`
**Note**: This plan is prepared manually in Spec Kit style without running branch-changing commands.

## Summary

Improve ShardingSphere MCP model usability with small, concrete changes:
align current-surface documentation, standardize next-action guidance, return direct metadata resource URIs, align output schemas,
harden common recovery metadata, introduce minimal descriptor lint, protect capabilities with a lightweight contract test,
and add examples or opt-in usability coverage only where they directly reduce model confusion.

This plan intentionally avoids heavy redesign. It preserves the existing resource-first surface, descriptor catalog, prompts, completions,
workflow tools, HTTP/STDIO transports, and session-scoped workflow behavior.
P0 is the implementation gate; P1 and P2 are explicitly non-blocking comfort improvements.

## Technical Context

**Language/Version**: Java 21 for MCP modules
**Primary Dependencies**: ShardingSphere MCP API/support/core/bootstrap, MCP Java SDK in bootstrap, descriptor YAML, JUnit 5, Mockito
**Storage**: Existing in-memory MCP session and workflow session context only; no new durable storage
**Testing**: Module-scoped JUnit tests, descriptor catalog tests, transport contract tests, opt-in LLM E2E extension
**Target Platform**: ShardingSphere MCP standalone runtime with HTTP and STDIO transports
**Project Type**: Java service modules plus descriptor and documentation assets
**Performance Goals**: No new model calls, vector search, background indexing, or cross-session ranking; descriptor checks should stay deterministic and fast
**Constraints**:

- Do not switch or create git branches.
- Do not redesign MCP runtime or feature SPI.
- Preserve preview-before-execute semantics for side-effecting actions.
- Keep real-model E2E opt-in.
- Avoid broad compatibility layers for historical `list_*` tool names in this increment.
- Record code evidence, affected paths, verification mapping, explicit non-goals, and rollback boundary before implementing each P0 item.

**Scale/Scope**:

- Public MCP model surface: capabilities, descriptors, prompts, completion metadata, common tool outputs, common error recovery.
- Primary modules: `mcp/support`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`, `test/e2e/mcp`, `mcp/README.md`, `mcp/README_ZH.md`, `docs/mcp`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Proxy-first logical abstraction**: Pass. Encrypt and mask workflow requirements remain logical-database and logical-table oriented. Metadata and SQL baseline continue to use logical MCP databases.
- **Explicit operator control**: Pass. Side-effecting SQL and workflow apply remain preview-first and require user approval before execution.
- **Minimal safe automation**: Pass. The plan does not add data migration, backfill, rollback orchestration, hidden planning, or automatic destructive behavior.
- **Deterministic naming and transparent changes**: Pass. This increment does not change generated naming behavior; examples and next actions make generated artifacts more visible.
- **Complete verification before completion**: Pass. The plan adds lightweight contract tests, descriptor lint, and opt-in usability scenarios; workflow validation semantics remain intact.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/008-mcp-ai-friendly-lightweight-experience/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
`-- tasks.md
```

### Source Code (repository root)

```text
mcp/
|-- README.md
|-- README_ZH.md
|-- support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/
|-- support/src/main/resources/META-INF/shardingsphere-mcp/
|-- core/src/main/java/org/apache/shardingsphere/mcp/core/
|-- bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/
|-- features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/
`-- features/mask/src/main/resources/META-INF/shardingsphere-mcp/

test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
docs/mcp/
```

**Structure Decision**: Keep implementation in existing MCP modules. Descriptor quality belongs in `mcp/support`.
SQL and workflow response guidance belongs in `mcp/core` and workflow support helpers.
Transport visibility tests belong in `test/e2e/mcp` or existing bootstrap transport tests.
Documentation alignment belongs in `mcp/README.md`, `mcp/README_ZH.md`, and existing `docs/mcp` files.

## Phase 0: Research

- Confirm the current MCP public surface from descriptor YAML, `shardingsphere://capabilities`, and README.
- Confirm whether historical docs under `docs/mcp` could be mistaken for current public contract.
- Inspect `execute_update` preview handler and tests before changing guidance fields or adding `suggested_arguments`.
- Inspect `search_metadata` result construction and descriptor-backed resource URI patterns before adding direct URI hints.
- Inspect real output payloads and descriptors for the seven core tools before changing output schemas.
- Inspect workflow preview guidance before deciding whether SQL preview and `apply_workflow` can share one lightweight vocabulary.
- Inspect recovery behavior for missing database, missing execution mode, wrong SQL tool, unknown tool/resource, and unavailable `plan_id`.
- Decide descriptor lint placement by reusing descriptor loader or support-layer tests where possible.
- Decide the `shardingsphere://capabilities` contract boundary by asserting section and shape without large snapshots.
- For P1 only, inspect whether existing metadata resources, workflow outputs, completion handlers, and algorithm resources can expose the requested hints without new storage.
- For P2 only, inspect startup logging, server instructions, package metadata, and README before documenting client configuration and troubleshooting hints.

### Phase 0 Deliverables

Each P0 item must leave a short implementation note before coding:

- `current_behavior`: descriptor, handler, resource, workflow, completion, test, or README evidence.
- `affected_paths`: minimal production, test, descriptor, and document paths expected to change.
- `verification_map`: deterministic tests or manual document checks mapped to acceptance criteria.
- `non_goals`: related capabilities intentionally excluded from the slice.
- `rollback_boundary`: behavior restored by reverting the slice.

### Fixed Decisions Not Reopened

- Do not switch or create git branches.
- Preserve resource-first discovery as the current MCP surface.
- Implement P0 before optional P1 and P2 conveniences.
- Keep README English and Chinese current-surface descriptions aligned.
- Keep real-model LLM E2E opt-in instead of default CI gating.
- Do not add heavy planner, vector search, cross-session memory, or a full authorization platform.

## Phase 1: Design

- Define the shared next-action shape.
- Define safe URI hint rules for `search_metadata` and metadata resource navigation.
- Define the output-schema alignment checklist for the seven core tools.
- Define the recovery envelope fields for the five common model errors.
- Define the capabilities contract assertions.
- Define the descriptor lint rule set and failure messages.
- Define compact example placement and shape for P1 only.
- Define approval-step, workflow side-effect scope, and stale-plan recovery wording for P1 only.
- Define startup, client-configuration, registry/package, and troubleshooting documentation boundaries for P2 only.

### Phase 1 Design Rules

- Prefer small response-builder or descriptor changes over new framework layers.
- Prefer descriptor/catalog tests over broad runtime snapshots.
- Prefer current-session state over new persistence.
- Prefer explicit `not_safe_to_derive` or ask-user guidance over guessed metadata URIs.
- Defer any design that needs planner state, vector ranking, cross-session memory, or a complete authorization model.

## Phase 2: Implementation Strategy

1. Align current documentation and descriptor wording first, because it is low risk and clarifies the target contract.
2. Standardize `next_actions`, reusable preview arguments, and common recovery fields in existing response builders.
3. Add direct `search_metadata` resource URI hints and align core output schemas.
4. Add descriptor lint and the capabilities contract test.
5. Add P1 examples, navigation hints, completion tuning, algorithm property templates, approval-step wording, side-effect scopes, and stale-plan recovery only after P0 is stable.
6. Extend P2 startup, client-configuration, troubleshooting, registry/package hints, and opt-in LLM usability coverage only after deterministic tests protect the basic surface.

## Complexity Tracking

No constitution violations are expected.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |
