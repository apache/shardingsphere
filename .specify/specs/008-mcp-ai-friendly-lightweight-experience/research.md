# Research: MCP AI-Friendly Lightweight Experience

## Decision 1: Keep the current resource-first public surface

**Decision**: Treat resource-first metadata discovery plus `search_metadata`, `execute_query`, `execute_update`, and workflow tools as the current public MCP contract.

**Rationale**: The implementation already exposes descriptor-backed resources, resource templates, prompts, completion targets, navigation, and capabilities.
Adding a full parallel `list_*` tool family now would duplicate the surface and increase maintenance cost.

**Alternatives considered**:

- Add compatibility `list_*` tools immediately. Rejected for this increment because it expands public API area before documentation and descriptor consistency are fixed.
- Replace resources with tools. Rejected because the current MCP design intentionally uses resources as context surfaces and tools as action surfaces.

## Decision 2: Standardize guidance before adding new runtime features

**Decision**: First normalize `next_actions`, recommended tool naming, and common recovery metadata.

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

## Decision 5: Use minimal descriptor lint

**Decision**: Add deterministic descriptor checks for only the most visible model-facing regressions:
empty descriptions, placeholders, missing side-effect hints, missing enum values, missing core output fields, and broken navigation references.

**Rationale**: Descriptor quality is now product behavior. A small lint catches obvious regressions without requiring real model calls.

**Alternatives considered**:

- Build a natural-language quality scorer. Rejected because it would be nondeterministic and excessive.
- Use only review discipline. Rejected because descriptor regressions are easy to miss in code review.

## Decision 6: Protect capabilities with a shape contract, not a large golden snapshot

**Decision**: Add a lightweight contract test that asserts core sections exist and remain shaped for model discovery.

**Rationale**: Full transcript golden snapshots are useful, but they can be noisy. A shape contract is smaller and enough for this increment.

**Alternatives considered**:

- Full protocol transcript golden suite. Deferred until the lightweight contract proves stable.
- No contract test. Rejected because capabilities is the model's best single discovery entry point.

## Decision 7: Keep real-model E2E opt-in

**Decision**: Add only a few high-value scenarios and keep them outside default CI.

**Rationale**: Real-model tests are valuable but can be slow, flaky, and environment-dependent. They should validate comfort, not block basic deterministic builds.

**Alternatives considered**:

- Run real-model tests by default. Rejected because default CI must stay deterministic and credential-free.
- Remove real-model tests. Rejected because they provide direct evidence that the surface is usable by an actual model.
