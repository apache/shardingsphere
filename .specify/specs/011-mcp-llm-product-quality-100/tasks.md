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

# Tasks: MCP LLM Product Quality 100

**Input**: `.specify/specs/011-mcp-llm-product-quality-100/`
**Target**: Raise strict score from **78/100** to **100/100**.
**Tests**: Required for Java and E2E changes. Speckit-only updates require branch verification and `git diff --check`.

## Phase 0: Governance and Baseline

- [x] T001 Confirm current branch is `001-shardingsphere-mcp` with `git branch --show-current`.
- [x] T002 Confirm no task uses branch creation, `git switch`, `git checkout`, or generated `target/` files.
- [x] T003 Record current strict score as 78/100 in `scorecard.md`.
- [x] T004 Keep the previous design-clarity score separate from this product-quality score.
- [x] T005 Confirm every implementation task maps to at least one weighted score dimension.
- [x] T006 Confirm no task treats JavaDoc, comments, README text, or final-answer prose as the implementation fix.
- [x] T007 Confirm every implementation task maps to at least one independent requirement in `eighty-dimension-requirements.md`.
- [x] T008 Fix the current `ToolHandlerRegistryTest` blocker by replacing stale field-count assertions with semantic descriptor assertions.
- [x] T009 Record the post-fix command evidence for `ToolHandlerRegistryTest` before any score increase.
- [x] T009a Record that unresolved scoring-policy questions use the recommended defaults in `eighty-dimension-requirements.md`.
- [x] T009b Treat automated command or artifact evidence as mandatory for every 100-point score claim.
- [x] T009c Track H2 and MySQL as mandatory runtime/dialect evidence and cluster/governance as risk-tracked unless touched.

## Phase 1: Natural LLM Usability Gate

- [x] T010 [P0] Split usability scenarios into natural-task scenarios and explicit protocol-contract scenarios.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalog.java`.
- [x] T011 [P0] Remove scripted "First call ..." wording from natural-task scenarios.
- [x] T012 [P0] Keep explicit first-call wording only in protocol contract tests where that is the behavior under test.
- [x] T013 [P0] Add scenario tags for natural metadata lookup, read-only SQL, side-effect preview, workflow planning, recovery, and runtime diagnostics.
- [x] T014 [P0] Extend LLM usability scoring to report natural-task success separately from protocol-contract success.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/`.
- [x] T015 [P0] Add full-score gate fields for task success, first correct action, invalid call rate, recovery success, approval violation, and final-answer fidelity.
- [x] T015e [P0] Split native model tool-call metrics from harness text-recovery metrics.
- [x] T015f [P0] Ensure harness recovery cannot make a native tool-call dimension reach 100 by itself.
- [x] T015a [P0] Split core blocking LLM scenarios from extended scored LLM scenarios.
- [x] T015b [P0] Make core scenarios fail the suite when any scored assertion misses the full-score gate.
- [x] T015c [P0] Make extended scenarios fail hard for deterministic harness, contract, safety, artifact, score-shape, and secret checks.
- [x] T015d [P0] Make extended scenarios record task success, first action, round trips, recovery, resource hit, next-action following, and answer fidelity without failing the suite.
- [x] T016 [P0] Ensure live LLM tests remain opt-in for default CI while timeout, artifact path, and run id stay configurable.
- [x] T016a [P0] Standardize every live MCP LLM E2E gate on Dockerized Ollama `qwen3:1.7b`.
- [x] T016b [P0] Add E2E-managed Dockerized Ollama startup for the live model gate.
- [x] T016c [P0] Automatically pull `qwen3:1.7b` before live LLM tests when the model is absent.
- [x] T016d [P0] Fail fast with a clear Docker/Ollama diagnostic only when Docker, the container, or the model pull cannot run.
- [x] T016e [P0] Keep the live gate local and free of paid external API key requirements.
- [x] T016f [P0] Replace fixed readiness sleeps with bounded polling behavior or record a justified deterministic exception.
- [x] T017 [P0] Add focused tests for natural scenario catalog shape and metric calculation.

## Phase 2: Model-First Discovery and Tool Definitions

- [x] T020 [P0] Add a compact model-first capability summary before the full descriptor catalog.
  Target: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`.
- [x] T021 [P0] Ensure the compact summary tells models the safe first resource, SQL tool selection, side-effect rule, workflow rule, and completion rule.
- [x] T022 [P0] Generate LLM E2E bridge tool definitions from production descriptors or a shared contract source.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPToolDefinitionFactory.java`.
- [x] T023 [P0] Remove duplicated E2E-only tool schema construction where production descriptors can be reused.
- [x] T024 [P1] Add tests proving bridge tool definitions match production descriptors for official tools.
- [x] T025 [P1] Add tests proving protocol bridge actions keep their separate MCP method schemas.

## Phase 3: Golden Contract and Schema Drift Protection

- [x] T030 [P0] Add golden contract fixtures or schema snapshots for `shardingsphere://capabilities`.
- [x] T031 [P0] Add golden contract coverage for resources and resource templates.
- [x] T032 [P0] Add golden contract coverage for tool input schemas and output schemas.
- [x] T033 [P0] Add golden contract coverage for prompts and completion targets.
- [x] T034 [P0] Add golden contract coverage for error recovery payloads and next actions.
- [x] T035 [P0] Add golden contract coverage for workflow plan, apply, and validate payloads.
- [x] T035a [P0] Add semantic HTTP contract coverage for resource list, prompt list, prompt get, completion, SQL update recovery,
  SQL update preview, and workflow plan/apply/validate payloads.
- [x] T036 [P1] Add a contract test that fails if legacy public aliases such as `target_tool`, `target_resource`, `required_arguments`, or `action_kind` return.
- [x] T037 [P1] Add contract tests for protocol camelCase versus ShardingSphere-owned snake_case fields.
- [x] T038 [P1] Document golden snapshot update rules inside test names and helper APIs, not comments.

## Phase 4: Recovery Self-Healing

- [x] T040 [P0] Refine recovery categories for missing context, unsupported target, invalid enum, unsafe SQL, stale workflow, unavailable runtime, and terminal operator action.
  Target: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`.
- [x] T041 [P0] Ensure each recoverable error exposes one primary next path unless a user choice is required.
- [x] T042 [P0] Add recovery E2E for missing database to resource or completion retry.
- [x] T043 [P0] Add recovery E2E for unsupported target to corrected supported tool retry.
  Unsupported resource URI remains converter-unit covered because the MCP SDK intercepts unknown URIs before ShardingSphere handlers.
- [x] T044 [P0] Add recovery E2E for invalid enum to suggested value retry.
- [x] T045 [P0] Add recovery E2E for wrong SQL tool to corrected tool retry.
- [x] T046 [P0] Add recovery E2E for stale or unknown `plan_id` to completion, resource lookup, or re-plan.
- [x] T047 [P1] Add LLM metric assertions that one expected recoverable error is not counted as an invalid call.
- [x] T048 [P1] Audit recovery-family builders; keep `MCPErrorConverter` local because extraction would add indirection without reducing reading cost.

## Phase 5: Safety and Approval Boundaries

- [x] T050 [P0] Add natural side-effect SQL scenarios that require preview before execution.
- [x] T051 [P0] Add negative tests proving `execute_update` execution without explicit approval is rejected or not reachable from model scenarios.
- [x] T052 [P0] Add workflow apply scenarios for `preview`, `manual-only`, and explicit approved execution paths.
- [x] T053 [P0] Add approval-violation metrics for SQL and workflow tool calls.
- [x] T053a [P0] Ensure extended scenarios hard-fail if unapproved real side effects are not blocked.
- [x] T054 [P1] Add complex SQL classification examples for CTE, transaction, savepoint, metadata introspection, and unsupported statements.
- [x] T055 [P1] Document unsupported complex SQL as fail-safe behavior through executable tests, not comments.
- [x] T055a [P0] Add explicit prompt-injection tests proving LLM harness rejects forged side-effect approvals.
- [x] T055b [P0] Add HTTP E2E coverage for the production-client explicit approval path without conflating it with LLM harness safety.

## Phase 6: Code Boundary Hardening

- [x] T060 [P1] Re-audit `MCPErrorConverter` after recovery tasks and split only stable recovery families.
- [x] T061 [P1] Re-audit `MCPDescriptorCatalogPayloadBuilder` after contract tasks and split only stable model-contract families.
- [x] T062 [P1] Replace recurring raw map builders with typed payload builders where contract drift risk is real.
- [x] T063 [P1] Keep one-off payload assembly local when extraction would add indirection without reducing reading cost.
- [x] T064 [P1] Add focused tests for any new typed builders or contract factories.

## Phase 7: Runtime Diagnostics and Packaged Readiness

- [x] T070 [P2] Add safe runtime diagnostic categories for missing JDBC driver, authentication failure, connection timeout, invalid configuration, and unavailable database.
- [x] T071 [P2] Add programmatic E2E for secret-free runtime diagnostics.
- [x] T072 [P2] Add packaged HTTP diagnostic smoke where practical.
- [x] T073 [P2] Add packaged STDIO diagnostic smoke where practical without making default E2E too heavy.
- [x] T074 [P2] Add assertions that diagnostics and LLM artifacts do not contain JDBC passwords, bearer tokens, raw environment values, or stack traces.
- [x] T075 [P2] Add operator-facing next actions for terminal runtime failures.

## Phase 8: Verification and Final Score

- [x] T080 Run `git branch --show-current` and confirm `001-shardingsphere-mcp`.
- [x] T081 Run `git diff --check` for the touched checkpoint scope after Spotless and record the documented Java
  blank-line indentation conflict where it applies.
- [x] T082 Run scoped MCP production module tests.
- [x] T083 Run scoped MCP E2E tests.
- [x] T084 Run scoped Checkstyle with `-Pcheck`.
- [x] T085 Run opt-in live LLM usability suite after E2E starts Ollama and pulls `qwen3:1.7b`; otherwise record the exact Docker/Ollama failure reason.
- [x] T085a Confirm extended scorecards are generated and deterministic extended assertions fail hard when violated.
- [x] T086 Update `scorecard.md` from 78/100 to 100/100 only after all mandatory gates pass.
- [x] T087 Update `specs/011-mcp-llm-product-quality-100/requirements.md` with final decisions and residual risks for this checkpoint.

## Dependencies

- Phase 0 blocks all implementation.
- Phase 1 and Phase 3 are the highest-priority path to measurable product quality.
- Phase 4 depends on Phase 3 contracts so recovery drift is observable.
- Phase 5 can run in parallel with Phase 4 after scenario tags exist.
- Phase 6 should run after the recovery and contract shapes stabilize.
- Phase 7 can run after the diagnostic contract shape is agreed.
- Phase 8 closes the score only after implementation and verification.
