# Research: MCP AI-Native Polish

## Decision 1: Add capability contracts, not a planner

**Decision**: Put next-action vocabulary and common task flows into `shardingsphere://capabilities`.

**Rationale**: Capabilities is already the current model-facing discovery entry point. Adding static contracts there helps models start and continue safely without adding runtime orchestration.

**Alternatives considered**:

- Add a planning tool that chooses the next call. Rejected as over-design.
- Put all guidance only in README. Rejected because protocol-visible guidance is more useful to MCP clients.

## Decision 2: Keep common flows short and static

**Decision**: Common flows should be short recipes that reference existing resources and tools.

**Rationale**: Models need a first hop, not a hidden workflow engine. Long tutorials belong in README.

**Alternatives considered**:

- Encode full workflows with conditional branches in capabilities. Rejected because that becomes a planner.
- Omit flows entirely. Rejected because first-call uncertainty remains a model comfort gap.

## Decision 3: Explain search matches without ranking engines

**Decision**: Add cheap match explanations such as exact, prefix, or contains, plus matched fields.

**Rationale**: The server already scans names and knows which fields are compared. Exposing that fact helps model selection without vector search.

**Alternatives considered**:

- Add semantic search or vector ranking. Rejected because it is beyond the lightweight scope.
- Keep search results as bare hits. Rejected because ambiguity remains when multiple tables or columns match.

## Decision 4: Prefer not-safe URI behavior over guessed encoding

**Decision**: If URI names cannot be safely represented by existing resource patterns, return no guessed URI and explain why.

**Rationale**: A wrong URI creates failed tool calls and model confusion. Conservative non-derivation is easier to recover from.

**Alternatives considered**:

- Percent-encode every name immediately. Deferred until current clients and resource matching behavior are rechecked.
- Return raw names even when unsafe. Rejected because it creates invalid or ambiguous URI strings.

## Decision 5: Expose applied defaults or structured recovery for numeric arguments

**Decision**: Numeric fields should either reject malformed input with recovery or expose applied defaults so the model knows what happened.

**Rationale**: Silent defaults are convenient for humans but confusing for models trying to explain results.

**Alternatives considered**:

- Keep current defaulting behavior without payload hints. Rejected because models must infer server behavior.
- Reject every omitted numeric argument. Rejected because omission is a valid ergonomic path.

## Decision 6: Add parse hints before adding new outputs

**Decision**: Improve existing SQL and workflow outputs with counts, status vocabulary, nested item schemas, and normalized request summaries.

**Rationale**: The model already receives the necessary payloads. The gap is clarity, not missing operations.

**Alternatives considered**:

- Add new introspection tools for SQL and workflow outputs. Rejected because descriptors and payloads can carry the needed hints.
- Add large golden snapshots. Rejected because focused shape tests are easier to maintain.

## Decision 7: Server-owned approval summary is enough

**Decision**: Add short approval summaries or questions to preview responses, but do not add approval tokens or durable approval records.

**Rationale**: The immediate problem is model paraphrasing risk. Tokens and approval records introduce state, expiry, replay, and policy questions.

**Alternatives considered**:

- Add preview or approval tokens now. Deferred as a future security hardening feature.
- Leave approval wording entirely to the model. Rejected because risk wording should come from server-known side-effect facts.

## Decision 8: Protect with focused deterministic tests

**Decision**: Add section/shape tests for capabilities, search response fields, schema item shapes, and approval summaries.

**Rationale**: The new fields are model-visible contracts. They should be testable without live model services.

**Alternatives considered**:

- Add a broad live-model matrix. Rejected because it is slow, flaky, and outside default CI.
- Add full MCP transcript golden files. Deferred because they are noisy for this small polish increment.
