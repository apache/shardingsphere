# Research: ShardingSphere MCP Direct Multi-Database Runtime

## Decision 1: Keep the MCP public contract and change only the runtime topology

- **Decision**: Reuse the MCP V1 public resources, tools, result models, and error surface from `001-shardingsphere-mcp`, and limit this feature to direct multi-database runtime topology changes.
- **Rationale**:
  - The current user request is about backend connectivity and routing scope, not about redesigning the MCP public surface.
  - Existing clients already pass `database` as the route key, so the new value can be delivered without inventing a new client contract.
  - Keeping the public contract stable reduces migration risk and keeps this feature aligned with the repository's smallest-safe-change principle.
- **Alternatives considered**:
  - Introduce new direct-runtime-specific tools or result fields: rejected because it would split the MCP public contract and force clients into topology-specific behavior.
  - Redesign discovery resources to expose transport or connection details: rejected because those are operator concerns, not MCP domain objects.

## Decision 2: Use explicit logical database bindings instead of reviving flat multi-db properties

- **Decision**: Model direct multi-database runtime as an explicit topology of logical database bindings instead of extending legacy flat `runtime.props` keys such as `databases.<name>.*`.
- **Rationale**:
  - Explicit bindings are easier to validate for uniqueness, capability overrides, schema defaults, and redaction rules.
  - A topology model matches the domain language already used by the MCP public contract, which exposes logical databases as first-class route keys.
  - Bringing back flat prefixed keys would preserve implementation convenience but weaken readability and traceability.
- **Alternatives considered**:
  - Re-enable `databases.<name>.*` in flat properties: rejected because it is hard to validate, review, and document cleanly.
  - Infer logical database bindings dynamically from reachable backends at startup: rejected because operator intent and stable naming would become ambiguous.

## Decision 3: Keep V1 startup fail-fast, but isolate runtime failures after startup per logical database

- **Decision**: Startup remains all-or-nothing for configured databases, while runtime failures after successful startup are isolated per logical database.
- **Rationale**:
  - Fail-fast startup prevents a service from advertising a partially valid topology as ready.
  - Once startup succeeds, isolating later backend failures per logical database keeps healthy databases usable and matches the user's multi-database availability goal.
  - This split keeps V1 behavior deterministic while still avoiding total service collapse for later single-database outages.
- **Alternatives considered**:
  - Allow partial startup success by default: rejected because it makes `list_databases` and capability exposure ambiguous at boot time.
  - Collapse the whole service whenever any runtime backend later becomes unavailable: rejected because it defeats the point of isolating multiple direct database bindings.

## Decision 4: Serve the last successful metadata snapshot during transient backend outages

- **Decision**: If a logical database becomes temporarily unavailable after successful startup,
  keep serving its last successful metadata snapshot for read-only discovery requests until the
  next successful refresh.
- **Rationale**:
  - Discovery clients still benefit from stable object facts even when the backend is briefly unreachable.
  - This avoids turning a transient runtime failure into an empty catalog or a contract-breaking disappearance of the logical database.
  - It aligns with the requirement that only live backend access should return `unavailable`, while read-only metadata remains reviewable.
- **Alternatives considered**:
  - Drop the logical database from `list_databases` immediately: rejected because route keys would flicker and break client expectations.
  - Replace failed metadata with an empty snapshot: rejected because it silently destroys previously valid object visibility.

## Decision 5: Keep single-database transaction semantics and reject cross-database federation

- **Decision**: Preserve the existing rule that an active transaction binds one logical database,
  and do not introduce cross-database transactions, savepoints, or SQL federation in V1 direct
  multi-database mode.
- **Rationale**:
  - The current MCP runtime, session model, and result contract are all shaped around one-database execution requests.
  - Cross-database transaction management or federated SQL would expand scope far beyond the user's request and create new correctness, consistency, and failure-recovery problems.
  - Explicitly rejecting these semantics keeps behavior safe and predictable.
- **Alternatives considered**:
  - Add best-effort cross-database transactions: rejected because partial commit and rollback behavior would be unsafe.
  - Allow backend-specific federation when a driver happens to support it: rejected because it would leak backend-specific semantics into a unified MCP contract.

## Decision 6: Refresh metadata only for the affected logical database after DDL or DCL

- **Decision**: Metadata refresh after committed DDL or DCL applies only to the target logical database and must not trigger full-topology replacement.
- **Rationale**:
  - Direct multi-database runtime can involve several independent backends; refreshing every backend on one database change would be slow and operationally noisy.
  - Per-database replacement preserves isolation and makes refresh behavior consistent with the public `database` routing boundary.
  - This is the clearest way to satisfy the requirement that unrelated databases keep their snapshots untouched.
- **Alternatives considered**:
  - Refresh the full topology after every metadata-affecting change: rejected because it adds avoidable latency and broadens failure blast radius.
  - Skip refresh entirely and rely on eventual manual restart: rejected because it breaks the published visibility SLA.

## Decision 7: Make secret redaction part of the feature contract, not a logging afterthought

- **Decision**: Treat secret redaction and credential-safe diagnostics as a first-class requirement for direct multi-database runtime.
- **Rationale**:
  - Direct backend connectivity introduces connection locators, credentials, drivers, and operator diagnostics that were not as visible in the original ShardingSphere-backed assumption set.
  - Specifying this now avoids accidental leakage through startup errors, audit records, or README examples.
  - The repository governance explicitly requires security boundaries to be visible and reviewable.
- **Alternatives considered**:
  - Leave redaction to implementation details: rejected because reviewers would not have a stable contract to validate.
  - Hide all diagnostics entirely: rejected because operators still need actionable failure messages.

## Decision 8: Keep the supported database scope aligned with the existing V1 capability registry

- **Decision**: Direct multi-database mode may mix multiple database types only from the existing V1 supported set.
- **Rationale**:
  - The current capability matrix already defines supported transaction and object boundaries for a stable set of database types.
  - Reusing that set keeps direct multi-database behavior aligned with the baseline MCP contract and existing tests.
  - Expanding the database set at the same time would create a second independent axis of change.
- **Alternatives considered**:
  - Allow any JDBC-accessible database type: rejected because the MCP capability guarantees would become under-specified.
  - Restrict V1 direct mode to one database type only: rejected because it would under-deliver the main user value of this feature.
