# Research: MCP AI-Native Polish

## Decision 1: Treat current capability contracts as baseline

**Decision**: Do not re-spec `next_action_contract`, `common_flows`, payload contracts, security hints, and fingerprints as new work. They are current baseline behavior.

**Rationale**: Current code already exposes these sections through `shardingsphere://capabilities` and tests protect their shape. Reopening them would make 009 drift from the repository state.

**Alternatives considered**:

- Repeat 008-style capability contract work in 009. Rejected because it is already implemented.
- Remove capability guidance from this package. Rejected because the next polish still builds on those sections.

## Decision 2: Add descriptive action ordering, not orchestration

**Decision**: Add order/dependency metadata to multi-step `next_actions` only where a response already implies sequence.

**Rationale**: Models need to know "ask user first, then call tool." They do not need a new planner or executor.

**Alternatives considered**:

- Add a planning tool that executes flow logic. Rejected as over-design.
- Leave action order implicit in list order. Rejected because model context compaction and clients may not preserve the intended semantics clearly.

## Decision 3: Add a compact surface summary above the rich catalog

**Decision**: Add `surface_summary` to capabilities with first resources, current public tools, flow names, and safety rules.

**Rationale**: Capabilities is the source of truth but can be large. A summary improves first-hop behavior without duplicating long README content.

**Alternatives considered**:

- Add another resource such as `shardingsphere://summary`. Rejected because capabilities is already the discovery entry point.
- Put summary only in README. Rejected because models commonly start from protocol-visible resources.

## Decision 4: Type navigation and completion hints locally

**Decision**: Add lightweight source/target kind hints to resource navigation and completion availability hints near fields or arguments where possible.

**Rationale**: The current catalog is accurate but requires cross-section joins. Local hints reduce small model mistakes without duplicating every descriptor.

**Alternatives considered**:

- Build a graph traversal engine. Rejected as over-design.
- Duplicate the full completion catalog under each tool. Rejected as noisy and drift-prone.

## Decision 5: Explain empty and not-found states

**Decision**: Add compact empty-state and not-found hints when the server can safely know why a response has no items.

**Rationale**: Count fields are already visible. Models still need to know whether zero means success, no match, unsupported capability, or missing scope.

**Alternatives considered**:

- Treat all empty states as success with `count=0`. Rejected because models may misreport or invent follow-ups.
- Add broad diagnostic resources. Rejected because small response hints are enough.

## Decision 6: Mark argument provenance instead of adding approval tokens

**Decision**: Mark reusable argument provenance using values such as user-provided, server-normalized, server-generated, server-defaulted, and redacted.

**Rationale**: The immediate problem is safe copying. Tokens and durable approvals introduce policy and state concerns beyond this increment.

**Alternatives considered**:

- Add preview or approval tokens. Rejected for this increment.
- Leave provenance to prose. Rejected because models can rewrite server-owned values.

## Decision 7: Keep runtime diagnostics safe and bounded

**Decision**: Improve JDBC/config/connection recovery categories and optionally add bounded request or trace identifiers only if they do not create persistent state or leak secrets.

**Rationale**: Better triage helps first-use and support workflows. The scope should not become an observability platform.

**Alternatives considered**:

- Add tracing, metrics exporters, or log aggregation. Rejected as outside this polish increment.
- Expose raw exception payloads. Rejected because they may contain sensitive environment details.

## Decision 8: Improve opt-in usability metrics, not default CI

**Decision**: Add next-action-follow and approval-violation metrics to the existing opt-in LLM usability lane.

**Rationale**: These metrics directly measure the desired model comfort and safety behavior. They provide more signal than expanding a broad scenario matrix.

**Alternatives considered**:

- Add a broad live-model benchmark matrix. Rejected because it is slow, flaky, and outside default CI.
- Make live-model tests mandatory. Rejected because default CI must remain deterministic and credential-free.

## Decision 9: Bound broad model calls before adding smarter search

**Decision**: Add explicit row, timeout, page-size, page-token, and blank-query contracts before considering richer metadata search behavior.

**Rationale**: The immediate model risk is accidental unbounded work or malformed pagination. Defaults, caps, and structured recovery solve that risk with low complexity.

**Alternatives considered**:

- Add semantic search or a broad metadata exploration engine. Rejected as over-design.
- Keep silent defaulting for malformed numbers. Rejected because models need repairable errors.

## Decision 10: Use structured clarification with native elicitation as an upgrade path

**Decision**: Represent missing inputs as field-level clarification questions and map them to MCP-native elicitation only when the client/SDK path supports it.

**Rationale**: Field-level shape helps all clients. Native elicitation improves supported clients without removing fallback compatibility.

**Alternatives considered**:

- Replace fallback `pending_questions` immediately. Rejected because not every client path supports elicitation.
- Keep prose-only questions. Rejected because models must infer field identity, type, choices, and secrecy.

## Decision 11: Support common Chinese workflow intent without building an NLP parser

**Decision**: Add deterministic synonym coverage and descriptor guidance for common Chinese encrypt/mask intents, while encouraging models to pass structured intent evidence.

**Rationale**: The user-facing experience is multilingual. A small vocabulary handles frequent cases without moving language understanding from the model into MCP.

**Alternatives considered**:

- Build a full natural-language parser in MCP. Rejected as over-design.
- Depend entirely on English keyword matching. Rejected because Chinese examples and users are expected.

## Decision 12: Add current-session read-back, not cross-session memory

**Decision**: Add a plan-status snapshot resource or equivalent current-session read-back for `plan_id`.

**Rationale**: Models often lose context after compaction. Current-session read-back is enough for safe continuation and does not create durable memory or audit concerns.

**Alternatives considered**:

- Add cross-session workflow memory. Rejected as outside this increment.
- Require the model to replan after every context loss. Rejected because it wastes user time and can drift from approved artifacts.

## Decision 13: Improve packaging and runtime comfort with secret-safe hints

**Decision**: Add env-placeholder configuration expectations, compact runtime status, HTTP bearer-token hints, and minimal client examples.

**Rationale**: First-use failures are common. Secret-safe hints improve setup while preserving the existing static-token and local-runtime posture.

**Alternatives considered**:

- Add full OAuth, RBAC, or tenant policy. Rejected as out of scope.
- Keep secrets directly in sample YAML. Rejected because MCP examples are likely copied into real local configs.
