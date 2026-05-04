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

# Feature Specification: AI-Friendly MCP Experience Hardening

> Status after over-design cleanup: This Spec Kit draft is kept for traceability only and is not the active implementation scope. Use `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md` as the current lightweight scope. Normalized golden transcript suites, broad real-model E2E expansion, model-confusion matrices, and MCP-native sampling/progress/logging/roots work are deferred until separately justified.

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-04
**Status**: Draft
**Input**: User request to use Spec Kit to include every P0, P1, and P2 MCP model-comfort improvement without switching branches.

## Process Constraints

- Stay on the current branch. Do not run branch-changing Spec Kit scripts, `git switch`, or `git checkout`.
- Treat this specification as the canonical Spec Kit entry point for the next increment.
- Keep `requirements.md` as the detailed requirements baseline generated from the latest analysis.
- Do not edit generated paths such as `target/`.
- Preserve explicit approval semantics for side-effecting MCP tools.
- Require explicit execution mode for side-effecting update tools.
- Prefer MCP-native elicitation, sampling, progress, logging, and roots when stable SDK APIs exist; otherwise expose structured fallback metadata.
- Avoid broad planner, memory, ranking, graph, or benchmark abstractions unless a smaller scoped requirement proves the need.

## Current Baseline

The previous implementation already provides descriptor-backed tools, resources, prompts, completions, session-scoped workflow plans, and preview support for `execute_update` and `apply_workflow`.
The next increment should not rebuild those surfaces.
It should harden the model experience, prevent regressions, and add only small targeted enhancements.

## Priority Scope

- **P0**: Golden transcript contracts, real-model E2E reports, explicit update execution mode, structured clarification, and completion diagnostics.
- **P1**: Descriptor lint, capability fingerprints, unified next-action metadata, descriptor-owned navigation, prompt stop conditions, and transport contract tests.
- **P2**: Naming audit, error taxonomy, pagination, sampling, progress, logging, roots boundaries, prompt argument coverage, compact output examples,
  and model-confusion tests.

## User Scenarios and Testing

### User Story 1: Protocol Surface Cannot Regress (Priority: P0)

Model-facing descriptions, schemas, prompts, completions, resource templates, preview enums, and safety annotations remain stable enough for models to rely on.

**Why this priority**: A handler can still compile while its descriptor becomes worse for models. This needs a contract guard before more usability changes land.

**Independent Test**: Normalized MCP transcript golden tests compare stable protocol payloads after sorting unordered collections and masking dynamic fields.

**Acceptance Scenarios**:

1. **Given** the MCP runtime starts with the default descriptor catalog, **When** normalized `initialize`, `tools/list`, `resources/list`,
   `resources/templates/list`, `prompts/list`, representative `completion/complete`, and `shardingsphere://capabilities` payloads are captured,
   **Then** they match the approved golden contract.
2. **Given** a descriptor removes a prompt, completion target, field description, preview enum, or safety annotation, **When** golden tests run,
   **Then** they fail with a clear contract diff.
3. **Given** runtime-specific values vary by session or fixture, **When** golden tests compare payloads, **Then** session identifiers, timestamps,
   database versions, and other dynamic values are normalized.

### User Story 2: Real Models Prove the Surface Is Comfortable (Priority: P0)

A real model can use prompts, completions, previews, resources, and tools in the expected order without relying on README-only instructions.

**Why this priority**: Deterministic tests prove protocol availability, but real-model E2E is the only direct evidence that the surface feels natural to an actual model.

**Independent Test**: Opt-in real-model E2E runs outside default CI with non-production credentials and trace-based assertions.

**Acceptance Scenarios**:

1. **Given** a real model is asked to inspect metadata, **When** MCP actions are available, **Then** the trace contains prompt discovery or prompt retrieval,
   resource reads or completions, and a schema-valid `execute_query`.
2. **Given** a real model is asked to execute side-effecting SQL safely, **When** it reaches an update-capable statement, **Then** the trace contains
   `execute_update` with `execution_mode=preview` before any execute mode.
3. **Given** a real model is asked to complete an encrypt or mask workflow, **When** it plans, previews, applies, and validates, **Then** the trace proves
   prompt use, completion use, preview before side effects, approval-boundary preservation, and final validation.
4. **Given** the model service is unavailable, **When** opt-in E2E runs, **Then** it reports an unavailable or skipped state without affecting default CI.

### User Story 3: Completion Results Prefer Useful Candidates (Priority: P2)

Completion remains simple and deterministic while ranking candidates in a way that reduces model hesitation and wrong argument choices.

**Why this priority**: Current completion is usable, but exact-match, context-aware, lifecycle-aware ordering improves model comfort without speculative intelligence.

**Independent Test**: Completion unit and transport tests assert candidate ordering for exact prefix, contextual metadata, workflow plan recency, and feature-specific algorithms.

**Acceptance Scenarios**:

1. **Given** a prefix exactly matches a candidate, **When** completion runs, **Then** the exact match appears before broader prefix matches.
2. **Given** database and schema context are supplied, **When** table or column completion runs, **Then** candidates are scoped to that context.
3. **Given** multiple current-session workflow plans are eligible, **When** `plan_id` completion runs, **Then** the most recently updated plan appears first.
4. **Given** encrypt and mask algorithm names overlap, **When** completion is requested from a feature-specific reference, **Then** candidates from that feature rank first.

### User Story 4: Models Repair Errors From Structured Recovery (Priority: P2)

Recoverable errors include structured fields that help a model retry safely without reconstructing the whole call from prose.

**Why this priority**: Models frequently make small call-shape mistakes. A consistent recovery envelope turns those mistakes into safe local repairs.

**Independent Test**: Error converter and model-like E2E tests assert recovery metadata for missing fields, invalid enum values, unsupported resources,
wrong SQL tool choice, and unavailable workflow plans.

**Acceptance Scenarios**:

1. **Given** a required argument is missing, **When** the server returns an error, **Then** `recovery` includes `recoverable`, `category`, `model_action`, and safe next-step metadata.
2. **Given** a model sends side-effecting SQL to `execute_query`, **When** the server rejects it, **Then** recovery recommends the update-capable preview path and preserves approval requirements.
3. **Given** a `plan_id` is unavailable, **When** apply or validation fails, **Then** recovery tells the model to re-run the matching planning tool in the current session.
4. **Given** the server cannot safely know a missing value, **When** recovery is generated, **Then** it omits guessed arguments and sets `ask_user_when_uncertain`.

### User Story 5: Resource Navigation Shows the Next MCP Hop (Priority: P1)

The capability payload exposes descriptor-owned navigation metadata so models can understand public resource, prompt, and tool relationships without a runtime graph engine.

**Why this priority**: Navigation improves orientation and should live beside descriptor ownership so new resources do not require catalog hardcoding.

**Independent Test**: Descriptor catalog tests verify navigation entries resolve to existing public resources, prompts, and tools and are loaded from descriptor metadata.

**Acceptance Scenarios**:

1. **Given** a model reads capabilities, **When** it sees metadata resources, **Then** it can infer `databases -> schemas -> tables -> columns/indexes/sequences`.
2. **Given** a model reads feature algorithm resources, **When** it sees workflow relationships, **Then** it can infer algorithms feed `plan_encrypt_rule` or `plan_mask_rule`.
3. **Given** a workflow plan exists, **When** the model reads navigation metadata, **Then** it can infer `plan -> apply_workflow preview -> apply_workflow execute -> validate_workflow`.
4. **Given** a navigation entry references a removed identifier, **When** descriptor tests run, **Then** they fail.

### User Story 6: Side-Effecting Tools Require Explicit Execution Mode (Priority: P0)

Models cannot trigger update-capable behavior by omitting `execution_mode`.

**Independent Test**: Tool handler and transport tests reject missing mode and return recovery that recommends preview.

**Acceptance Scenarios**:

1. **Given** a model calls `execute_update` without `execution_mode`, **When** validation runs, **Then** the call is rejected as recoverable.
2. **Given** a model reads `execute_update` descriptors, **When** it forms arguments, **Then** preview and execute modes are explicit.
3. **Given** a future side-effecting tool is added, **When** descriptor lint runs, **Then** it requires explicit preview or approval semantics.

### User Story 7: Models Know Whether to Ask, Read, or Retry (Priority: P0)

Missing context, empty completions, and user-only choices are exposed through native MCP capabilities when available or through structured fallback metadata.

**Independent Test**: Completion, recovery, and prompt tests assert missing context, pending questions, and ask-user flags.

**Acceptance Scenarios**:

1. **Given** completion lacks required context, **When** it runs, **Then** metadata identifies the missing argument or resource.
2. **Given** a server cannot compute user intent, **When** a tool or prompt needs it, **Then** the model is told to ask the user.
3. **Given** completion returns values, **When** diagnostics are available, **Then** ranking source is explained without changing reusable values.

### User Story 8: Descriptor Quality Is Enforced and Versioned (Priority: P1)

Descriptors are linted for model clarity and capability payloads carry deterministic fingerprints.

**Independent Test**: Descriptor lint fails on weak descriptions, missing field explanations, missing annotations, missing navigation, and absent fingerprints.

**Acceptance Scenarios**:

1. **Given** a descriptor exposes an enum, **When** lint runs, **Then** every enum value has model-facing meaning.
2. **Given** capabilities are read, **When** payloads are generated, **Then** descriptor, prompt, navigation, and schema fingerprints are present.
3. **Given** real-model E2E reports a result, **When** artifacts are written, **Then** the relevant fingerprints are recorded.

### User Story 9: Next Actions Use One Vocabulary (Priority: P1)

Successful outputs, resource outputs, prompt instructions, and errors use the same next-action fields.

**Independent Test**: Schema and model-like tests assert shared fields across representative outputs.

**Acceptance Scenarios**:

1. **Given** a workflow plan is returned, **When** the next step is known, **Then** the output includes standard next-tool and approval metadata.
2. **Given** a prompt describes a workflow, **When** the model retrieves it, **Then** the prompt includes stop and ask-user conditions.
3. **Given** a value is unsafe to suggest, **When** next-action metadata is generated, **Then** the model is told to ask rather than guess.

### User Story 10: Protocol Transport Proves Model Visibility (Priority: P1)

Prompt retrieval, completion metadata, capability fingerprints, and descriptor-owned navigation are tested through public MCP transport calls.

**Independent Test**: HTTP and STDIO contract tests call public protocol methods instead of factories only.

**Acceptance Scenarios**:

1. **Given** prompt descriptors exist, **When** `prompts/get` runs through transport, **Then** model-facing messages are returned.
2. **Given** completion targets exist, **When** `completion/complete` runs through transport, **Then** values and diagnostics are visible.
3. **Given** capabilities are read through transport, **When** navigation is included, **Then** all endpoints resolve publicly.

### User Story 11: Ergonomic Boundaries Are Clear (Priority: P2)

Names, pagination, sampling, progress, logging, and roots or permission boundaries are clear enough for models to avoid hidden project knowledge.

**Independent Test**: Descriptor lint and model-confusion tests cover ambiguous names, large lists, long workflows, and boundary hints.

**Acceptance Scenarios**:

1. **Given** a list can be large, **When** it is returned, **Then** pagination fields explain whether and how to continue.
2. **Given** a workflow can take time, **When** progress reporting is supported, **Then** native progress or structured progress fields are exposed.
3. **Given** sampling or logging is available, **When** a workflow does not need it, **Then** it is not used for hidden planning, execution, or ranking.
4. **Given** file or config roots become public resources, **When** descriptors are exposed, **Then** access boundaries are explicit.

### User Story 12: Examples and Negative Paths Teach Safe Use (Priority: P2)

Complex outputs include compact examples and common model mistakes are tested.

**Independent Test**: Descriptor lint verifies examples and model-confusion tests assert structured recovery for wrong order, stale state, and ambiguity.

**Acceptance Scenarios**:

1. **Given** complex output descriptors exist, **When** lint runs, **Then** examples are compact, static, and secret-free.
2. **Given** a model applies before planning, **When** the call fails, **Then** recovery points to planning in the current session.
3. **Given** a model executes before preview or approval, **When** validation runs, **Then** recovery preserves the approval boundary.

## Edge Cases

- A golden payload contains runtime-specific metadata. The test must normalize it before comparison.
- A completion request lacks prerequisite context. The server should return an empty or clearly bounded result rather than fabricating candidates.
- Two completion candidates share a prefix. Ordering must be deterministic and explainable by static ranking rules.
- A real-model E2E environment lacks credentials. The run should skip or report unavailable status outside default CI.
- A recovery envelope cannot compute a safe argument. It must ask the user instead of guessing.
- Resource navigation would require hidden physical metadata. It must omit that relationship unless it is already public.
- A model omits `execution_mode` for a side-effecting update. The server must reject and recommend preview rather than silently execute.
- Native MCP elicitation, sampling, progress, logging, or roots are unavailable in the current SDK. The server must expose equivalent structured fallback metadata.
- Descriptor fingerprints include runtime-specific values. The fingerprint generation must ignore session and environment state.
- A prompt argument has no completion. The descriptor must mark it as resource-derived or user-provided only.
- Pagination can omit data. The response must clearly state whether more data exists and how to continue.
- Complex examples could leak real schema or secrets. Examples must remain static, small, and fixture-safe.

## Requirements

### Functional Requirements

- **FR-001**: README documentation MUST no longer describe prompts or completions as deferred when protocol-visible support exists.
- **FR-002**: The project MUST include normalized golden tests for the protocol-visible MCP model surface.
- **FR-003**: Golden normalization MUST remove or replace dynamic values such as session IDs, timestamps, runtime database versions, and nondeterministic ordering.
- **FR-004**: Golden tests MUST fail when model-facing descriptions, schemas, prompts, completions, preview enums, annotations, or safety metadata disappear.
- **FR-005**: Real-model E2E MUST be opt-in and excluded from default CI.
- **FR-006**: Real-model E2E MUST use non-production credentials and deterministic ShardingSphere fixtures.
- **FR-007**: Real-model E2E MUST redact credentials from logs and artifacts.
- **FR-008**: Real-model E2E MUST assert prompt calls, completion calls, resource reads, tool order, argument schema validity,
  preview-before-execution, approval boundaries, recovery path, and final validation.
- **FR-009**: Completion handlers MUST keep returned values directly reusable as argument strings.
- **FR-010**: Completion handlers MUST keep deterministic ordering for the same runtime state and request.
- **FR-011**: Completion handlers SHOULD rank exact prefix matches before broader prefix matches.
- **FR-012**: Completion handlers SHOULD rank context-matching candidates before weaker-context candidates.
- **FR-013**: `plan_id` completions SHOULD rank current-session eligible plans by most recent update.
- **FR-014**: Algorithm completions SHOULD prefer candidates matching the prompt or resource feature context.
- **FR-015**: Completion ranking MUST NOT use cross-session history, vector search, model calls, or user behavior learning in this increment.
- **FR-016**: Recoverable error payloads MUST use a consistent `recovery` envelope.
- **FR-017**: Recovery metadata MUST include `recoverable`, `category`, and `model_action` when recovery is possible.
- **FR-018**: Recovery metadata SHOULD include `suggested_next_tool`, `suggested_arguments`, `read_resources_first`, and `ask_user_when_uncertain` when safe.
- **FR-019**: `suggested_arguments` MUST contain only server-known values or values already supplied by the user.
- **FR-020**: `suggested_arguments` MUST NOT contain secrets, guessed values, placeholders, or hidden physical objects.
- **FR-021**: Wrong-tool SQL recovery MUST preserve preview and user approval semantics.
- **FR-022**: Unavailable workflow plan recovery MUST recommend replanning in the current MCP session.
- **FR-023**: The capability payload MUST expose lightweight `resourceNavigation` metadata loaded from descriptor-owned metadata.
- **FR-024**: Navigation entries SHOULD include `from`, `to`, `requiredArguments`, `carriedArguments`, and `description`.
- **FR-025**: Navigation MUST cover metadata hierarchy, feature algorithm to planning relationships, and workflow plan to apply and validate relationships.
- **FR-026**: Navigation MUST reference only public resources, prompts, and tools.
- **FR-027**: Navigation MUST NOT introduce a runtime graph engine, automatic traversal tool, or hidden planning service.
- **FR-028**: Navigation SHOULD be owned by descriptor YAML or equivalent descriptor inputs rather than Java hardcoded relationships.
- **FR-029**: `execute_update` MUST require explicit `execution_mode` and MUST reject omission with recovery that recommends preview.
- **FR-030**: Missing user-only input MUST use MCP-native elicitation when stable SDK support exists, otherwise structured clarification fields.
- **FR-031**: Completion responses SHOULD include diagnostic metadata for ranking source, missing context, and empty results without changing reusable values.
- **FR-032**: Descriptor lint MUST enforce model-facing descriptions, enum explanations, output schemas, annotations, safety hints, prompt links, completion targets, and navigation links.
- **FR-033**: Capability payloads MUST include deterministic fingerprints for descriptor catalog, prompt set, navigation metadata, and model-facing schema set.
- **FR-034**: Tool outputs, resource outputs, prompt instructions, and recoverable errors SHOULD share next-action fields for next tool, arguments, resources, approval, and ask-user status.
- **FR-035**: Prompt templates MUST include stop conditions and ask-user conditions where workflows or user intent require them.
- **FR-036**: Transport contract tests MUST cover `prompts/get`, `completion/complete`, capability fingerprints, navigation, and explicit side-effect mode requirements.
- **FR-037**: Names and descriptions MUST distinguish read-only, preview, and side-effecting actions.
- **FR-038**: Large result surfaces MUST expose consistent pagination continuation metadata.
- **FR-039**: Long-running workflows SHOULD expose native progress or structured progress fields when supported.
- **FR-040**: File or configuration resources MUST expose roots or permission boundaries before access if such resources become public.
- **FR-041**: Prompt arguments MUST be covered by completion, resource derivation, or explicit user-provided-only documentation.
- **FR-042**: Complex output descriptors SHOULD include compact static examples.
- **FR-043**: Model-confusion tests MUST cover wrong call order, missing execution mode, stale `plan_id`, unknown database, ambiguous metadata, invalid enum, and wrong SQL tool.
- **FR-044**: Recovery categories MUST distinguish SQL parse error, unsupported statement, multiple statements, missing database, unsafe SQL,
  unsupported dialect, unsupported metadata object, invalid enum, wrong tool, missing execution mode, and stale workflow plan.
- **FR-045**: Native sampling and logging SHOULD be used only with stable SDK support and concrete workflow need; they MUST NOT become hidden planning,
  hidden execution, or completion ranking dependencies.

### Key Entities

- **Normalized Transcript Fixture**: Stable JSON payload derived from public MCP protocol calls after dynamic fields are normalized.
- **Real-Model Scenario**: Opt-in E2E scenario with public prompts, completions, resources, tools, and trace-based assertions.
- **Completion Ranking Rule**: Deterministic ordering rule based on prefix, context, lifecycle, and feature reference.
- **Recovery Envelope**: Structured error metadata that tells a model whether and how it can safely repair a failed MCP call.
- **Resource Navigation Entry**: Descriptor-owned relationship between public MCP resources, prompts, or tools.
- **Capability Fingerprint Set**: Deterministic hashes that identify descriptor, prompt, navigation, and schema versions visible to models.
- **Descriptor Lint Finding**: Validation result that points to a specific model-facing descriptor defect.
- **Next-Action Metadata**: Shared output fields that tell a model what to read, call, ask, or stop doing next.
- **Completion Diagnostic Metadata**: Optional metadata that explains candidate ordering and missing context while preserving string completion values.
- **Model-Confusion Scenario**: Negative-path test for common model mistakes and structured recovery.

## Success Criteria

- **SC-001**: Golden tests fail when prompt, completion, preview, description, schema, or safety metadata is accidentally removed.
- **SC-002**: At least one opt-in real-model metadata scenario proves prompt/completion/resource/tool trace coverage without README-only hints.
- **SC-003**: At least one opt-in real-model side-effect scenario proves preview-before-execution and approval-boundary preservation.
- **SC-004**: Completion tests prove deterministic ordering for exact match, prefix match, contextual metadata, plan recency, and feature-specific algorithms.
- **SC-005**: Recovery tests prove a model-like caller can repair common failures from structured fields without guessing hidden values.
- **SC-006**: Explicit update-mode tests prove missing `execution_mode` cannot execute and recovers to preview.
- **SC-007**: Descriptor lint fails on missing clarity, schema, safety, completion, prompt, or navigation metadata.
- **SC-008**: Capability payloads and reports contain deterministic model-surface fingerprints.
- **SC-009**: Navigation tests prove every descriptor-owned navigation entry resolves to existing public MCP identifiers.
- **SC-010**: Transport tests prove prompt, completion, capability, navigation, and side-effect requirements are visible through public MCP calls.
- **SC-011**: Model-confusion tests prove common wrong paths recover safely.
- **SC-012**: Default CI remains deterministic and does not require real model credentials.

## Assumptions

- MCP Java SDK prompt and completion support remains available in the current dependency set.
- The existing descriptor catalog remains the source of truth for public model-facing identifiers.
- Existing MCP interaction trace infrastructure can be extended instead of replaced.
- Real-model E2E runs against non-production fixtures and credentials only.
- Workflow `plan_id` remains session-scoped and non-durable.
- Descriptor-owned navigation can be represented in YAML or equivalent descriptor inputs without adding a graph service.
- Native MCP elicitation, sampling, progress, logging, and roots are optional by SDK availability; structured fallback metadata is acceptable when native APIs are absent.
