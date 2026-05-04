<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Research: AI-Friendly MCP Experience Hardening

## Decision 1: Make Transcript Golden Tests the First Guard

**Decision**: Add normalized transcript golden tests before further model-comfort enhancement.

**Rationale**: Prompt, completion, resource, and tool descriptors are now protocol-visible.
A small descriptor edit can silently remove model-facing context without breaking handler tests.
Golden tests provide a stable regression guard for the public MCP surface.

**Alternatives considered**:

- Rely on descriptor unit tests only. Rejected because unit tests rarely capture the full protocol payload a client sees.
- Snapshot every runtime resource. Rejected because large dynamic payloads would be brittle and hard to review.

## Decision 2: Keep Real-Model E2E Opt-In

**Decision**: Add real-model E2E with strong trace assertions, but keep it outside default CI.

**Rationale**: Actual model behavior is the final usability signal.
However, model calls are credential-dependent, network-dependent, costly, and nondeterministic enough that default CI must remain local and deterministic.

**Alternatives considered**:

- Put real-model calls in PR CI. Rejected due to nondeterminism, external credentials, and cost.
- Omit real-model tests entirely. Rejected because deterministic tests cannot prove that real models naturally choose the intended MCP path.

## Decision 3: Improve Completion Ranking Without Intelligence Layers

**Decision**: Add deterministic ranking rules only: exact prefix first, stronger context first, current-session plan recency first, and feature-specific algorithms first.

**Rationale**: The current completion implementation already returns valid values.
The next improvement should reduce ambiguity without introducing cross-session memory, embeddings, model calls, or behavior learning.

**Alternatives considered**:

- Add semantic vector ranking. Rejected as overbuilt for bounded metadata values.
- Track user behavior across sessions. Rejected because completions should remain session-safe and deterministic.
- Let the model rank values itself. Rejected because the server can cheaply provide stable context-aware ordering.

## Decision 4: Standardize Recovery Instead of Creating an Error DSL

**Decision**: Extend the existing `recovery` map into a consistent envelope instead of adding a new error language or planner.

**Rationale**: Models need a small stable set of fields to retry safely. A full recovery DSL would add abstraction cost before the need is proven.

**Alternatives considered**:

- Keep recovery as prose. Rejected because models may miss or misapply repair instructions.
- Add a central recovery planner. Rejected because existing errors can be improved locally with simpler structured fields.

## Decision 5: Add Descriptor-Owned Navigation, Not a Graph Engine

**Decision**: Add descriptor-owned `resourceNavigation` metadata to capability payloads after higher-priority guards and recovery are in place.

**Rationale**: Models benefit from knowing how resources and tools relate, but a runtime graph traversal service would duplicate model reasoning and introduce a new abstraction layer.

**Alternatives considered**:

- Build a graph service with automatic traversal. Rejected as overdesigned for the current need.
- Do nothing. Rejected because resource hierarchy and workflow next hops are stable enough to expose cheaply.

## Decision 6: Correct Documentation Drift Early

**Decision**: Update README content that still describes prompts and completions as deferred.

**Rationale**: Documentation feeds users, reviewers, and model context. Stale documentation undermines the newly implemented protocol surface and makes future E2E prompts less reliable.

**Alternatives considered**:

- Wait until all hardening work is done. Rejected because the current README is already incorrect for the baseline.

## Decision 7: Preserve Explicit Operator Control

**Decision**: Every real-model scenario, recovery suggestion, and navigation path involving side effects must preserve preview and approval boundaries.

**Rationale**: The MCP constitution requires reviewable schema/rule changes. Model comfort must come from clarity and safety, not silent execution.

**Alternatives considered**:

- Auto-execute safe-looking calls. Rejected because the server cannot infer operator approval from model confidence.

## Decision 8: Require Explicit Update Execution Mode

**Decision**: `execute_update` must reject missing `execution_mode` and recover to `preview`.

**Rationale**: Hidden compatibility defaults make a model-facing tool harder to reason about. Explicit mode selection is safer and clearer for both models and operators.

**Alternatives considered**:

- Preserve omitted-mode execution for compatibility. Rejected because this increment explicitly does not require backward compatibility and model safety is higher priority.
- Split `execute_update` into two tools now. Deferred because the explicit enum mode already exists and can satisfy the requirement with less churn.

## Decision 9: Prefer Native Clarification but Keep Structured Fallbacks

**Decision**: Use MCP-native elicitation, sampling, progress, roots, or logging when stable SDK APIs exist; otherwise expose the same model-facing semantics through structured fields.

**Rationale**: Native protocol features are best for model clients, but the implementation should not block on SDK gaps or invent unstable dependencies.

**Alternatives considered**:

- Wait for every native capability before improving the surface. Rejected because structured fallback metadata still improves model behavior.
- Implement a custom conversation planner. Rejected because it hides MCP primitives and exceeds the current scope.

## Decision 10: Add Completion Diagnostics Without Changing Values

**Decision**: Completion may include metadata describing source, ranking reason, and missing context, while `values` remain plain reusable argument strings.

**Rationale**: Models need to know why a list is empty or ranked, but clients expect completion values to be directly reusable.

**Alternatives considered**:

- Return rich objects as completion values. Rejected because it would make values less directly usable.
- Keep empty results silent. Rejected because models then guess missing context.

## Decision 11: Enforce Descriptor Quality With Lint and Fingerprints

**Decision**: Add descriptor lint and deterministic capability fingerprints for descriptor catalog, prompt set, navigation metadata, and model-facing schemas.

**Rationale**: Description quality is now part of the public protocol. Lint catches local quality regressions, and fingerprints make test and model reports traceable.

**Alternatives considered**:

- Rely on code review. Rejected because descriptor regressions are easy to miss.
- Use runtime timestamps as versions. Rejected because fingerprints must be deterministic and reviewable.

## Decision 12: Standardize Next-Action Metadata

**Decision**: Use one next-action vocabulary across tool outputs, resource outputs, prompt instructions, and recovery.

**Rationale**: Models handle repeated field shapes well. A single vocabulary reduces hesitation and avoids different outputs teaching different retry patterns.

**Alternatives considered**:

- Keep source-specific field names. Rejected because semantic duplicates make model traces noisier.
- Add a central next-action planner. Rejected because local structured metadata is enough.

## Decision 13: Move Navigation Ownership Into Descriptors

**Decision**: Store navigation relationships in descriptor YAML or equivalent descriptor inputs instead of Java catalog hardcoding.

**Rationale**: Navigation belongs beside public identifiers. Descriptor ownership lets new features add relationships without modifying catalog logic.

**Alternatives considered**:

- Keep Java hardcoding. Rejected because it will drift as descriptors grow.
- Add a graph engine. Rejected because models can traverse lightweight relationships themselves.

## Decision 14: Add Ergonomics Guards Instead of Broad Benchmarks

**Decision**: Cover naming clarity, pagination, sampling, progress, logging, roots or permission boundaries, prompt argument coverage, examples,
and model-confusion tests with deterministic checks.

**Rationale**: These checks directly target model comfort without the cost and nondeterminism of benchmark leaderboards.

**Alternatives considered**:

- Create a multi-model benchmark suite. Rejected for this increment because it is expensive, nondeterministic, and less actionable than targeted contract tests.
- Leave ergonomics to documentation. Rejected because models consume protocol payloads first.
