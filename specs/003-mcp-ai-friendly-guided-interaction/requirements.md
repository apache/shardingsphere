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

# Requirements: AI-Friendly MCP Experience Hardening

> Status after over-design cleanup: This requirements draft is kept for traceability only and is not the active implementation baseline. Use `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md` for the current lightweight scope. Normalized golden transcript suites, broad real-model E2E expansion, model-confusion matrices, sampling/progress/logging/roots work, metadata freshness, env-var config interpolation, and current-session workflow listing resources are deferred until separately justified.

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-04
**Status**: Draft
**Input**: Convert the complete P0, P1, and P2 model-comfort improvement list into a Spec Kit requirements baseline after SDK-native prompts,
completions, workflow session lifecycle, and preview support were implemented.

## Process Constraints

- Stay on the current branch. Do not run branch-changing Spec Kit commands, `git switch`, or `git checkout`.
- Treat this file as the current requirements baseline for the next increment.
- Keep the existing `spec.md`, `plan.md`, `tasks.md`, `research.md`, `data-model.md`, and `quickstart.md` for traceability until they are regenerated from this baseline.
- Do not edit generated paths such as `target/`.
- Require explicit model-visible approval semantics for side-effecting tools.
- Do not introduce broad planner, memory, ranking, graph, or benchmark abstractions before a smaller requirement proves the need.

## Current Baseline

The completed baseline already includes:

- descriptor-backed MCP tools, resources, prompts, and completions;
- SDK-native prompt and completion registration;
- session-scoped workflow `plan_id` lifecycle;
- `execute_update` and `apply_workflow` preview modes;
- deterministic tests for prompt/completion bridge support;
- descriptor validation for prompt templates and completion references.

The next requirements therefore focus on hardening and measuring model comfort, not rebuilding the base MCP surface.

## Scope Decision

### Must Do

- Correct stale README statements that still describe prompt and completion support as deferred.
- Add normalized MCP transcript golden tests for the protocol-visible model surface.
- Add opt-in real-model E2E scenarios with strong trace assertions and no default CI dependency.
- Make side-effecting update execution require an explicit model-supplied execution mode.
- Add model-native or structured clarification semantics for missing user input.
- Add completion diagnostic metadata that explains ranking sources and empty completion results.
- Add descriptor lint and version fingerprints for model-facing capabilities.
- Add a unified next-action contract across tool outputs, resource outputs, prompts, and recoverable errors.
- Add a unified structured recovery envelope that helps models repair failed calls.
- Improve completion ordering with deterministic, context-aware ranking only.
- Move lightweight resource navigation ownership into descriptors while avoiding a runtime graph engine.
- Add prompt stop conditions and explicit ask-user conditions.
- Add protocol transport contract tests for prompt retrieval, completion, and capability navigation.
- Add model ergonomics requirements for naming, pagination, sampling, progress, logging, roots or permission boundaries, prompt argument coverage,
  output examples, and model-confusion tests.

### Conditional Native Protocol Use

- Use MCP-native elicitation, sampling, progress, logging, roots, or related capabilities when the current SDK exposes them with stable APIs.
- When a native MCP capability is not available, expose the same model-facing semantics through structured fields and keep the fallback discoverable in descriptors.

## Priority Coverage Map

- **P0**: Golden transcript contract, real-model E2E report, explicit `execute_update` mode, structured clarification, and completion diagnostics.
- **P1**: Descriptor lint, capability fingerprints, unified next-action contract, descriptor-owned navigation, prompt stop conditions, and transport contract tests.
- **P2**: Naming clarity, recovery taxonomy, pagination, sampling, progress, logging, roots or permission boundaries, prompt argument coverage,
  compact examples, and model-confusion tests.

### Do Not Do In This Increment

- Do not add live model calls to default CI.
- Do not build a model benchmark suite or multi-model leaderboard.
- Do not add semantic vector ranking, cross-session memory, or user behavior learning for completions.
- Do not add a central AI planner or mega tool that hides MCP primitives.
- Do not build a runtime graph engine or automatic resource traversal service.
- Do not add broad benchmark leaderboards, adaptive cross-session memory, or hidden execution shortcuts.

## User Scenarios and Testing

### User Story 1: Protocol Surface Cannot Regress (Priority: P0)

Model-facing descriptions, schemas, prompts, completions, resource templates, preview enums, and safety annotations must remain stable enough for models to rely on.

**Why this priority**: Without a contract guard, later descriptor or YAML changes can silently make the model surface less natural even while handlers still compile.

**Independent Test**: Run normalized transcript golden tests that compare stable protocol payloads after removing dynamic fields and sorting unordered collections.

**Acceptance Scenarios**:

1. **Given** the MCP runtime starts with the default descriptor catalog, **When** normalized `initialize`, `tools/list`, `resources/list`,
   `resources/templates/list`, `prompts/list`, `completion/complete`, and `shardingsphere://capabilities` payloads are captured,
   **Then** they match the approved golden contract.
2. **Given** a descriptor removes a prompt, completion target, field description, preview enum, or safety annotation, **When** golden tests run,
   **Then** the tests fail with the missing model-facing contract field.
3. **Given** runtime-specific fields such as timestamps, session identifiers, database versions, or result ordering can vary,
   **When** golden payloads are compared, **Then** those dynamic fields are normalized before comparison.

### User Story 2: Real Models Prove the Surface Is Comfortable (Priority: P0)

A real model should naturally use prompts, completions, previews, resources, and tools in the expected order without relying on README-only knowledge.

**Why this priority**: Deterministic tests prove protocol availability, but only real-model E2E can show whether an actual model chooses the intended MCP path.

**Independent Test**: Run opt-in real-model E2E outside default CI with non-production credentials and trace-based assertions.

**Acceptance Scenarios**:

1. **Given** a real model is asked to inspect metadata, **When** the MCP actions are available, **Then** the trace includes prompt discovery or prompt retrieval,
   resource reads or completions, and a schema-valid `execute_query`.
2. **Given** a real model is asked to execute side-effecting SQL safely, **When** it reaches an update-capable statement, **Then** the trace includes
   `execute_update` with `execution_mode=preview` before any execute mode.
3. **Given** a real model is asked to complete an encrypt or mask workflow, **When** it plans, previews, applies, and validates, **Then** the trace proves
   prompt use, completion use, preview before side effects, approval boundary preservation, and final validation.
4. **Given** the model service is unavailable, **When** real-model E2E runs, **Then** the test reports an explicit skipped or unavailable state rather than failing default CI.

### User Story 3: Completion Results Prefer the Most Useful Candidate (Priority: P2)

Completion should remain simple and deterministic while ranking values in a way that reduces model hesitation and wrong argument selection.

**Why this priority**: Current prefix filtering and lexicographic ordering are usable.
Context-aware deterministic ordering can make the model choose faster without adding speculative intelligence.

**Independent Test**: Use completion unit tests and transport tests that assert sorted values for exact match, prefix match, metadata context, plan lifecycle,
and feature-specific algorithms.

**Acceptance Scenarios**:

1. **Given** a completion prefix exactly matches a candidate, **When** completion returns values, **Then** the exact match appears before broader prefix matches.
2. **Given** database and schema context are supplied, **When** table or column completion runs, **Then** candidates from that context appear and unrelated values are omitted.
3. **Given** multiple current-session workflow plans are completion-eligible, **When** `plan_id` completion runs, **Then** the most recently updated eligible plan appears first.
4. **Given** encrypt and mask algorithm names overlap, **When** algorithm completion is requested from an encrypt or mask reference,
   **Then** candidates are ranked for that feature context without cross-feature confusion.

### User Story 4: Models Can Repair Errors From Structured Recovery (Priority: P2)

Recoverable errors should provide enough structured fields for a model to retry safely without reconstructing the entire call from prose.

**Why this priority**: Models often fail in small ways. A consistent recovery envelope converts these failures into safe, local repairs.

**Independent Test**: Error converter tests and model-like E2E tests assert recovery shape for missing fields, invalid enums, unsupported resources,
wrong SQL tool choice, and unavailable workflow plans.

**Acceptance Scenarios**:

1. **Given** a required argument is missing, **When** the server returns an error, **Then** `recovery` includes `recoverable`, `category`,
   `model_action`, `suggested_next_tool` when known, `suggested_arguments` when safe, and `ask_user_when_uncertain`.
2. **Given** a model calls `execute_query` with side-effecting SQL, **When** the server rejects it, **Then** recovery recommends the update-capable path
   with `execution_mode=preview` and preserves user approval requirements.
3. **Given** a `plan_id` is unavailable, **When** apply or validation fails, **Then** recovery tells the model to call the matching planning tool again instead of retrying stale state.
4. **Given** the server cannot know a missing value safely, **When** recovery is generated, **Then** `suggested_arguments` omits guessed values and `ask_user_when_uncertain` is true.

### User Story 5: Resource Navigation Shows the Next MCP Hop (Priority: P1)

The capability payload should expose descriptor-owned navigation metadata so models can understand resource, prompt, and tool relationships without a graph engine.

**Why this priority**: Navigation helps models choose the next resource or tool. Descriptor ownership keeps the relationship close to the public identifier and avoids Java hardcoding drift.

**Independent Test**: Descriptor catalog tests verify navigation entries reference existing resources, prompts, and tools and are loaded from descriptor metadata.

**Acceptance Scenarios**:

1. **Given** a model reads capabilities, **When** it sees metadata resources, **Then** it can infer `databases -> schemas -> tables -> columns/indexes/sequences`.
2. **Given** a model reads feature algorithm resources, **When** it sees workflow relationships, **Then** it can infer algorithms feed `plan_encrypt_rule` or `plan_mask_rule`.
3. **Given** a workflow response exposes a plan, **When** the model reads navigation metadata, **Then** it can infer `plan -> apply_workflow preview -> apply_workflow execute -> validate_workflow`.
4. **Given** navigation references drift from descriptors, **When** descriptor tests run, **Then** the tests fail.

### User Story 6: Side-Effecting Tools Require Explicit Execution Mode (Priority: P0)

Models should never trigger update-capable behavior by omitting a field that defaults to execution.

**Why this priority**: Model comfort includes safety. A clear preview-first path is easier for models and operators than hidden backward-compatible defaults.

**Independent Test**: Tool handler and transport tests reject missing execution mode for update-capable calls and return structured recovery that recommends preview.

**Acceptance Scenarios**:

1. **Given** a model calls `execute_update` without `execution_mode`, **When** validation runs, **Then** the call is rejected as recoverable and recommends `execution_mode=preview`.
2. **Given** a model calls `execute_update` with `execution_mode=preview`, **When** SQL is valid, **Then** the server returns a preview without side effects.
3. **Given** a model calls `execute_update` with `execution_mode=execute`, **When** approval requirements are satisfied, **Then** the server executes using the existing update path.
4. **Given** a descriptor describes `execute_update`, **When** a model reads it, **Then** the description and schema make the explicit mode requirement unambiguous.

### User Story 7: Clarification and Completion Explain Missing Context (Priority: P0)

Models should know whether to ask the user, read a resource, or retry with a different argument when completion or validation lacks enough context.

**Why this priority**: Empty completions and missing arguments are common. Structured clarification prevents models from guessing.

**Independent Test**: Completion, recovery, and prompt tests verify missing context metadata, ask-user flags, and native-MCP fallback behavior.

**Acceptance Scenarios**:

1. **Given** completion cannot return values because a required context argument is missing, **When** completion runs,
   **Then** metadata identifies the missing context and the resource or prompt that can provide it.
2. **Given** completion returns ranked values, **When** metadata is included, **Then** it explains the ranking source without changing the reusable string values.
3. **Given** a tool requires user-only input, **When** the server cannot compute it, **Then** clarification metadata tells the model to ask the user instead of inventing a value.
4. **Given** the MCP SDK supports native elicitation, **When** missing user input is detected, **Then** native elicitation is preferred; otherwise the structured fallback is exposed.

### User Story 8: Descriptors Are Linted and Versioned (Priority: P1)

Every model-facing descriptor should meet a minimum clarity contract and publish a stable fingerprint for regression reports.

**Why this priority**: Description quality is now part of the product surface. A linter catches unclear or incomplete descriptors before models feel the regression.

**Independent Test**: Descriptor lint tests fail for missing titles, weak descriptions, undocumented enum values, missing output schemas, missing annotations, and unversioned capability payloads.

**Acceptance Scenarios**:

1. **Given** a tool descriptor lacks a useful description, **When** descriptor lint runs, **Then** it fails with the descriptor identifier and missing field.
2. **Given** an enum argument is exposed, **When** descriptor lint runs, **Then** every enum value has model-facing meaning.
3. **Given** capabilities are read, **When** the payload is generated, **Then** descriptor catalog, prompt set, navigation, and schema fingerprints are present.
4. **Given** a real-model E2E report is generated, **When** it records context, **Then** the descriptor and prompt fingerprints are included.

### User Story 9: Outputs and Prompts Share a Next-Action Contract (Priority: P1)

Models should receive the same next-step vocabulary from successful tool outputs, resource outputs, prompt instructions, and recovery envelopes.

**Why this priority**: Different field names for the same concept increase model hesitation and wrong retries.

**Independent Test**: Schema and model-like tests assert a shared next-action shape across representative tools, resources, prompts, and errors.

**Acceptance Scenarios**:

1. **Given** a model reads a successful workflow plan output, **When** next actions are available,
   **Then** it includes standard fields for next tool, arguments, resources to read, and approval requirements.
2. **Given** a model reads a resource output with follow-up resources, **When** navigation is available, **Then** it uses the same next-action vocabulary as tool outputs.
3. **Given** a prompt is retrieved, **When** it describes a workflow, **Then** it includes stop conditions and ask-user conditions.
4. **Given** a field cannot be safely suggested, **When** next-action metadata is generated, **Then** the field is omitted and `ask_user_when_uncertain` is set.

### User Story 10: Prompt, Completion, and Capability Transport Is Contract-Tested (Priority: P1)

The protocol transport layer should prove that prompt retrieval, completion, capability fingerprints, and descriptor-owned navigation are visible exactly where models consume them.

**Why this priority**: Factory-level tests are not enough when the model only sees transport payloads.

**Independent Test**: HTTP and STDIO contract tests call public MCP methods and assert the model-facing fields.

**Acceptance Scenarios**:

1. **Given** a prompt exists in descriptors, **When** `prompts/get` is called through transport, **Then** the returned messages include expected stop and ask-user conditions.
2. **Given** a completion target exists, **When** `completion/complete` is called through transport, **Then** values and diagnostic metadata are returned.
3. **Given** capabilities are read through a resource call, **When** navigation is included, **Then** every navigation endpoint resolves to a public identifier.
4. **Given** a descriptor fingerprint changes, **When** transport golden tests run, **Then** the change is visible in the normalized contract.

### User Story 11: Naming, Pagination, Native Signals, and Roots Feel Native (Priority: P2)

Tool names, resource pagination, native sampling, progress, logging, and permission boundaries should be clear enough that a model does not need hidden project knowledge.

**Why this priority**: These are second-order comfort issues. They do not block correctness, but they reduce token waste and wrong calls.

**Independent Test**: Descriptor lint, protocol tests, and model-confusion tests cover ambiguous names, large lists, long-running workflows, and boundary hints.

**Acceptance Scenarios**:

1. **Given** two tools can look similar, **When** descriptor lint runs, **Then** names and descriptions must distinguish action intent and side-effect level.
2. **Given** a resource or tool can return a large list, **When** it is called, **Then** pagination fields consistently show whether more data exists and how to continue.
3. **Given** a workflow can take noticeable time, **When** progress reporting is available, **Then** native progress or structured progress fields are exposed.
4. **Given** sampling or logging is available, **When** a workflow does not need it, **Then** it is not used as hidden planning, execution, or ranking.
5. **Given** file or config roots become part of MCP resources, **When** descriptors are exposed, **Then** roots or permission boundaries are explicit before access.

### User Story 12: Complex Outputs Include Examples and Confusion Tests (Priority: P2)

Complex workflow, preview, metadata, and recovery outputs should include compact examples and tests for model mistakes.

**Why this priority**: Examples and negative-path tests make the protocol easier for models to learn and harder to misuse.

**Independent Test**: Descriptor lint verifies concise examples for complex outputs, and model-confusion tests simulate common wrong orders and stale state.

**Acceptance Scenarios**:

1. **Given** a workflow plan output is described, **When** a model reads the descriptor, **Then** it can see a compact shape example without runtime secrets.
2. **Given** a model attempts apply before plan, **When** the call fails, **Then** recovery points to planning in the current session.
3. **Given** a model attempts execute without preview or approval, **When** validation runs, **Then** recovery preserves the approval boundary.
4. **Given** a model uses stale or ambiguous metadata, **When** the call fails, **Then** recovery recommends a resource read or user clarification instead of guessing.

## Functional Requirements

### Documentation Correction

- **FR-001**: README documentation MUST no longer describe prompts or completions as deferred when protocol-visible support exists.
- **FR-002**: README documentation MUST describe prompt discovery, prompt retrieval, completion usage, preview usage, and opt-in real-model E2E at a model-facing level.
- **FR-003**: Documentation MUST preserve explicit approval guidance for `execute_update`, `apply_workflow`, and any future side-effecting tool.

### Transcript Golden Contract

- **FR-010**: The project MUST include normalized golden tests for `initialize`, `tools/list`, `resources/list`, `resources/templates/list`,
  `prompts/list`, representative `completion/complete`, and `shardingsphere://capabilities`.
- **FR-011**: Golden normalization MUST remove or replace dynamic values such as session IDs, timestamps, runtime database versions, and nondeterministic ordering.
- **FR-012**: Golden tests MUST fail when model-facing descriptions, input schemas, prompt descriptors, completion targets, preview enum values,
  tool annotations, or safety metadata disappear.
- **FR-013**: Golden fixtures MUST be small enough for maintainers to review and MUST avoid locking large runtime data payloads.
- **FR-014**: Golden tests MUST use public MCP protocol surfaces rather than private Java implementation details.

### Real-Model E2E

- **FR-020**: Real-model E2E MUST be opt-in and excluded from default CI.
- **FR-021**: Real-model E2E MUST use non-production model credentials and deterministic ShardingSphere fixtures.
- **FR-022**: Real-model E2E MUST redact API keys, tokens, and credentials from logs and artifacts.
- **FR-023**: Real-model E2E MUST assert observable traces, including prompt calls, completion calls, resource reads, tool order,
  argument schema validity, preview-before-execution, approval boundaries, recovery path, and final validation.
- **FR-024**: Real-model E2E MUST NOT assert exact natural-language wording except for stable protocol fields.
- **FR-025**: Real-model E2E SHOULD cover metadata inspection, safe SQL execution, encrypt workflow, mask workflow, and at least one recovery scenario.
- **FR-026**: Real-model E2E reports MUST record provider, model identifier, prompt set version, descriptor catalog version, scenario ID,
  skipped assertions, and failure classification.

### Completion Ranking

- **FR-030**: Completion handlers MUST keep returned `values` directly reusable as argument strings.
- **FR-031**: Completion handlers MUST keep deterministic ordering for the same runtime state and request.
- **FR-032**: Completion handlers SHOULD rank exact prefix matches before broader prefix matches.
- **FR-033**: Completion handlers SHOULD rank candidates that match supplied context arguments before candidates with weaker context.
- **FR-034**: `plan_id` completions SHOULD rank current-session eligible plans by most recent update before older plans.
- **FR-035**: Algorithm completions SHOULD prefer candidates matching the prompt or resource feature context.
- **FR-036**: Completion ranking MUST NOT use cross-session history, vector search, model calls, or user behavior learning in this increment.

### Structured Recovery

- **FR-040**: Recoverable error payloads MUST use a consistent `recovery` envelope.
- **FR-041**: The `recovery` envelope MUST include `recoverable`, `category`, and `model_action` when recovery is possible.
- **FR-042**: The `recovery` envelope SHOULD include `suggested_next_tool`, `suggested_arguments`, `read_resources_first`, and `ask_user_when_uncertain` when those fields can be computed safely.
- **FR-043**: `suggested_arguments` MUST contain only server-known values or values already supplied by the user.
- **FR-044**: `suggested_arguments` MUST NOT contain secrets, guessed runtime values, placeholder values, or hidden physical objects.
- **FR-045**: Wrong-tool SQL recovery MUST preserve preview and user approval semantics.
- **FR-046**: Unavailable workflow plan recovery MUST recommend replanning in the current MCP session instead of retrying stale `plan_id` values.
- **FR-047**: Structured recovery tests MUST cover missing arguments, invalid enum values, unsupported tool, unsupported resource, wrong SQL tool choice,
  unsupported metadata object, and unavailable workflow plan.
- **FR-048**: Recovery categories MUST distinguish SQL parse error, unsupported statement, multiple statements, missing database, unsafe SQL,
  unsupported dialect, unsupported metadata object, invalid enum, wrong tool, missing execution mode, and stale workflow plan.

### Resource Navigation

- **FR-050**: The capability payload MUST expose lightweight `resourceNavigation` metadata loaded from descriptor-owned metadata.
- **FR-051**: Each navigation entry SHOULD include `from`, `to`, `requiredArguments`, `carriedArguments`, and `description`.
- **FR-052**: Navigation MUST cover metadata hierarchy, feature algorithm to planning relationships, and workflow plan to apply and validate relationships.
- **FR-053**: Navigation MUST reference only public resources, prompts, and tools.
- **FR-054**: Navigation MUST NOT introduce a runtime graph engine, automatic traversal tool, or hidden planning service in this increment.
- **FR-055**: Navigation ownership SHOULD live in descriptor YAML or equivalent descriptor input so new features can add relationships without Java catalog hardcoding.

### Explicit Side-Effect Execution

- **FR-060**: `execute_update` MUST require an explicit `execution_mode` value for every call.
- **FR-061**: Missing `execution_mode` for `execute_update` MUST be rejected as recoverable and MUST recommend `execution_mode=preview`.
- **FR-062**: `execute_update` descriptors MUST state that preview is the safe first step and that execute is side-effecting.
- **FR-063**: Direct execution compatibility defaults MUST NOT be preserved for model-facing update calls in this increment.
- **FR-064**: `apply_workflow` and future side-effecting tools MUST keep explicit preview or approval-bound execution semantics.

### Clarification and Completion Diagnostics

- **FR-070**: Missing user-only input MUST be represented through MCP-native elicitation when the SDK supports it.
- **FR-071**: When native elicitation is unavailable, missing user-only input MUST use structured fallback fields with `pending_questions`, `missing_arguments`, and `ask_user_when_uncertain`.
- **FR-072**: Completion responses SHOULD include diagnostic metadata that explains the source of ranked values and missing context when values are empty or partial.
- **FR-073**: Completion diagnostic metadata MUST NOT change the `values` contract; returned values remain directly reusable argument strings.
- **FR-074**: Completion diagnostics MUST NOT reveal hidden physical metadata, secrets, or guessed runtime values.

### Descriptor Lint and Version Fingerprints

- **FR-080**: Descriptor lint MUST validate useful title, description, input schema descriptions, enum descriptions, output schema descriptions,
  annotations, prompt links, completion targets, navigation links, and safety hints.
- **FR-081**: Descriptor lint MUST reject vague descriptions that do not identify the model action, required context, or side-effect level.
- **FR-082**: Capability payloads MUST include stable fingerprints for descriptor catalog, prompt set, navigation metadata, and model-facing schema set.
- **FR-083**: Real-model E2E reports and golden transcripts MUST record the relevant fingerprints.
- **FR-084**: Fingerprints MUST be deterministic for the same descriptor inputs and MUST ignore runtime session values.

### Unified Next-Action Contract

- **FR-090**: Tool outputs, resource outputs, prompt instructions, and recoverable errors SHOULD use the same next-action vocabulary when they guide the model.
- **FR-091**: The shared vocabulary SHOULD include `suggested_next_tool`, `suggested_arguments`, `read_resources_first`, `requires_user_approval`, and `ask_user_when_uncertain`.
- **FR-092**: Suggested arguments MUST follow the same safety rules as recovery suggested arguments.
- **FR-093**: Prompt templates MUST include stop conditions for workflows where the model should stop reading or calling tools.
- **FR-094**: Prompt templates MUST include ask-user conditions when the server cannot infer required user intent.

### Protocol Transport Contracts

- **FR-100**: Transport-level tests MUST cover `prompts/get` for at least support, encrypt, and mask prompt templates.
- **FR-101**: Transport-level tests MUST cover `completion/complete` values and diagnostic metadata for representative prompt and resource references.
- **FR-102**: Transport-level tests MUST cover capability fingerprints and descriptor-owned `resourceNavigation`.
- **FR-103**: Transport-level tests MUST verify side-effecting tools expose explicit mode requirements and safety annotations.

### Naming, Pagination, Native Signals, and Roots

- **FR-110**: Tool names and descriptions MUST distinguish read-only, preview, and side-effecting actions without relying on README context.
- **FR-111**: Resource and tool outputs that can exceed a bounded result size MUST expose consistent pagination fields,
  including whether more data exists and the token or arguments needed to continue.
- **FR-112**: Long-running workflow actions SHOULD expose MCP-native progress or structured progress fields when the current SDK supports them.
- **FR-113**: If future resources access files or configuration roots, descriptors MUST expose MCP-native roots or equivalent permission-boundary metadata before access.
- **FR-114**: Prompt argument coverage MUST be explicit: every prompt argument is either backed by completion, backed by a resource, or documented as user-provided only.
- **FR-115**: MCP-native sampling and logging SHOULD be used only when stable SDK support exists and a concrete workflow requires it;
  they MUST NOT become hidden planning, hidden execution, or completion ranking dependencies.

### Output Examples and Model-Confusion Tests

- **FR-120**: Complex output descriptors SHOULD include compact example shapes for workflow plans, SQL previews, metadata search results, completion diagnostics, and recovery envelopes.
- **FR-121**: Examples MUST be static, small, secret-free, and free of environment-specific values.
- **FR-122**: Model-confusion tests MUST cover apply-before-plan, execute-before-preview, missing execution mode, stale `plan_id`,
  unknown database, ambiguous metadata, invalid enum, and wrong SQL tool.
- **FR-123**: Confusion tests MUST assert structured recovery or next-action metadata rather than exact prose.

## Key Entities

- **Normalized Transcript Fixture**: Stable, reviewable JSON payload derived from public MCP protocol calls after dynamic fields are normalized.
- **Real-Model Scenario**: Opt-in E2E case with public prompts, completions, resources, and tools plus trace-based assertions.
- **Completion Ranking Rule**: Deterministic ordering rule that uses prefix, context, lifecycle, and feature reference without external model inference.
- **Recovery Envelope**: Structured error metadata that describes whether and how a model can safely repair a failed MCP call.
- **Resource Navigation Entry**: Descriptor-owned relationship from one public MCP resource, prompt, or tool to another.
- **Capability Fingerprint Set**: Stable hashes for descriptor, prompt, navigation, and schema inputs that identify the model-facing contract version.
- **Descriptor Lint Finding**: Reviewable validation result that identifies a descriptor clarity, schema, annotation, safety, completion, prompt, or navigation defect.
- **Next-Action Metadata**: Shared guidance fields used by outputs, prompts, and recovery to tell the model what to read, call, ask, or stop doing next.
- **Completion Diagnostic Metadata**: Optional metadata that explains candidate source, ranking reason, and missing context without changing completion values.
- **Model-Confusion Scenario**: Deterministic negative-path test that simulates a common model misuse and verifies safe recovery.

## Success Criteria

- **SC-001**: Golden tests fail when prompt, completion, preview, description, schema, or safety metadata is accidentally removed.
- **SC-002**: At least one opt-in real-model metadata scenario proves prompt/completion/resource/tool trace coverage without README-only hints.
- **SC-003**: At least one opt-in real-model side-effect scenario proves preview-before-execution and approval-boundary preservation.
- **SC-004**: Completion tests prove deterministic ordering for exact match, prefix match, contextual metadata, plan recency, and feature-specific algorithms.
- **SC-005**: Recovery tests prove a model-like caller can repair common failures from structured fields without guessing hidden values.
- **SC-006**: Side-effecting update tests prove missing execution mode is rejected and preview is the recommended repair.
- **SC-007**: Descriptor lint fails on missing descriptions, enum explanations, output schemas, safety annotations, completion targets, and navigation links.
- **SC-008**: Capability payloads, golden transcripts, and real-model E2E reports contain deterministic fingerprints for model-facing contracts.
- **SC-009**: Navigation tests prove every descriptor-owned navigation entry resolves to existing public MCP identifiers.
- **SC-010**: Prompt, completion, and capability transport tests prove model-facing fields are visible through public protocol calls.
- **SC-011**: Model-confusion tests prove unsafe or ambiguous call orders recover through structured next-action metadata.
- **SC-012**: Default CI remains deterministic and does not require real model credentials.

## Assumptions

- MCP Java SDK prompt and completion support remains available in the current dependency set.
- The existing descriptor catalog remains the source of truth for public model-facing identifiers.
- Existing MCP interaction trace infrastructure can be extended instead of replaced.
- Real-model E2E runs against non-production fixtures and credentials only.
- Session-scoped workflow plan lifecycle remains the accepted design; no durable plan storage is required for this increment.
- Native MCP elicitation, sampling, progress, logging, and roots are used only when the current SDK exposes stable server APIs;
  otherwise structured fallback fields are acceptable.
- Descriptor-owned navigation can be represented in YAML or an equivalent descriptor input without adding a graph service.
