# Research: MCP AI-Friendly Lightweight Experience

## Decision 1: Keep the current resource-first public surface

**Decision**: Treat resource-first metadata discovery plus `search_metadata`, `execute_query`, `execute_update`, and workflow tools as the current public MCP contract.

**Rationale**: The implementation already exposes descriptor-backed resources, resource templates, prompts, completion targets, navigation, and capabilities.
Adding a full parallel `list_*` tool family now would duplicate the surface and increase maintenance cost.

**Alternatives considered**:

- Add compatibility `list_*` tools immediately. Rejected for this increment because it expands public API area before documentation and descriptor consistency are fixed.
- Replace resources with tools. Rejected because the current MCP design intentionally uses resources as context surfaces and tools as action surfaces.

## Decision 2: Standardize guidance before adding new runtime features

**Decision**: First normalize `next_actions` and common recovery metadata, and remove legacy recommendation-field aliases from the active contract.

**Rationale**: Models fail most often when the next step is ambiguous. A consistent action shape improves existing tools without changing the protocol stack.

**Alternatives considered**:

- Add a workflow planner or automatic traversal service. Rejected as over-design for the current ask.
- Leave guidance as prose. Rejected because prose-only guidance is harder for models to follow and harder to test.

## Decision 3: Keep preview semantics lightweight

**Decision**: Require preview-before-execute guidance and reusable suggested arguments, but do not introduce preview tokens or approval tokens in this increment.

**Rationale**: Preview tokens can be valuable, but they add state, expiry, replay, and compatibility questions.
The user explicitly asked to avoid over-design. The current gap can be reduced by making preview output easier to reuse safely.

**Alternatives considered**:

- Add `preview_id` and force execute to reference it. Deferred as a possible future hardening step.
- Rely only on tool descriptions. Rejected because response-level guidance is more actionable for model callers.

## Decision 4: Limit recovery scope to common model mistakes

**Decision**: Cover missing database, missing execution mode, wrong SQL tool, unknown tool/resource, and unavailable plan id first.

**Rationale**: These are frequent and cheap to test. A full error taxonomy can wait until the common path is stable.

**Alternatives considered**:

- Build a complete protocol error ontology. Rejected as larger than needed.
- Do nothing because some recovery fields already exist. Rejected because the fields are not yet fully consistent across common mistakes.

## Decision 5: Return direct resource URIs from metadata search where safe

**Decision**: Add direct `resource_uri` hints to `search_metadata` hits only when the URI can be derived from descriptor-backed resource patterns and known logical metadata.

**Rationale**: Search is a model's fastest path from vague user intent to a precise metadata object.
Without a URI, the model must manually reconstruct a resource path and can easily guess wrong.

**Alternatives considered**:

- Add a complete `list_*` and `describe_*` tool matrix. Rejected because it duplicates the resource-first contract.
- Return guessed best-effort URIs. Rejected because wrong URIs are worse than asking the model to read capabilities or parent resources.

## Decision 6: Treat output schema drift as a P0 usability defect

**Decision**: Align descriptor-visible output schemas with real payloads for the seven core tools before adding broad new behavior.

**Rationale**: Models rely on schemas to choose fields and retry calls. A schema that is protocol-valid but behaviorally stale still causes model confusion.

**Alternatives considered**:

- Document mismatches only in README. Rejected because descriptors are the protocol-visible contract.
- Add broad golden snapshots. Deferred because small schema-contract checks are enough for this increment.

## Decision 7: Use minimal descriptor lint

**Decision**: Add deterministic descriptor checks for only the most visible model-facing regressions:
empty descriptions, placeholders, missing side-effect hints, missing enum values, missing core output fields, and broken navigation references.

**Rationale**: Descriptor quality is now product behavior. A small lint catches obvious regressions without requiring real model calls.

**Alternatives considered**:

- Build a natural-language quality scorer. Rejected because it would be nondeterministic and excessive.
- Use only review discipline. Rejected because descriptor regressions are easy to miss in code review.

## Decision 8: Protect capabilities with a shape contract, not a large golden snapshot

**Decision**: Add a lightweight contract test that asserts core sections exist and remain shaped for model discovery.

**Rationale**: Full transcript golden snapshots are useful, but they can be noisy. A shape contract is smaller and enough for this increment.

**Alternatives considered**:

- Full protocol transcript golden suite. Deferred until the lightweight contract proves stable.
- No contract test. Rejected because capabilities is the model's best single discovery entry point.

## Decision 9: Keep real-model E2E opt-in

**Decision**: Add only a few high-value scenarios and keep them outside default CI.

**Rationale**: Real-model tests are valuable but can be slow, flaky, and environment-dependent. They should validate comfort, not block basic deterministic builds.

**Alternatives considered**:

- Run real-model tests by default. Rejected because default CI must stay deterministic and credential-free.
- Remove real-model tests. Rejected because they provide direct evidence that the surface is usable by an actual model.

## Decision 10: Keep P1 and P2 as comfort layers, not blockers

**Decision**: Metadata navigation hints, compact examples, completion tuning, algorithm property templates, approval-step wording,
workflow side-effect scope, stale-plan recovery, startup diagnostics, client configuration examples, troubleshooting docs,
registry hints, and LLM usability scenarios stay behind P0.

**Rationale**: These are useful, but the first-order model failures are surface mismatch, unclear next action, missing URI hints, schema drift, unsafe preview follow-up, and weak recovery.

**Alternatives considered**:

- Build all improvements in one large pass. Rejected because it increases review risk and hides the minimum useful change.
- Drop P1/P2 completely. Rejected because they are cheap, concrete follow-ups once P0 is protected by tests.

## Decision 12: Defer protocol-native and runtime-management ideas

**Decision**: Do not add current-session workflow list resources, metadata freshness semantics, environment-variable config references,
runtime status resources, preview tokens, or MCP-native elicitation/progress/sampling/roots in this increment.

**Rationale**: These ideas may be useful later, but the current model-use pain is simpler: the model needs clear next actions,
examples, algorithm properties, approval values, side-effect scope, first-use client setup, and safe recovery.

**Alternatives considered**:

- Add workflow listing and freshness fields now. Rejected because current `plan_id` return, completion, and recovery cover the main session path.
- Add environment-variable config references now. Rejected because it is operational security convenience rather than model-native MCP usability.
- Add MCP-native elicitation or progress now. Deferred until SDK/client support is stable and a concrete use path proves it is needed.

## Decision 11: Make code re-analysis a gate, not a scope expansion loop

**Decision**: Before implementing each P0 item, record current behavior evidence, affected paths, verification mapping, explicit non-goals,
and rollback boundary. This re-analysis is a gate to prevent guessing, not a new request to expand requirements.

**Rationale**: The MCP surface already has descriptors, resources, tools, prompts, completions, workflow state, and recovery behavior.
Implementation should adapt to those facts instead of re-opening product scope or asking the user to re-confirm decisions that are already fixed.

**Alternatives considered**:

- Ask the user again whenever a code detail is unclear. Rejected because the code can answer implementation facts better than the user.
- Skip re-analysis and implement from the requirement text only. Rejected because schema, recovery, and descriptor behavior can drift from docs.
- Produce a large architecture document before coding. Rejected because the user asked to avoid over-design.
