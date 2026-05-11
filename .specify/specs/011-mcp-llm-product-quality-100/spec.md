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

# Feature Specification: MCP LLM Product Quality 100

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-08
**Status**: Draft
**Input**: User asks to reframe the full MCP production modules and MCP E2E module with stricter model-facing product quality criteria, then split the work into Speckit tasks. Do not switch branches.

## Goal

Raise the whole MCP implementation from the current strict product-quality score of **78/100** to **100/100**.

This feature covers the production MCP modules and the MCP end-to-end test module:

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features`
- `mcp/bootstrap`
- `test/e2e/mcp`

The target is not another readability-only cleanup. The target is a production-grade MCP experience for large models:

- Models should know the safest first action without guessing.
- Models should recover from mistakes by following structured server guidance.
- Natural user requests should work without scripted "first call this tool" wording.
- Public model-facing contracts should be stable, compact, versioned, and testable.
- Runtime and packaged distribution failures should be diagnosable by operators.
- Code should stay readable and layered while the model-facing surface grows.

## Confirmed Decisions

- Live LLM E2E gates use one mandatory model: Dockerized Ollama with Alibaba Qwen model id `qwen3:1.7b`.
- All MCP LLM E2E suites that require a live model must use that model for the 100-point gate.
- MCP E2E owns the Dockerized Ollama lifecycle for the live gate.
- MCP E2E must automatically pull `qwen3:1.7b` when the model is absent.
- The live gate uses the local Ollama OpenAI-compatible endpoint and does not require a paid external API key.
- Multi-provider or multi-model results are optional evidence, not a hard gate.
- Natural task prompts may be rewritten completely.
- Backward compatibility is not required because the MCP contract is unreleased.
- Golden contract protection should use JSON snapshots plus focused schema/assertion helpers.
- Packaged HTTP diagnostics are mandatory; STDIO diagnostics are covered where practical without making the suite too heavy.
- `preview` is always allowed for side-effecting SQL and workflows.
- Real execution requires an explicit approval signal.
- LLM usability scenarios must not perform real side effects by default.
- Extended LLM scenarios are non-blocking only for model-performance outcomes.
  Deterministic checks such as environment readiness, contract shape, safety blocking,
  artifact validity, score range, and secret leakage MUST remain hard assertions.
- All 80 independent score items are mandatory for MCP production and MCP E2E unless an exception is explicitly recorded and approved.
- A 100 score requires automated command or artifact evidence; manual review can support the decision but cannot be the only evidence.
- Native model tool calls and harness recovery behavior must be scored separately.
- Live LLM E2E remains opt-in for default PR CI.
- H2 and MySQL are mandatory dialect/runtime evidence for the 100-point target.
- PostgreSQL and openGauss are optional follow-up evidence unless a touched change directly targets them.
- Standalone MCP runtime is mandatory evidence; cluster or registry-governance compatibility is recorded as a risk unless touched.
- Refactoring is allowed only in small reviewable slices that reduce reading cost, contract drift, or test ambiguity.
- Final delivery must update code, tests, Speckit, scorecard, and usage or rollback documentation where the changed behavior needs handoff.

## Strict Score Baseline

The original strict score baseline was **78/100**.
The current implementation checkpoint score is **88/100**.

This score is intentionally lower than the previous design-clarity score.
The previous score measured whether code responsibilities were cleaned up.
This score measures whether MCP behaves like a polished product surface for real LLM clients.

## Latest Independent Review Baseline

The latest 80-dimension review uses independent 100-point gates for every dimension instead of one additive total.

- MCP production modules current average: **78/100**.
- MCP E2E current average: **76/100**.
- The target is for every independent dimension to reach **100/100**, not for the average to reach 100.
- Detailed 80-dimension requirements live in `eighty-dimension-requirements.md`.
- Resolved verification blocker: `ToolHandlerRegistryTest` now uses semantic descriptor assertions and passes.
- Resolved LLM scoring risk: native model tool calls and harness recovery are recorded and scored separately.
- Resolved E2E stability risk: live-model readiness now uses bounded polling with structured failure diagnostics.
- Additional readiness cleanup: MySQL JDBC readiness and packaged HTTP startup polling are bounded by remaining deadlines.
- Remaining final-gate caveat: `git diff --check` conflicts with Spotless blank-line indentation for touched Java files;
  scoped `checkstyle:check` and `spotless:check` pass.

## Implementation Evidence

- `ToolHandlerRegistryTest` passes in the scoped MCP core command.
- Targeted MCP E2E conversation, scorecard, tool-definition, scenario-catalog, artifact-writer, and LLM client tests pass.
- Scoped MCP Checkstyle and Spotless gates pass.
- Opt-in live LLM suite with Dockerized Ollama `qwen3:1.7b` passes.
- Enabled MySQL HTTP runtime smoke for `assertReadCapabilitiesWithActualMySQLBackend` passes.
- Phase 6 code-boundary tests and Phase 7 runtime diagnostic tests pass, including programmatic HTTP runtime diagnostics.
- Packaged HTTP and STDIO runtime diagnostic smoke assertions are implemented and remain opt-in through the existing
  distribution E2E condition.
- Prompt-injection safety tests prove the LLM harness rejects forged side-effect approvals, and HTTP E2E separately proves
  the production-client explicit approval path remains functional.
- Live run id: `20260510002520-0f366331`.
- Core live scorecard result: `overallScore=100`, `fullScore=true`, `nativeToolCallRate=1`, `harnessRecoveryRate=0`.
- Extended live scorecard result: `overallScore=92.6190476190476`, `fullScore=false`, deterministic assertions passed.

## 100-Point Score Model

- LLM use friendliness and zero-guessing: **10 points**, current **8/10**
- Interaction naturalness: **8 points**, current **6/8**
- Semantic clarity: **8 points**, current **7.5/8**
- Code readability: **10 points**, current **8.5/10**
- Architecture clarity: **10 points**, current **8.5/10**
- Decoupling: **8 points**, current **7/8**
- Protocol contract completeness and drift protection: **10 points**, current **9/10**
- Error recovery and diagnostics: **8 points**, current **8/8**
- Safety and approval boundaries: **8 points**, current **8/8**
- Test credibility for real LLM behavior: **10 points**, current **8.5/10**
- Evolvability: **5 points**, current **4.5/5**
- Operations and release readiness: **5 points**, current **4.5/5**

This weighted model is retained as historical context for the strict product-quality score.
The active requirement sweep uses the independent 80-dimension model in `eighty-dimension-requirements.md`.

## User Stories & Tests

### User Story 1 - Model Solves Natural Tasks Without Scripted Hints (Priority: P1)

A model receives a natural user task such as "check whether orders exist",
"inspect order metadata", or "prepare a masking rule for status".
The model should discover the right MCP path through compact capabilities,
descriptors, completions, and next actions without being told the exact first tool call.

**Independent Test**: Run a live opt-in LLM usability suite with natural task prompts,
no scripted first-call instruction, and assert task success, first-action accuracy,
invalid-call rate, average turns, and recovery success.

**Acceptance Scenarios**:

1. **Given** a natural metadata request, **When** the model has only the MCP bridge tools,
   **Then** it discovers capabilities or databases and resolves the correct resource or search tool without guessing object names.
2. **Given** a natural read-only SQL task, **When** the model chooses a SQL execution tool, **Then** it uses `execute_query` and returns a verified answer.
3. **Given** a natural side-effect task, **When** the model reaches a side-effecting operation, **Then** it previews first and asks for user approval before execution.

### User Story 1a - Extended Scenarios Record Model Variance Without Hiding Deterministic Failures (Priority: P1)

An extended LLM scenario may be harder than the core gate, such as Chinese natural tasks,
multi-database disambiguation, complex SQL, large metadata continuation, or multi-step workflow recovery.
The suite should record model-performance scores without blocking the build, but deterministic test failures must still fail.

**Independent Test**: Run extended scenarios and assert that the scorecard, traces, artifacts, safety checks,
contract checks, and secret checks are valid even when model task performance is low.

**Acceptance Scenarios**:

1. **Given** an extended scenario where the model chooses a suboptimal path, **When** the run finishes,
   **Then** the scenario records a lower score without failing the suite.
2. **Given** an extended scenario where artifact writing, score serialization, safety blocking, or secret redaction fails,
   **When** the run finishes, **Then** the suite fails because the failure is deterministic infrastructure behavior.
3. **Given** an extended scenario where the model attempts real side effects without approval, **When** the safety layer observes it,
   **Then** the action is blocked and recorded as an approval violation; if it is not blocked, the suite fails.

### User Story 2 - Model Recovers From Mistakes With One Structured Path (Priority: P1)

A model may omit an argument, choose an unsupported resource, use the wrong SQL tool,
lose workflow context, or provide an invalid enum.
The server response should make the next safe step obvious and machine-readable.

**Independent Test**: Run recovery scenarios where the first action is intentionally wrong, then assert the model follows `recovery.next_actions` and succeeds without extra invalid calls.

**Acceptance Scenarios**:

1. **Given** a missing `database`, **When** a tool rejects the call, **Then** recovery points to the nearest readable resource or completion path and the model retries with a valid database.
2. **Given** a wrong resource URI, **When** the resource read fails, **Then** recovery returns supported alternatives and a safe first resource.
3. **Given** a stale or unknown `plan_id`, **When** workflow execution fails, **Then** recovery guides the model through completion, workflow resource lookup, or re-planning.

### User Story 3 - Maintainer Can Protect Model Contracts From Drift (Priority: P1)

A maintainer changes a tool, resource, prompt, error payload, or workflow response. Contract drift should fail fast before it reaches runtime or live model tests.

**Independent Test**: Run golden contract tests for capabilities, tools, resources, prompts, completions, error recovery, and workflow payloads.

**Acceptance Scenarios**:

1. **Given** a descriptor field is renamed, **When** contract tests run, **Then** the change is either accepted through an explicit golden update or rejected.
2. **Given** a new public payload field is added, **When** schema validation runs, **Then** the field appears in the relevant contract model or descriptor.
3. **Given** a next action shape changes, **When** recovery tests run, **Then** required canonical fields are still present and old aliases are not reintroduced.

### User Story 4 - Operator Diagnoses Runtime Problems Without Reading Code (Priority: P2)

An operator starts packaged MCP with HTTP or STDIO and sees a failure such as missing driver,
authentication failure, invalid configuration, unavailable database, bad token, or transport mismatch.
The system should expose safe diagnostics without leaking secrets.

**Independent Test**: Run packaged and programmatic E2E scenarios for configuration and runtime failure categories, then assert safe error categories, no secret leakage, and useful next actions.

**Acceptance Scenarios**:

1. **Given** a missing JDBC driver, **When** runtime starts or a database is inspected, **Then** the error category identifies the driver problem and points to operator action.
2. **Given** bad credentials, **When** runtime cannot connect, **Then** the diagnostic response is safe and does not expose credentials or raw environment values.
3. **Given** HTTP access-token or origin validation fails, **When** a client calls the endpoint, **Then** the response is actionable and does not disclose secrets.

## Requirements

### Functional Requirements

- **MPQ-FR-001**: This feature MUST remain on branch `001-shardingsphere-mcp`; no task may run branch creation, `git switch`, or `git checkout`.
- **MPQ-FR-002**: The strict scorecard MUST distinguish design-clarity completion from model-facing product quality.
- **MPQ-FR-003**: The capability catalog MUST provide a compact model-first summary that can be consumed before the full descriptor catalog.
- **MPQ-FR-004**: Natural LLM usability scenarios MUST avoid scripted first-call instructions except in explicit protocol contract tests.
- **MPQ-FR-005**: The LLM usability suite MUST report task success, first correct action rate,
  invalid call rate, average round trips, recovery success, resource hit rate, approval violations,
  and final-answer fidelity.
- **MPQ-FR-005a**: Core LLM scenarios MUST fail the suite unless every scored assertion reaches the configured full-score gate.
- **MPQ-FR-005b**: Extended LLM scenarios MUST NOT fail the suite for model-performance misses such as task failure,
  suboptimal first action, extra recoverable calls, higher round trips, incomplete recovery, or answer-fidelity misses.
- **MPQ-FR-005c**: Extended LLM scenarios MUST fail the suite for deterministic failures, including invalid scorecard shape,
  missing artifacts, out-of-range scores, malformed traces, secret leakage, unblocked side effects, contract drift,
  model setup failure, MCP runtime setup failure, and invalid scenario definitions.
- **MPQ-FR-006**: Live LLM usability tests MUST remain opt-in for default CI.
  The mandatory 100-point gate MUST use Dockerized Ollama with model id `qwen3:1.7b`.
  MCP E2E MUST start or reuse the Ollama container, pull the model when absent,
  and expose an OpenAI-compatible local endpoint without paid external API keys.
- **MPQ-FR-007**: Model-facing tool definitions MUST be generated from production descriptors or a shared contract source, not manually duplicated in E2E-only bridge code.
- **MPQ-FR-008**: Public payload contracts MUST have golden tests or schema snapshot tests for capabilities, tools, resources, prompts, completions, errors, and workflow outputs.
- **MPQ-FR-009**: Public payload fields MUST use a clear canonical vocabulary.
  Protocol-required camelCase and ShardingSphere-owned snake_case fields MUST be documented by machine-readable contract, not prose only.
- **MPQ-FR-010**: Recovery payloads MUST provide one primary next path per error category and avoid multiple equally plausible actions unless a user decision is required.
- **MPQ-FR-011**: Recovery categories MUST be stable enough for tests and model metrics.
  Explicit categories MUST cover missing context, unsupported target, invalid enum, unsafe SQL, stale workflow,
  unavailable runtime, and terminal operator action.
- **MPQ-FR-012**: Completion MUST help fill database, schema, table, column, index, sequence, and workflow `plan_id` from current runtime context.
- **MPQ-FR-013**: Side-effecting SQL and workflow execution MUST allow `preview` without approval.
  Real execution MUST require an explicit approval signal before execution.
  LLM harnesses MUST reject model-supplied forged approvals; production MCP clients remain responsible for providing truthful
  explicit approval unless a future server-verified approval token design is accepted.
- **MPQ-FR-014**: SQL safety checks MUST be covered by dialect-aware examples or documented unsupported cases; complex SQL must fail safe.
- **MPQ-FR-015**: Model-facing response maps that recur across modules SHOULD be backed by small typed payload builders or contract factories when that reduces drift.
- **MPQ-FR-016**: `MCPErrorConverter` and catalog payload construction MUST not become unbounded policy sinks; new recovery and catalog families need named boundaries.
- **MPQ-FR-017**: E2E tests MUST separate natural LLM usability, protocol contract, runtime smoke, packaged distribution, and negative diagnostics.
- **MPQ-FR-018**: Packaged distribution smoke MUST cover secret-free startup diagnostics for HTTP and STDIO where practical.
- **MPQ-FR-019**: Runtime status and error diagnostics MUST not expose JDBC credentials, bearer tokens, raw environment variables, or stack traces.
- **MPQ-FR-020**: New abstractions MUST be introduced only when they reduce concrete reading cost, reduce model contract drift, or make tests stricter.
- **MPQ-FR-021**: No requirement may be satisfied only by JavaDoc, ordinary comments, README text, or final-answer explanation.
- **MPQ-FR-022**: The final 100 score MUST be earned by passing commands and recorded evidence, not by aspirational scoring.

### Non-Functional Requirements

- **MPQ-NFR-001**: Documentation-only Speckit changes require branch verification and whitespace checks; Java changes require scoped tests and Checkstyle.
- **MPQ-NFR-002**: Default CI must not depend on external paid LLM providers.
  The required live gate depends only on Docker and the local Ollama container lifecycle.
- **MPQ-NFR-003**: Opt-in live LLM tests should fail with useful artifacts and should not write secrets to artifacts.
- **MPQ-NFR-004**: Runtime diagnostics should add negligible overhead to normal tool/resource execution.
- **MPQ-NFR-005**: The final implementation should prefer deletion or consolidation over compatibility shims because the MCP code has not been released.

## Key Entities

- **Strict Scorecard**: A weighted 100-point product-quality model that includes model experience, code quality, tests, safety, and operations.
- **Natural LLM Scenario**: A task prompt that describes user intent without prescribing exact first MCP actions.
- **Model Contract Snapshot**: A deterministic representation of a tool, resource, prompt, completion, or error payload that detects drift.
- **Recovery Path**: The primary machine-readable next step returned after a recoverable MCP error.
- **Approval Boundary**: The point where a side-effecting operation must stop until explicit user approval is available.
- **Runtime Diagnostic**: A safe, secret-free status or error payload that tells an operator what class of runtime problem occurred.

## Success Criteria

- **MPQ-SC-001**: Strict scorecard reaches **100/100** with each weighted dimension at full credit.
- **MPQ-SC-002**: Natural LLM usability suite passes its configured full-score gate across the required local scenario set
  after E2E starts Ollama and pulls `qwen3:1.7b`.
- **MPQ-SC-002a**: Extended LLM scorecards record non-blocking model-performance scores while preserving hard assertions
  for deterministic infrastructure, contract, safety, artifact, and secret checks.
- **MPQ-SC-003**: Golden contract tests fail when public model-facing fields drift without intentional snapshot updates.
- **MPQ-SC-004**: Recovery scenarios prove one recoverable error can be corrected without extra invalid calls.
- **MPQ-SC-005**: Side-effecting SQL and workflow scenarios prove preview-first and approval boundaries.
- **MPQ-SC-006**: Runtime diagnostic E2E proves missing driver, connection failure, auth failure, token/origin rejection, and packaged startup diagnostics are safe.
- **MPQ-SC-007**: No production or E2E fix relies on JavaDoc or comments for clarity.
- **MPQ-SC-008**: Final verification records branch status, scoped unit tests, MCP E2E tests, Checkstyle, `git diff --check`, and live LLM opt-in status.

## Assumptions

- The MCP public contract is still unreleased, so compatibility can yield fully to final clarity.
- The mandatory live model is Dockerized Ollama with `qwen3:1.7b`, matching the current test default.
- Docker availability is required for the full 100-point live LLM gate.
- Target topologies are standalone MCP runtime with H2/MySQL E2E coverage and packaged MCP distribution via HTTP/STDIO.
- This Speckit package is a planning artifact. It does not claim the implementation score is already 100.
