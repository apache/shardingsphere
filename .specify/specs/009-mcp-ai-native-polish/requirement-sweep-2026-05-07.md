# Requirement Sweep 2026-05-07: MCP AI-Native Polish

**Branch observed**: `001-shardingsphere-mcp`
**Branch rule**: Do not switch, create, or check out branches.
**Purpose**: Capture the final repeated-question requirement pass for making `shardingsphere-mcp` native, convenient, comfortable, and clear for large models without over-design.

## Decision Rules

- Preserve the existing descriptor-backed MCP surface when it already satisfies a requirement.
- Convert only concrete model-guessing gaps into requirements.
- Prefer compact payload contracts, descriptor/schema clarity, deterministic tests, and documentation checks.
- Reject system-sized additions such as planners, vector search, semantic ranking, cross-session memory, approval-token platforms, RBAC, and default-CI live-model suites.
- Treat this document as a requirement reconciliation layer over `spec.md`, `final-requirement-inventory.md`, and `implementation-task-breakdown.md`.

## P0 Requirements

### RS-P0-01: Compact first-hop entry

Capabilities MUST keep a compact first-hop summary that tells a model the safe entry resource, public tools, main flows, and preview-before-approval rule.

Acceptance:

- If `surface_summary` already exists, preserve it as the compact entry contract.
- The summary stays short and descriptor-backed.
- It does not introduce historical `list_*` or `describe_*` aliases.
- It does not become a planner or tutorial.

### RS-P0-02: Direct metadata resource size control

Direct metadata list resources SHOULD avoid unbounded context expansion by paginating, capping, or pointing the model to `search_metadata` with safe narrowing arguments.

Acceptance:

- Direct list responses expose count, truncation or pagination state, and continuation guidance when broad.
- `search_metadata` remains the preferred broad discovery tool.
- Zero-row SQL results remain valid SQL results, not metadata empty-state errors.

### RS-P0-03: SQL row-limit semantics

`execute_query.max_rows` MUST have schema-visible and README-visible semantics, including the behavior for omitted values and `0`.

Acceptance:

- Either `0` is rejected or documented as "use server default"; it must not be ambiguous.
- Default and maximum row caps are machine-readable where the descriptor owns the schema.
- Truncation tells the model whether to narrow the query rather than retry blindly.

### RS-P0-04: Side-effect preview limits

Side-effect previews MUST say they classify SQL or workflow side-effect scope; they MUST NOT imply affected-row estimates or hidden execution.

Acceptance:

- Preview payloads expose stable response mode.
- `affected_rows_estimated=false` or equivalent wording is explicit when estimates are not available.
- Follow-up execution remains blocked on user approval.

### RS-P0-05: Workflow argument conflict handling

Workflow planning SHOULD detect conflicting duplicated public arguments, such as top-level fields that disagree with `user_overrides` values.

Acceptance:

- If both forms are accepted and equivalent, conflicts return structured clarification instead of silent precedence.
- Recovery names public argument paths.
- No private Java field names or synthetic algorithm-property paths leak into user-facing recovery.

### RS-P0-06: Workflow preview review focus

Workflow plan and apply preview SHOULD expose a compact review focus so the model can ask the user to inspect the right artifacts.

Acceptance:

- Review focus includes artifact categories, side-effect scope, manual-only status when relevant, and approval requirement.
- It avoids duplicating full DistSQL or DDL bodies when those bodies already exist in artifact fields.
- It remains current-session scoped and does not become an approval record.

### RS-P0-07: Documentation drift guard

Current user-facing and Speckit documents MUST not contradict the implemented public MCP surface.

Acceptance:

- Historical PRD/design documents stay clearly marked as historical.
- Current README, descriptors, and Speckit handoff agree on public resources such as workflow plan read-back.
- Documentation contract checks cover removed legacy aliases and high-risk stale guidance.

### RS-P0-08: Field naming contract

Capabilities SHOULD explain field naming conventions so models do not mistake MCP-owned fields and ShardingSphere payload fields for aliases.

Acceptance:

- MCP protocol fields, descriptor-derived fields, and ShardingSphere structured payload fields are distinguishable.
- No duplicate camelCase/snake_case aliases are added solely for compatibility.
- Payload cleanup keeps one canonical field where ShardingSphere owns the contract.

## P1 Requirements

### RS-P1-01: Single-candidate completion inference

Completion MAY infer missing context only when there is exactly one safe candidate.

Acceptance:

- Inferred values are marked as server-defaulted or equivalent provenance.
- Multi-candidate cases return schema-first or context-first next actions.
- Completion references and resume targets remain public MCP names.

### RS-P1-02: Capability-gated native elicitation

MCP-native elicitation SHOULD mirror structured clarification for required inputs when the SDK and client advertise support.

Acceptance:

- Structured JSON clarification remains the canonical fallback.
- Side-effect approval can be represented through native UI only as a mirror of preview approval, not as hidden authorization.
- Transport-specific SDK types stay at the bootstrap boundary.

### RS-P1-03: Secret-free runtime readiness

Runtime status SHOULD summarize readiness without exposing secrets.

Acceptance:

- Include active transport, safe logical database names or count, feature availability, and first-check resources.
- Call out obvious Proxy-vs-physical-database workflow mismatch only as a hint.
- Do not expose JDBC credentials, bearer tokens, raw env values, or stack traces.

### RS-P1-04: Result payload token control

SQL results SHOULD avoid unnecessary duplication when positional rows and object-shaped rows would produce large repeated payloads.

Acceptance:

- Positional rows remain canonical for correctness.
- Object-shaped rows appear only when column labels are unique and useful.
- Large results may omit duplicate convenience views or expose a future `result_format` option without breaking correctness.

### RS-P1-05: Resource hint parity

Typed resource hints SHOULD remain canonical across tool results, resource results, and recoverable errors; protocol-native `ResourceLink` content stays additive.

Acceptance:

- JSON `structuredContent` is sufficient even when clients ignore native resource links.
- Resource hints carry URI, resource kind, purpose, reason, and source field where known.
- Resource-read errors remain machine-distinguishable through `response_kind=error`.

### RS-P1-06: Specific SQL/JDBC recovery categories

SQL and runtime failures SHOULD classify common safe categories when possible.

Acceptance:

- Categories may include missing JDBC driver, authentication failure, connection timeout, database unavailable, unsupported SQL, and wrong SQL tool.
- Recovery never exposes secrets or raw stack traces.
- Generic SQL exceptions still return safe next actions where possible.

### RS-P1-07: Terminal stop clarity

Terminal responses SHOULD make it clear when no further MCP call is needed.

Acceptance:

- SQL terminal responses and completed workflow states may include `stop`.
- Resource detail responses can omit `stop` when typed next resources are more useful.
- The contract avoids payload bloat by using terminal hints only where they reduce repeated exploration.

### RS-P1-08: Deterministic MCP client smoke

A non-LLM MCP client smoke SHOULD validate the public contract over STDIO and HTTP when implementation changes touch transport behavior.

Acceptance:

- Smoke covers initialize, capabilities/runtime read, one metadata search, one bounded read-only query, completion, and a preview-only side-effect path.
- It remains deterministic and credential-free by default.
- It does not replace scoped module tests.

## P2 Requirements

### RS-P2-01: Distribution handoff examples

Distribution docs SHOULD include minimal STDIO, HTTP, Docker bind-host, and bearer-token examples without embedding real secrets.

Acceptance:

- Examples use placeholders or environment variables.
- The registry or package metadata stays release-neutral unless the release surface is official.

### RS-P2-02: Token-safe health-check recipe

Docs SHOULD keep one short health-check sequence for models and users.

Acceptance:

- The recipe uses initialize, runtime, capabilities, and optionally a bounded read-only query.
- It does not require new endpoints or live-model credentials.

### RS-P2-03: Capability fingerprint usability

Capability fingerprints SHOULD remain available so a model or deterministic smoke can detect public-surface drift.

Acceptance:

- Fingerprints do not include secrets.
- Runtime may reference the current capability fingerprint if useful, but capabilities remain the source of truth.

### RS-P2-04: Descriptor authoring lint

New MCP feature descriptors SHOULD fail fast when they omit model-critical metadata.

Acceptance:

- Checks cover tool schema, output schema, prompt, completion target, resource navigation, annotations, examples, and side-effect metadata where applicable.
- The check stays descriptor-level and does not call a model.

### RS-P2-05: Optional request correlation

Bounded request correlation remains optional and should be added only if it helps local triage without creating audit, approval, identity, or persistence meaning.

Acceptance:

- If omitted, recovery remains category and next-action based.
- If added later, identifiers are generated per response, secret-free, and short-lived.

## Explicit Non-Goals

- No auto planner, graph traversal, vector search, semantic ranking, or model-call ranking.
- No cross-session memory or user behavior learning.
- No durable approval tokens, RBAC, tenant isolation, production approval workflow, migration/backfill, or rollback orchestration.
- No `list_*` or `describe_*` compatibility tool matrix.
- No affected-row estimation through SQL rewriting inside preview.
- No default-CI live-model benchmark.

## Verification Map

- Documentation-only changes: run `git diff --check`.
- Branch rule: confirm `git branch --show-current` before reporting.
- Future Java or descriptor changes: run scoped MCP tests and scoped Checkstyle for touched modules.
- Future documentation cleanup: search for stale historical aliases and conflicting workflow-resource guidance.
