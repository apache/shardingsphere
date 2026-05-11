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

# Eighty-Dimension Traceability Matrix

## Purpose

This file maps every independent `MPQ-080` scoring item to the concrete work, evidence, and exit condition required before
that item can be scored as `100/100`.

The 80 scores are independent. No row is satisfied by an average, a weighted subtotal, or manual confidence alone.

## Status Legend

- `Blocked`: a known failing command or missing capability prevents a 100 score.
- `Open`: implementation, test, documentation, or artifact evidence is still required.
- `Evidence`: implementation appears present, but the 100 score still requires recorded verification.
- `Risk`: the item is intentionally scoped by an approved product decision and must be recorded in the scorecard.

## Global Exit Rule

Every row must have:

- a passing command or generated artifact,
- a scorecard entry with command and exit code,
- a reviewable link to the task or test that proves the behavior,
- no unresolved deterministic failure in the affected module.

## Final Checkpoint Evidence

The mandatory MCP/MCP E2E checkpoint is scored as `100/100` on 2026-05-11.

Closure evidence:

- Real LLM run id: `20260511153115-e746552a`.
- Core scorecard: `overallScore=100`, `fullScore=true`, `nativeToolCallRate=1`, `harnessRecoveryRate=0`,
  `invalidCallRate=0`, `scenarioCount=5`.
- Extended scorecard: `overallScore=100`, `fullScore=true`, `nativeToolCallRate=1`, `harnessRecoveryRate=0`,
  `invalidCallRate=0`, `scenarioCount=7`.
- `search_metadata` now avoids unrelated child ambiguity and tolerates the query token when models accidentally include it
  in `object_types` beside a valid type.
- The LLM runner no longer turns a text-only SQL answer into an `execute_query` call; missing required tools must be issued
  as real model tool calls.
- `ToolHandlerTest`, `MCPToolArgumentsTest`, `LLMMCPConversationRunnerTest`, and `LLMUsabilityMetricCalculatorTest` pass
  with scoped Maven commands recorded in `scorecard.md`.
- The final scorecard update is recorded by `T086`.

## Product, Clarity, And Safety

### MPQ-080-001 Requirement understanding

- Status: `Open`.
- Route: `T003`, `T007`, `T086`, `T087`.
- 100 evidence: Speckit names the target user, model client, maintainer, and operator goals, then links them to executable
  verification in the scorecard.

### MPQ-080-002 Problem modeling

- Status: `Open`.
- Route: `T010`, `T013`, `T040`, `T060`, `T061`.
- 100 evidence: discovery, SQL execution, workflow planning, approval, recovery, and diagnostics have separate owners,
  focused tests, and no mixed-responsibility growth.

### MPQ-080-003 Acceptance alignment

- Status: `Evidence`.
- Route: `T007`, `T008`, `T009`, `T030` to `T035`, `T080` to `T087`.
- 100 evidence: every acceptance scenario maps to a passing unit, contract, E2E, or live LLM artifact.
- Recorded evidence: `ToolHandlerRegistryTest` descriptor-count blocker is fixed by semantic assertions and the scoped
  core command exited `0`; final full-row closure still depends on the remaining contract and final score gates.

### MPQ-080-004 Goal alignment

- Status: `Open`.
- Route: `T006`, `T060` to `T064`, `T079`, `T087`.
- 100 evidence: each patch explains the MCP product value and avoids unrelated cleanup.

### MPQ-080-005 User value

- Status: `Open`.
- Route: `T010` to `T017`, `T085`, `T085a`.
- 100 evidence: natural user tasks complete through MCP without scripted first-call hints, and live LLM artifacts prove it.
- Recorded evidence: usability scenarios now declare either `natural-task` or `protocol-contract`; natural tasks reject
  known protocol-scripting phrases and scorecards expose natural-task success separately from protocol-contract success.

### MPQ-080-006 Result correctness

- Status: `Evidence`.
- Route: `T008`, `T050` to `T053`, `T082`, `T083`.
- 100 evidence: successful responses are asserted against exact payloads or verified database state.
- Recorded evidence: the MCP core descriptor test passes, the targeted MCP E2E conversation and scorecard tests pass, and
  the live core LLM scorecard reports `overallScore=100` and `fullScore=true`.

### MPQ-080-007 Clarity

- Status: `Evidence`.
- Route: `T020`, `T021`, `T030` to `T035`, `T040` to `T047`.
- 100 evidence: capabilities, descriptors, errors, next actions, and artifacts make the safe next action clear without code
  reading.
- Recorded evidence: `HttpTransportContractE2ETest` now verifies capabilities, resource list, prompt list, tool schemas,
  prompts, completions, SQL update recovery/preview, and workflow plan/apply/validate payloads with canonical
  `next_actions` checks.
- Recorded evidence: `shardingsphere://capabilities` now places `model_first_summary` before the descriptor-heavy
  sections and covers safe first resource, metadata lookup, SQL tool selection, side-effect approval, workflow,
  completion, and recovery guidance.
- Recorded evidence: `MCPErrorConverterTest` and `HttpTransportRecoveryE2ETest` now prove model-readable recovery
  categories and canonical next paths for missing context, unsupported target, invalid enum, unsafe SQL, and stale workflow.

### MPQ-080-008 Explainability

- Status: `Evidence`.
- Route: `T040` to `T047`, `T070` to `T075`.
- 100 evidence: recovery and diagnostics expose stable machine-readable codes plus human-readable guidance.
- Recorded evidence: recovery errors now include stable `recovery_category`, human guidance, and retryable
  `suggested_arguments` where the next call can be generated without guessing.
- Recorded evidence: `RuntimeStatusHandlerTest` and `HttpTransportContractE2ETest` assert runtime diagnostic
  `current_category`, safe category lists, redaction marker, and operator next actions.

### MPQ-080-009 Uncertainty handling

- Status: `Evidence`.
- Route: `T040` to `T047`, `T050` to `T053`.
- 100 evidence: ambiguous database, schema, SQL, workflow, and runtime states fail safe or ask for clarification.
- Recorded evidence: missing database/schema context returns `missing_context` or `ambiguous` recovery, invalid enum values
  return a preview retry, unsafe SQL tool mismatch redirects to the safe preview/update path, and stale workflow IDs point
  to completion instead of guessing a plan id.
- Recorded evidence: complex SQL now fails safe through executable tests: read-only CTE is queryable, data-modifying CTE
  redirects to `execute_update` preview recovery, metadata SQL redirects to metadata resources, and banned SQL returns a
  terminal operator-action recovery.
- Recorded evidence: invalid runtime database type configuration maps to `invalid_configuration` instead of leaking the
  raw JDBC configuration.

### MPQ-080-010 Evidence sufficiency

- Status: `Open`.
- Route: `T030` to `T035`, `T080` to `T087`.
- 100 evidence: scorecard cites passing commands, artifacts, and contract fixtures for every score claim.

### MPQ-080-011 Verifiability

- Status: `Evidence`.
- Route: `T008`, `T009`, `T080` to `T087`.
- 100 evidence: a reviewer can reproduce the score from documented commands.
- Recorded evidence: the scorecard now lists scoped unit, E2E, Checkstyle, Spotless, and live LLM commands with exit code
  `0`, plus run id `20260510002520-0f366331`.

### MPQ-080-012 Risk identification

- Status: `Open`.
- Route: `T012`, `T015`, `T016`, `T070` to `T075`, `T087`.
- 100 evidence: security, compatibility, runtime, LLM variance, and CI-cost risks are recorded with mitigation or accepted scope.

### MPQ-080-013 Impact analysis

- Status: `Open`.
- Route: `T087` plus each behavior-changing task.
- 100 evidence: changed files identify the affected MCP layer, data-flow step, downstream runtime impact, and E2E impact.

### MPQ-080-014 Boundary coverage

- Status: `Evidence`.
- Route: `T040` to `T047`, `T050` to `T055`, `T070` to `T075`.
- 100 evidence: missing context, invalid enum, bad resource, SQL mismatch, stale workflow, and runtime failure are covered.
- Recorded evidence: recovery-focused unit and HTTP E2E coverage now exercises missing database, unsupported target,
  invalid execution mode, wrong SQL tool, stale workflow plan, unavailable runtime mapping, unsupported SQL, and banned SQL.
- Recorded evidence: Phase 5 HTTP E2E adds approval-required boundaries for `execute_update` and `apply_workflow`,
  plus transaction, savepoint, CTE, metadata-introspection, and banned-SQL executable boundaries.
- Recorded evidence: runtime diagnostic boundaries now cover missing JDBC driver, authentication failure, timeout,
  invalid configuration, unavailable database, and generic connection failure categories.

### MPQ-080-015 Robustness

- Status: `Evidence`.
- Route: `T016f`, `T070` to `T075`.
- 100 evidence: transient runtime and model-service readiness failures use bounded retries with useful failure artifacts.
- Recorded evidence: `LLMChatModelClient.waitUntilReady` now uses bounded backoff polling and includes attempt count,
  elapsed time, timeout, and last failure in the readiness diagnostic; MySQL JDBC readiness uses the same bounded
  backoff shape; packaged HTTP startup polling is bounded by remaining deadline; `LLMChatModelClientTest` and one enabled
  MySQL HTTP runtime smoke pass.

### MPQ-080-016 Error handling

- Status: `Evidence`.
- Route: `T040` to `T047`, `T070` to `T075`.
- 100 evidence: every expected error class has category, message, recovery path, and focused test evidence.
- Recorded evidence: `MCPErrorConverterTest` now asserts recovery categories and retry payloads for validation,
  unsupported target/resource, invalid enum, unsafe SQL, stale workflow, terminal operator action, and unavailable runtime
  classes; runtime diagnostic families remain tracked by `T070` to `T075`.
- Recorded evidence: HTTP E2E now asserts `approval_required`, `unsafe_sql`, `metadata_introspection_sql`, and
  `banned_sql_statement` recovery categories at the transport boundary.
- Recorded evidence: `RuntimeDatabaseConnectionExceptionTest`, `MCPJdbcMetadataLoaderTest`, and `MCPErrorConverterTest`
  assert safe runtime connection categories and recovery payloads, including `invalid_configuration`.

### MPQ-080-017 Security

- Status: `Evidence`.
- Route: `T050` to `T055`, `T074`.
- 100 evidence: banned SQL, unapproved side effects, token checks, origin checks, and unsafe workflow paths fail closed.
- Recorded evidence: `LLMMCPSafetyValidator` now delegates SQL classification to the production classifier; focused tests
  reject data-modifying CTEs, non-preview `execute_update`, and executable workflow actions, while allowing read-only CTE,
  `EXPLAIN ANALYZE`, preview, and `manual-only`.
- Recorded evidence: `HttpTransportApprovalSafetyE2ETest` rejects unapproved SQL and workflow side effects and verifies
  preview/manual workflow paths remain available without real execution.
- Recorded evidence: `LLMMCPSafetyValidatorTest` now rejects prompt-injection-style forged `approved_by_user=true` SQL and
  workflow execution attempts inside LLM scenarios; `HttpTransportApprovalSafetyE2ETest` separately proves explicit
  production-client approval still executes the safe no-op SQL path.

### MPQ-080-018 Privacy

- Status: `Evidence`.
- Route: `T074`, `T085a`.
- 100 evidence: diagnostics and artifacts redact credentials, bearer tokens, environment values, and stack traces.
- Recorded evidence: `HttpTransportContractE2ETest` and `PackagedDistributionSmokeE2ETest` assert runtime diagnostics do
  not contain `Authorization: Bearer`, `runtime-secret`, `jdbc:`, or Java stack trace package fragments; LLM artifact
  redaction remains covered by `LLME2EArtifactWriterTest`.

### MPQ-080-019 Consistency

- Status: `Evidence`.
- Route: `T030` to `T037`, `T040` to `T047`.
- 100 evidence: vocabulary, field naming, response modes, statuses, and next-action shapes are contract-tested.
- Recorded evidence: shared `MCPModelContractAssertions` recursively rejects legacy aliases including `target_tool`,
  `target_resource`, `required_arguments`, `action_kind`, `suggested_next_tool`, `recommended_recovery`, and
  `suggested_next_action`; `HttpTransportContractE2ETest` applies it to protocol discovery, recovery, and workflow
  payload families.

### MPQ-080-020 Documentation and handoff

- Status: `Open`.
- Route: `T087` and scorecard updates.
- 100 evidence: README, Speckit, scorecard, and tasks explain usage, limits, verification, and rollback.

## Large-Model Product Experience

### MPQ-080-021 LLM friendliness

- Status: `Open`.
- Route: `T020`, `T021`, `T010` to `T017`.
- 100 evidence: models can identify the safe first action from capabilities, descriptors, completions, and next actions.
- Recorded evidence: `model_first_summary` now gives a compact first-hop contract for the safe first resource, metadata
  discovery, SQL tool choice, preview-first side effects, workflow sequence, completion-before-guessing, and
  structured recovery.
- Recorded evidence: LLM usability scorecards now distinguish natural task success from protocol-contract success, so
  model-friendly behavior cannot be hidden inside protocol-scripted scenarios.

### MPQ-080-022 Naturalness

- Status: `Open`.
- Route: `T010` to `T017`, `T085`.
- 100 evidence: natural prompts pass without artificial tool-order instructions.
- Recorded evidence: `LLMUsabilityScenarioCatalogTest` proves core gate scenarios are `natural-task`, contain no
  `First call` prompt text, and keep protocol-contract scripting phrases out of natural tasks.

### MPQ-080-023 Controllability

- Status: `Evidence`.
- Route: `T050` to `T053`, `T053a`.
- 100 evidence: preview and explicit approval are required for side effects, and bypass attempts fail.
- Recorded evidence: `execute_update` execute mode and `apply_workflow review-then-execute` require
  `approved_by_user=true`; preview and `manual-only` stay callable, and approved workflow calls preserve explicit
  `approved_steps`.
- Recorded evidence: HTTP E2E keeps the explicit approved `execute_update` production-client path functional, with
  `response_mode=executed`, `result_kind=update_count`, `execution_mode=execute`, and zero affected rows for the no-op
  statement used by the test.

### MPQ-080-024 Context management

- Status: `Open`.
- Route: `T020`, `T040` to `T047`.
- 100 evidence: database, schema, table, column, and workflow context can be discovered, completed, and recovered.
- Recorded evidence: the compact summary points models to `shardingsphere://databases`, `search_metadata`, and MCP
  completion before guessing database, schema, table, column, index, sequence, or workflow identifiers.
- Recorded evidence: recovery next actions now preserve database/schema/SQL context on retryable tool calls and use
  completion for stale workflow `plan_id` recovery.

### MPQ-080-025 Output stability

- Status: `Evidence`.
- Route: `T030` to `T035`, `T085a`.
- 100 evidence: public payload schemas are snapshot or semantic-contract protected, and live LLM artifacts use deterministic metadata.
- Recorded evidence: HTTP contract E2E now semantically protects discovery, prompt, completion, SQL update, and workflow
  payload families from legacy public aliases and malformed `next_actions`; live LLM artifact evidence remains recorded
  under the scorecard run id.

### MPQ-080-026 Tool-call friendliness

- Status: `Evidence`.
- Route: `T022` to `T025`.
- 100 evidence: official LLM bridge tool definitions are generated from production descriptors, with focused contract tests.
- Recorded evidence: official OpenAI-compatible E2E tool definitions reuse `MCPToolDescriptor.toInputSchema()`, bootstrap adapts
  the same schema into MCP SDK `JsonSchema`, and protocol bridge actions keep separate MCP method schemas.

### MPQ-080-027 Model safety

- Status: `Evidence`.
- Route: `T050` to `T053`, `T053a`.
- 100 evidence: LLM scenarios cannot execute side effects unless explicit approval is part of the scenario.
- Recorded evidence: safety validator tests and scorecard metric tests prove direct unsafe actions are blocked and counted;
  nested approval-required `next_actions` followed by execution are also approval violations.
- Recorded evidence: forged model-supplied approval fields are rejected by the LLM harness for SQL and workflow execution,
  so prompt text cannot turn model output into approved side effects inside the live usability suite.

### MPQ-080-028 Prompt-injection defense

- Status: `Evidence`.
- Route: `T050` to `T055b`.
- 100 evidence: malicious or conflicting prompts cannot bypass SQL, workflow, approval, or resource boundaries.
- Recorded evidence: `LLMMCPSafetyValidatorTest` covers injected approval claims for `execute_update` and `apply_workflow`;
  both fail before any LLM harness tool execution even when `approved_by_user=true` appears in model arguments.
- Recorded evidence: production MCP clients still use `approved_by_user=true` as the explicit approval signal; server-verified
  human approval tokens are intentionally not introduced in this checkpoint because they require a separate protocol design.

### MPQ-080-029 Evaluation-set quality

- Status: `Open`.
- Route: `T010` to `T017`.
- 100 evidence: core and extended scenarios cover natural metadata, SQL, workflow, recovery, diagnostics, and safety.
- Recorded evidence: scenario tags now separate `natural-task` from `protocol-contract` while preserving domain tags for
  metadata, read-only SQL, side-effect preview, workflow, recovery, resource discovery, runtime diagnostics, and
  multi-database coverage.

### MPQ-080-030 Model regression detection

- Status: `Evidence`.
- Route: `T014`, `T015`, `T015e`, `T015f`, `T085a`.
- 100 evidence: scorecards detect task success, native tool-call rate, recovery rate, and approval-safety regressions.
- Recorded evidence: scorecards now expose `nativeToolCallRate` and `harnessRecoveryRate`; `isFullScore` requires native
  tool calls to reach `1` and harness recovery to remain `0`.
- Recorded evidence: scorecards now also expose `naturalTaskSuccessRate` and `protocolContractSuccessRate`; full score
  requires both classified groups to pass when present.
- Recorded evidence: approval-violation metrics now catch unsafe SQL execution, unsafe workflow execution, and execution
  following nested approval-required recovery actions.
- Recorded evidence: expected recovery categories are now part of each recovery scenario, and the metric calculator fails a
  scenario when the observed recoverable error uses the wrong category.

### MPQ-080-031 Human handoff

- Status: `Evidence`.
- Route: `T040` to `T047`, `T050` to `T053`, `T075`.
- 100 evidence: responses clearly identify human approval, operator action, or manual workflow requirements.
- Recorded evidence: HTTP workflow contract coverage exercises `apply_workflow` `preview` and `manual-only`, verifies
  approval-oriented next actions, and validates the failed manual workflow handoff path contains `recovery_guidance`.

### MPQ-080-032 Cost and latency

- Status: `Evidence`.
- Route: `T016` to `T016f`, `T085`.
- 100 evidence: live LLM gates are opt-in, bounded, locally runnable, and excluded from default CI cost.
- Recorded evidence: the live LLM suite remains behind `-Pllm-e2e`; the readiness loop now uses bounded backoff instead of
  fixed sleeps, MySQL JDBC readiness no longer waits with a fixed one-second interval, and packaged HTTP startup avoids
  oversleeping past its deadline.

## Code And Architecture

### MPQ-080-033 Code readability

- Status: `Evidence`.
- Route: `T060` to `T064`.
- 100 evidence: touched classes expose intent through names, small methods, and focused tests.
- Recorded evidence: `MCPErrorConverter` keeps one-off recovery payload assembly local and extracts only the repeated
  execution-mode recovery family; `RuntimeStatusHandler` exposes runtime diagnostics through focused helper methods.

### MPQ-080-034 Architecture clarity

- Status: `Evidence`.
- Route: `T020` to `T025`, `T060` to `T064`, `T071`.
- 100 evidence: API, support, core, feature, bootstrap, and E2E boundaries remain explicit after changes.
- Recorded evidence: the model-first summary is assembled in `mcp/support` from the existing descriptor catalog, asserted
  in `mcp/core`, snapshotted through HTTP E2E, and smoke-tested through the production H2 MCP runtime.
- Recorded evidence: runtime diagnostic categories are defined in `mcp/support`, surfaced by `mcp/core`, and verified by
  programmatic and packaged E2E tests without adding cross-layer shortcuts.

### MPQ-080-035 Decoupling

- Status: `Evidence`.
- Route: `T022` to `T025`, `T062`, `T063`.
- 100 evidence: production descriptors, bridge tools, scorecards, and harnesses avoid duplicated schema logic.
- Recorded evidence: duplicated E2E-only official tool schema construction was removed; the shared descriptor schema method
  is covered by API, bootstrap, and E2E bridge tests.

### MPQ-080-036 Cohesion

- Status: `Evidence`.
- Route: `T040` to `T048`, `T060` to `T064`.
- 100 evidence: recovery, descriptor construction, workflow execution, LLM orchestration, and diagnostics have focused owners.
- Recorded evidence: recovery categorization stays in `MCPErrorConverter`, retry-context defaults stay at the throwing
  handler/service boundary, and LLM score extraction uses a focused `LLMMCPNextActions` helper instead of duplicating
  nested `next_actions` parsing.
- Recorded evidence: `LLMMCPSafetyValidator` reuses the production `StatementClassifier` instead of carrying a separate
  string-based SQL classifier in the LLM harness.
- Recorded evidence: model-first capability contract assembly moved to `MCPModelFirstContractPayloadBuilder`, leaving the
  descriptor payload builder focused on catalog/resource payload composition.

### MPQ-080-037 Maintainability

- Status: `Evidence`.
- Route: `T048`, `T060` to `T064`.
- 100 evidence: large policy sinks are split only when extraction reduces reading cost or contract drift.
- Recorded evidence: `T048` audit kept recovery builder logic local because the converter remains readable; no speculative
  recovery-family abstraction was added.
- Recorded evidence: Phase 5 added focused tests and one production-classifier reuse point rather than introducing a new
  SQL policy abstraction in the E2E harness.
- Recorded evidence: Phase 6 kept one-off maps local and added `MCPModelFirstContractPayloadBuilderTest` for the only new
  stable contract builder.

### MPQ-080-038 Extensibility

- Status: `Evidence`.
- Route: `T020` to `T025`, `T040` to `T048`.
- 100 evidence: new tools, resources, prompts, completions, and workflows can be added through stable extension points.
- Recorded evidence: workflow planning tools in `model_first_summary` are derived from descriptor tool names prefixed
  with `plan_`, so feature-specific planning extensions remain descriptor-driven.
- Recorded evidence: recovery categories are normalized through stable error-code families, and new expected recovery
  categories can be added to scenarios without changing scorecard shape.
- Recorded evidence: runtime database connection categories are centralized on `RuntimeDatabaseConnectionException`, so
  future diagnostics can reuse the same category vocabulary across support, core, and E2E.

### MPQ-080-039 Complexity control

- Status: `Evidence`.
- Route: `T060` to `T064`; include LLM runner extraction only when required by native/recovery scoring.
- 100 evidence: orchestration classes have bounded responsibilities and explicit extraction criteria.
- Recorded evidence: the Phase 6 audit avoided splitting the whole recovery converter or descriptor builder; only stable,
  repeated contract families were extracted.

### MPQ-080-040 Abstraction quality

- Status: `Evidence`.
- Route: `T062`, `T063`.
- 100 evidence: abstractions remove real duplication and do not hide simple one-off payload logic.
- Recorded evidence: `MCPModelFirstContractPayloadBuilder` owns repeated model-first payload sections; runtime diagnostics
  remain simple maps because no repeated builder family exists yet.

### MPQ-080-041 Dependency governance

- Status: `Evidence`.
- Route: `T016a` to `T016e`, `T078`.
- 100 evidence: SDK, Tomcat, Jackson, JDBC, Docker, and LLM dependencies are scoped and justified by Maven evidence.

### MPQ-080-042 API design

- Status: `Open`.
- Route: `T008`, `T024`, `T030` to `T037`.
- 100 evidence: public descriptor, payload, exception, and runtime APIs are compact, stable, and testable.
- Recorded evidence: normalized golden contract E2E snapshots capabilities, resource discovery, resource templates,
  tool schemas, prompt/completion payloads, recovery payloads, and workflow payloads. The descriptor API now exposes the
  compact, stable `toInputSchema()` contract source used by bootstrap and E2E tests.
- Recorded evidence: the capabilities payload contract now declares `model_first_summary` as a stable field and tells
  consumers to read it before choosing metadata, SQL, or workflow calls.

### MPQ-080-043 Configuration design

- Status: `Evidence`.
- Route: `T016`, `T016f`, `T070` to `T075`.
- 100 evidence: defaults are safe, opt-ins explicit, and invalid config returns safe diagnostics.
- Recorded evidence: mismatched configured database type now throws `RuntimeDatabaseConnectionException` with
  `invalid_configuration`, and runtime status exposes that category without JDBC URL, credentials, environment, or stack
  trace details.

### MPQ-080-044 Data consistency

- Status: `Open`.
- Route: `T040` to `T047`, `T050` to `T053`.
- 100 evidence: metadata, workflow plans, approval steps, and runtime artifacts stay coherent across calls.
- Recorded evidence: retryable execution-mode errors preserve the original database, schema, SQL, and workflow `plan_id`
  where applicable, so recovery does not lose user context.
- Recorded evidence: approved workflow E2E keeps `approved_steps` explicit and proves unapproved workflow artifacts remain
  skipped instead of being applied implicitly.

### MPQ-080-045 Concurrency safety

- Status: `Open`.
- Route: `T050` to `T053`, `T071`, `T083`.
- 100 evidence: session, workflow, runtime, and artifact operations are parallel-test safe or documented as isolated.

### MPQ-080-046 Test sufficiency

- Status: `Evidence`.
- Route: `T008`, `T017`, `T024`, `T030` to `T035`, `T064`.
- 100 evidence: every public production method and model-facing contract has focused test coverage.
- Recorded evidence: the stale core descriptor test passes and targeted E2E metric, runner, client, tool-definition,
  scenario-catalog, and artifact-writer tests pass; `HttpTransportContractE2ETest` now covers resource, prompt,
  completion, SQL recovery/preview, and workflow plan/apply/validate public payload families.
  `HttpTransportGoldenContractE2ETest` now protects the same model-facing families with normalized golden fixtures.
  `MCPToolDescriptorTest`, `MCPToolSpecificationFactoryTest`, and `LLMMCPToolDefinitionFactoryTest` cover the shared
  official tool schema source and the protocol bridge separation.
  `ServerCapabilitiesHandlerTest`, `HttpTransportGoldenContractE2ETest`, and `ProductionH2AiNativeInteractionE2ETest`
  now verify the compact model-first capability summary from core, HTTP contract, and production H2 runtime angles.
  `MCPErrorConverterTest`, `ToolHandlerRegistryTest`, `LLMUsabilityMetricCalculatorTest`,
  `LLMUsabilityScenarioCatalogTest`, `HttpTransportRecoveryE2ETest`, `HttpTransportContractE2ETest`, and
  `HttpTransportGoldenContractE2ETest` now cover recovery self-healing, retry context, expected category scoring, and
  contract drift.
  `LLMMCPSafetyValidatorTest`, `ExecuteQueryTransactionE2ETest`, and `HttpTransportApprovalSafetyE2ETest` now cover
  production-classifier-backed LLM safety, complex SQL fail-safe paths, and approval boundaries.

### MPQ-080-047 Test quality

- Status: `Evidence`.
- Route: `T008`, `T017`, `T024`, `T047`.
- 100 evidence: tests use precise semantic assertions and avoid brittle counts.
- Recorded evidence: descriptor assertions now check names, required fields, value types, and enum values instead of raw
  field counts.
- Recorded evidence: recovery tests assert semantic categories, retry arguments, and followed next-action outcomes rather
  than only checking the presence of an error payload.
- Recorded evidence: SQL boundary tests assert exact statement classes, statement types, side-effect scopes, recovery
  categories, and retry arguments instead of only checking transport success.

### MPQ-080-048 Regression risk

- Status: `Evidence`.
- Route: `T008`, `T030` to `T037`, `T050` to `T055`.
- 100 evidence: contract drift, safety regressions, SQL-classification changes, and harness changes fail fast.
- Recorded evidence: native tool-call origin and harness recovery origin are asserted in focused LLM runner and scorecard
  tests; model-facing alias and canonical `next_actions` regressions now fail in `HttpTransportContractE2ETest`.
  Normalized golden fixtures now make unexpected changes to capabilities, resources, tools, prompts, completions,
  recovery, and workflow public payloads fail fast.
- Recorded evidence: expected recovery-category mismatches now fail the LLM usability scenario and increment invalid calls,
  preventing broad first-error recovery from hiding wrong error paths.
- Recorded evidence: approval-safety regressions now fail at three levels: validator tests, metric tests, and HTTP E2E
  boundary tests.
- Recorded evidence: prompt-injection regressions now fail in focused LLM safety tests, while explicit production-client
  approval behavior remains protected by HTTP E2E.

## Engineering And Operations

### MPQ-080-049 Performance

- Status: `Evidence`.
- Route: `T016f`, `T060` to `T064`, `T070` to `T075`.
- 100 evidence: MCP requests avoid unnecessary scanning, waits, and expensive harness work.
- Recorded evidence: runtime diagnostics are derived from already loaded metadata and constant category lists; no extra
  database probe or heavyweight scan is added to normal resource reads.

### MPQ-080-050 Resource usage

- Status: `Evidence`.
- Route: `T050` to `T053`, `T070` to `T075`, `T083`.
- 100 evidence: sessions, clients, containers, files, and database resources close deterministically.
- Recorded evidence: new runtime diagnostic E2E reuses the existing HTTP runtime lifecycle and packaged client
  try-with-resources paths; no long-lived client, container, or file handle was added.

### MPQ-080-051 Cost

- Status: `Evidence`.
- Route: `T016`, `T016e`, `T085`.
- 100 evidence: mandatory gates avoid paid providers, and heavy live tests remain explicitly selected.

### MPQ-080-052 Observability

- Status: `Evidence`.
- Route: `T015e`, `T052`, `T070` to `T075`.
- 100 evidence: traces record target, execution mode, approval state, classification, latency, error category, and action origin.
- Recorded evidence: `MCPInteractionTraceRecord` now records action origin with known categories for model tool call,
  protocol bridge, harness text recovery, and harness argument normalization.

### MPQ-080-053 Operability

- Status: `Evidence`.
- Route: `T070` to `T075`.
- 100 evidence: packaged HTTP and STDIO startup and failure modes have safe operator diagnostics.
- Recorded evidence: `shardingsphere://runtime` exposes safe category names and operator actions, and packaged HTTP/STDIO
  smoke tests assert those diagnostics when the distribution E2E condition is enabled.

### MPQ-080-054 Stability

- Status: `Evidence`.
- Route: `T015a` to `T015d`, `T016`, `T083`.
- 100 evidence: default tests are deterministic, and live LLM variance is isolated from deterministic assertions.
- Recorded evidence: live run `20260510002520-0f366331` produced a blocking core scorecard with `fullScore=true` and a
  separate extended scorecard that records model-performance misses without failing deterministic suite checks.

### MPQ-080-055 Fault recovery

- Status: `Evidence`.
- Route: `T040` to `T047`.
- 100 evidence: recoverable errors guide the model through exactly one primary next path.
- Recorded evidence: `HttpTransportRecoveryE2ETest` follows the primary next action for missing database, unsupported
  target, invalid enum, wrong SQL tool, and stale workflow plan; unsupported unknown resource remains unit-covered because
  the MCP SDK rejects the URI before ShardingSphere code can emit recovery guidance.

### MPQ-080-056 Gray release and rollback

- Status: `Open`.
- Route: `T016`, `T087`, README and scorecard updates.
- 100 evidence: risky behavior has opt-in gates, disable paths, and documented rollback.

### MPQ-080-057 Release readiness

- Status: `Evidence`.
- Route: `T080` to `T085`.
- 100 evidence: JDK 21 subchain CI, distribution smoke, Docker smoke, and scoped quality gates pass.
- Recorded evidence: scoped production, E2E, Checkstyle, Spotless, and live LLM commands exited `0`; full release readiness
  plus one enabled MySQL HTTP runtime smoke command exited `0`; full release readiness remains gated by final all-80
  closure and the `git diff --check`/Spotless conflict recorded under `MPQ-080-077`.

### MPQ-080-058 Reproducibility

- Status: `Open`.
- Route: `T015e`, `T085`, `T087`.
- 100 evidence: scorecards record model, model digest when available, prompt hash, descriptor hash, run id, and command evidence.

### MPQ-080-059 Auditability

- Status: `Evidence`.
- Route: `T015e`, `T074`, `T085a`.
- 100 evidence: tool calls, recoveries, approvals, diagnostics, and score decisions are traceable without leaking secrets.
- Recorded evidence: runtime diagnostic payloads now include stable categories and operator actions while secret-free
  assertions reject bearer headers, runtime secrets, JDBC URLs, and stack trace fragments.

### MPQ-080-060 Ownership

- Status: `Evidence`.
- Route: `T022` to `T025`, `T030` to `T037`, `T060` to `T064`.
- 100 evidence: every contract family and E2E suite has source ownership and update rules.
- Recorded evidence: model-contract snapshots live under `test/e2e/mcp/src/test/resources/golden/model-contract`, and
  update intent is encoded by `HttpTransportGoldenContractE2ETest` plus `assertMatchesNormalizedGoldenContract`.
  Official tool input schema ownership now lives in `MCPToolDescriptor`; E2E owns only protocol bridge method schemas.
- Recorded evidence: model-first contract ownership now sits in `MCPModelFirstContractPayloadBuilder`, and runtime
  diagnostic category ownership sits in `RuntimeDatabaseConnectionException`.

## Long-Term Quality

### MPQ-080-061 Compatibility

- Status: `Evidence`.
- Route: `T036`, `T061`, `T062`.
- 100 evidence: unreleased-contract cleanup is intentional, and remaining compatibility behavior is tested.
- Recorded evidence: runtime status only adds a new stable `diagnostics` field and leaves existing status, transport,
  readiness, redaction, resources, and next-action fields intact.

### MPQ-080-062 Migration

- Status: `Open`.
- Route: `T030` to `T035`, `T087`.
- 100 evidence: public contract changes include golden update and consumer migration notes.
- Recorded evidence: public MCP contract changes now require an intentional golden fixture update for capabilities,
  discovery, tool schemas, prompts/completion, recovery, and workflow payload families.

### MPQ-080-063 Long-term evolution

- Status: `Evidence`.
- Route: `T020` to `T025`, `T040` to `T048`, `T060` to `T064`.
- 100 evidence: growth paths for tools, resources, workflows, diagnostics, and LLM scenarios are clear.
- Recorded evidence: future runtime categories can be added through the centralized exception vocabulary and the runtime
  status diagnostic list without changing unrelated descriptor or workflow code.

### MPQ-080-064 Technical debt

- Status: `Evidence`.
- Route: `T036`, `T060` to `T064`.
- 100 evidence: stale aliases, brittle tests, unused helpers, and speculative abstractions are removed.
- Recorded evidence: Phase 6 removed repeated model-first contract assembly from the main descriptor payload builder while
  avoiding speculative abstractions for one-off payloads.

### MPQ-080-065 Knowledge transfer

- Status: `Open`.
- Route: `T087`, README and Speckit updates.
- 100 evidence: contributors can update descriptors, contracts, scenarios, and diagnostics by following docs and tests.

### MPQ-080-066 Compliance

- Status: `Evidence`.
- Route: `T081`, `T084`.
- 100 evidence: ASF headers, Checkstyle, Spotless, unit-test rules, and license expectations pass.
- Recorded evidence: scoped Checkstyle and Spotless commands pass; `git diff --check` remains blocked by the documented
  Spotless blank-line indentation conflict.
  Latest full `git diff --check` exits `2` on Spotless-preserved blank-line indentation in touched Java files.

### MPQ-080-067 Governance transparency

- Status: `Open`.
- Route: `T003`, `T007`, `T086`, `T087`.
- 100 evidence: score changes and scope decisions are recorded in scorecard, tasks, and requirements.

### MPQ-080-068 Domain fit

- Status: `Open`.
- Route: `T010` to `T017`, `T050` to `T055`, `T073`, `T076`.
- 100 evidence: SQL, rule workflows, governance metadata, and runtime diagnostics match real ShardingSphere workflows.

### MPQ-080-069 Project style

- Status: `Open`.
- Route: `T077`, `T081`, `T084`.
- 100 evidence: naming, layout, test style, assertions, and SPI usage follow ShardingSphere conventions.

### MPQ-080-070 Internationalization and accessibility

- Status: `Risk`.
- Route: `T010` to `T017`; add multilingual prompt evidence only when selected.
- 100 evidence: model-facing English contracts are clear, and selected multilingual natural prompts are covered or explicitly scoped out.

## ShardingSphere-Specific Quality

### MPQ-080-071 Module boundaries

- Status: `Evidence`.
- Route: `T020` to `T025`, `T060` to `T064`.
- 100 evidence: `api`, `support`, `core`, `features`, `bootstrap`, and E2E each own their proper layer.
- Recorded evidence: no new module or cross-layer dependency was added for the compact summary; `support` builds the
  payload, `core` exposes it, and E2E verifies the HTTP and production runtime surfaces.
- Recorded evidence: runtime diagnostic constants live in support, runtime resource assembly lives in core, and E2E
  verifies only public HTTP/packaged behavior.

### MPQ-080-072 SPI usage

- Status: `Evidence`.
- Route: `T024`, `T072`, `T080` to `T084`.
- 100 evidence: extensions load through ShardingSphere SPI mechanisms and tests verify plugin discovery.

### MPQ-080-073 SQL parsing-chain impact

- Status: `Evidence`.
- Route: `T054`, `T055`.
- 100 evidence: classifier behavior is validated against parser expectations and metadata-introspection paths.
- Recorded evidence: `ExecuteQueryTransactionE2ETest` covers read-only CTE, data-modifying CTE, transaction control,
  savepoint, metadata-introspection SQL, and banned SQL through the HTTP execution boundary.

### MPQ-080-074 Routing and rewrite impact

- Status: `Evidence`.
- Route: `T050` to `T055`.
- 100 evidence: SQL and workflow tests prove routing, rewrite, and rule behavior do not regress.
- Recorded evidence: production workflow E2E continues to cover Proxy rule apply/validate paths, while Phase 5 HTTP E2E
  covers approval and approved-step boundaries without assuming Proxy-only DistSQL execution in programmatic H2.

### MPQ-080-075 Governance mode compatibility

- Status: `Risk`.
- Route: `T087`; add cluster/governance tests only when touched.
- 100 evidence: standalone assumptions are explicit, and cluster/governance compatibility is tested or risk-scoped.

### MPQ-080-076 Dialect compatibility

- Status: `Evidence`.
- Route: `T083`, `T085`, H2 and MySQL runtime evidence.
- 100 evidence: H2 and MySQL pass mandatory evidence, and other target dialects are covered or safely rejected when touched.
- Recorded evidence: H2 programmatic runtime diagnostics pass through `HttpTransportContractE2ETest`, MySQL runtime smoke
  has passing evidence, and configured/actual database-type mismatch is safely rejected as `invalid_configuration`.

### MPQ-080-077 Checkstyle and Spotless compliance

- Status: `Risk`.
- Route: `T081`, `T084`.
- 100 evidence: scoped Checkstyle, Spotless, and diff checks pass after touched changes.
- Recorded evidence: scoped `checkstyle:check` and `spotless:check` exit `0`. `git diff --check` reports trailing whitespace
  on Spotless-formatted blank lines; removing it makes `spotless:check` fail and demand the indentation back.
  This is a tool-policy conflict, not an MCP source failure.
  Latest scoped `checkstyle:check` and `spotless:check` exit `0` after adding golden contract fixtures.
  Latest scoped `checkstyle:check` and `spotless:check` exit `0` again after the recovery self-healing changes.
  Latest full `git diff --check` exits `2` on the same documented Spotless blank-line indentation conflict.
  Latest scoped `checkstyle:check` and `spotless:check` exit `0` again after Phase 7 runtime diagnostics.

### MPQ-080-078 Maven integration

- Status: `Evidence`.
- Route: `T080`, `T082`, `T083`, `T085`.
- 100 evidence: JDK 21 subchain, distribution module, and profile gates run from documented Maven commands.
- Recorded evidence: scoped production, E2E, Checkstyle, Spotless, `-Pllm-e2e`, and enabled MySQL HTTP smoke Maven
  commands exit `0` on branch `001-shardingsphere-mcp`.
  Golden contract E2E and the combined MCP support/core/E2E regression command also exit `0`.
  Recovery-focused MCP core/E2E tests exit `0` with `Tests run: 42, Failures: 0, Errors: 0, Skipped: 0`.

### MPQ-080-079 Community reviewability

- Status: `Open`.
- Route: `T079`, `T087`.
- 100 evidence: changes remain small enough to review and explain behavior, tests, risk, and rollback.

### MPQ-080-080 Rollback and compatibility notes

- Status: `Evidence`.
- Route: `T087`, README and scorecard updates.
- 100 evidence: release notes or Speckit explain how to disable, revert, or migrate MCP behavior.
- Recorded evidence: scorecard records opt-in `-Pllm-e2e` execution, local Ollama dependency, final score gate caveats, and
  the fact that compatibility shims are unnecessary for the unreleased MCP contract.

## Immediate Work Queue

1. Done: fix `T008` and record `T009` evidence.
2. Done: implement `T015e` and `T015f` so LLM native tool calls and harness recovery cannot be conflated.
3. Done: implement `T016f` with bounded LLM readiness polling.
4. Done for the immediate blockers: re-score rows that were blocked only by stale descriptor assertions, harness conflation,
   or fixed readiness sleeps.
5. Done: implement Phase 4 recovery self-healing with deterministic HTTP E2E and recovery-category scorecard evidence.
6. Still enforced: move a row to `100/100` only after the scorecard records the command or artifact evidence.
