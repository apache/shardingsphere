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

# Research: AI-Friendly MCP Lightweight Requirements

This research note records the active decisions behind `spec.md` and `requirements.md`.
It intentionally removes earlier over-built ideas from the active increment.

## Decision 1: Treat Capabilities as the Current Public Surface

**Decision**: Use `shardingsphere://capabilities` as the sole current model-facing contract.
Descriptors and README public lists must align to it.
Historical PRD and design documents may remain for traceability, but they must be labeled as non-current when they mention older tool matrices.

**Rationale**: Models should not need to reconcile multiple competing surfaces before making the first MCP call.

**Alternatives considered**:

- Reintroduce `list_*` and `describe_*` tools for every resource. Rejected because the current resource-first surface already exposes metadata without a broad tool matrix.
- Keep historical design text unmarked. Rejected because models may treat old tool names as active tools.
- Keep legacy compatibility shims. Rejected because the confirmed direction is to fix compatibility drift by removing obsolete contracts.

## Decision 2: Standardize `next_actions` Without a Planner

**Decision**: Use `next_actions` as the primary continuation shape for successful outputs, previews, workflow states, and recoverable errors.
Each action should say what kind of next step is safe, why it is needed, whether user approval is required, and which tool/resource/inputs are known.
Legacy recommendation fields such as `recommended_next_tool` and `suggested_next_tool` must be migrated out of the active contract.

**Rationale**: A stable local shape reduces model guessing while avoiding a new orchestration layer.

**Alternatives considered**:

- Add a central planner. Rejected because the current gaps are local output-shape gaps.
- Keep separate field names for each tool. Rejected because duplicate semantics increase model confusion.

## Decision 3: Return Descriptor-Backed Resource URIs From Search

**Decision**: `search_metadata` should return readable resource URIs only when they can be safely derived from public resource templates.
When derivation is unsafe, the response should explain that status instead of guessing.

**Rationale**: URI construction is repetitive and easy for models to get slightly wrong.

**Alternatives considered**:

- Add a runtime graph traversal service. Rejected as unnecessary for known metadata hierarchy.
- Let models construct all URIs manually. Rejected because the server already knows many safe links.

## Decision 4: Align Output Schemas With Real Payloads

**Decision**: The core tools need descriptor output schemas that match actual payloads for search, SQL, planning, apply, and validation paths.

**Rationale**: Schema drift teaches models the wrong contract even when handlers still work.

**Alternatives considered**:

- Use broad `object` or `array` schemas. Rejected because they do not help models parse or continue safely.
- Add large golden transcript suites now. Rejected because focused schema and capabilities checks provide enough protection for this increment.

## Decision 5: Keep Recovery Structured and Local

**Decision**: Recoverable errors should expose missing fields, safe retry hints, resources to read first, approval requirements, and current-session workflow recovery where applicable.

**Rationale**: Missing `database`, missing `execution_mode`, wrong SQL tool, unknown public identifier, and stale `plan_id` are high-frequency local mistakes.

**Alternatives considered**:

- Keep recovery as prose only. Rejected because models can miss important constraints.
- Add model-confusion matrices first. Rejected because focused branch tests are simpler and more actionable.

## Decision 6: Preserve Preview and User Approval Boundaries

**Decision**: Side-effecting SQL and workflow apply paths must continue through preview first and must not execute without explicit user approval.
Missing `execution_mode` must be rejected, then recovered to `execution_mode=preview`; it must not silently default.

**Rationale**: Model comfort should come from safe continuation, not hidden execution.

**Alternatives considered**:

- Auto-execute safe-looking changes. Rejected because model confidence is not operator approval.
- Split every side-effect path into extra tools now. Deferred because explicit rejected-or-preview `execution_mode` already gives a smaller contract.

## Decision 7: Improve Completion Deterministically

**Decision**: Completion improvements should stay deterministic: prefix first, contains fallback, supplied-context ordering, and current-session `plan_id` ordering.

**Rationale**: The server can reduce ambiguity without embeddings, model calls, cross-session memory, or user behavior learning.

**Alternatives considered**:

- Add vector search or semantic ranking. Rejected as over-built for bounded metadata and algorithm identifiers.
- Learn from user behavior across sessions. Rejected because completions should remain session-safe and predictable.

## Decision 8: Expose Algorithm Properties Through Existing Resources

**Decision**: Encrypt and mask algorithm resources should expose required properties, optional properties, defaults, secret flags, and capability hints where known.

**Rationale**: Models should be able to plan with fewer clarifying turns without inventing algorithm properties.

**Alternatives considered**:

- Hide property requirements until planner validation. Rejected because it forces avoidable trial and error.
- Add a separate algorithm-planning service. Rejected because the existing resource surface is enough.

## Decision 9: Keep Regression Guards Lightweight

**Decision**: Use descriptor lint, capabilities shape checks, focused contract assertions, and scoped unit tests.
Real-model scenarios remain opt-in and limited to a few high-value paths.

**Rationale**: The current goal is to protect the model-visible contract without adding a brittle benchmark system.

**Alternatives considered**:

- Add normalized transcript mega suites. Rejected for this increment because they are broad and review-heavy.
- Put live model tests in default CI. Rejected because they require credentials, network access, and nondeterministic external services.

## Decision 10: Keep P1 and P2 Behind P0

**Decision**: Resource navigation fields, compact examples, completion refinements, startup hints, troubleshooting docs, and opt-in usability scenarios should follow P0.

**Rationale**: P0 fixes correctness and safety. P1/P2 are comfort improvements and should not block or expand the core surface.

**Alternatives considered**:

- Build all comfort features in one pass. Rejected because it would blur the smallest safe implementation boundary.
- Drop P1/P2 entirely. Rejected because they are useful once P0 is stable and can be delivered independently.
