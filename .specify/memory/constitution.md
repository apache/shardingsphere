# ShardingSphere MCP Constitution

## Core Principles

### Proxy-First Logical Abstraction

All product behavior in this initiative MUST target ShardingSphere-Proxy and MUST present a logical database view to users. Physical tables, derived columns, and DistSQL details are implementation artifacts that may be surfaced for review, but they are never the primary user-facing mental model.

### Explicit Operator Control

Any action that can change schema or rules MUST be reviewable before execution. The system MUST present a step list, preview the generated SQL and DistSQL, and respect the operator-selected execution mode rather than silently applying changes.

### Minimal Safe Automation

V1 MAY automate physical DDL generation and rule lifecycle changes, but it MUST NOT backfill data, run migration jobs, or infer destructive behavior. Physical column type decisions MUST follow ShardingSphere default type strategy rather than custom MCP-side type inference. No rollback or audit persistence is required in V1, so execution scope must stay narrow and explicit.

### Deterministic Naming and Transparent Changes

Generated names for derived columns and related artifacts MUST be deterministic. Existing physical columns are not automatically reused just because names overlap with generated defaults. If naming conflicts occur, the system MUST create a new generated name using numeric suffixes and MUST return the final generated names to the operator before or during execution summary.

### Complete Verification Before Completion

Work is not complete when SQL or DistSQL has been emitted. A completed flow MUST validate four layers when applicable: physical DDL state, rule state, logical metadata state, and SQL executability from the logical view. V1 validation does not require historical data migration or query-result correctness checks against real payloads.

## Product Boundaries

- Scope is limited to ShardingSphere-Proxy.
- V1 focuses on single database, single table, single column scenarios first.
- Database context MUST be explicit; the workflow cannot depend on session `USE`.
- V1 must support create, alter, and drop flows for encrypt and mask rules.
- Data migration, historical data backfill, rollback orchestration, and audit persistence are out of scope for V1.

## Delivery Standards

- Specifications drive plans, and plans drive tasks; implementation should not precede reviewed requirements.
- All future implementation work under this initiative MUST honor repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md`.
- Changes should remain traceable, minimal, and aligned with existing ShardingSphere architecture and DistSQL capabilities.

## Governance

- Repository-level instructions take precedence over this constitution if conflicts arise.
- Changes to this constitution MUST preserve Proxy-first scope, explicit operator control, and the V1 no-data-migration boundary unless stakeholders explicitly revise them.

**Version**: 1.0.0 | **Ratified**: 2026-04-17 | **Last Amended**: 2026-04-17
