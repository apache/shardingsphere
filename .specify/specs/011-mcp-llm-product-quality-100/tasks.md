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

- [ ] T001 Confirm current branch is `001-shardingsphere-mcp` with `git branch --show-current`.
- [ ] T002 Confirm no task uses branch creation, `git switch`, `git checkout`, or generated `target/` files.
- [ ] T003 Record current strict score as 78/100 in `scorecard.md`.
- [ ] T004 Keep the previous design-clarity score separate from this product-quality score.
- [ ] T005 Confirm every implementation task maps to at least one weighted score dimension.
- [ ] T006 Confirm no task treats JavaDoc, comments, README text, or final-answer prose as the implementation fix.

## Phase 1: Natural LLM Usability Gate

- [ ] T010 [P0] Split usability scenarios into natural-task scenarios and explicit protocol-contract scenarios.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalog.java`.
- [ ] T011 [P0] Remove scripted "First call ..." wording from natural-task scenarios.
- [ ] T012 [P0] Keep explicit first-call wording only in protocol contract tests where that is the behavior under test.
- [ ] T013 [P0] Add scenario tags for natural metadata lookup, read-only SQL, side-effect preview, workflow planning, recovery, and runtime diagnostics.
- [ ] T014 [P0] Extend LLM usability scoring to report natural-task success separately from protocol-contract success.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/`.
- [ ] T015 [P0] Add full-score gate fields for task success, first correct action, invalid call rate, recovery success, approval violation, and final-answer fidelity.
- [ ] T015a [P0] Split core blocking LLM scenarios from extended scored LLM scenarios.
- [ ] T015b [P0] Make core scenarios fail the suite when any scored assertion misses the full-score gate.
- [ ] T015c [P0] Make extended scenarios fail hard for deterministic harness, contract, safety, artifact, score-shape, and secret checks.
- [ ] T015d [P0] Make extended scenarios record task success, first action, round trips, recovery, resource hit, next-action following, and answer fidelity without failing the suite.
- [ ] T016 [P0] Ensure live LLM tests remain opt-in for default CI while timeout, artifact path, and run id stay configurable.
- [ ] T016a [P0] Standardize every live MCP LLM E2E gate on Dockerized Ollama `qwen3:1.7b`.
- [ ] T016b [P0] Add E2E-managed Dockerized Ollama startup for the live model gate.
- [ ] T016c [P0] Automatically pull `qwen3:1.7b` before live LLM tests when the model is absent.
- [ ] T016d [P0] Fail fast with a clear Docker/Ollama diagnostic only when Docker, the container, or the model pull cannot run.
- [ ] T016e [P0] Keep the live gate local and free of paid external API key requirements.
- [ ] T017 [P0] Add focused tests for natural scenario catalog shape and metric calculation.

## Phase 2: Model-First Discovery and Tool Definitions

- [ ] T020 [P0] Add a compact model-first capability summary before the full descriptor catalog.
  Target: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java`.
- [ ] T021 [P0] Ensure the compact summary tells models the safe first resource, SQL tool selection, side-effect rule, workflow rule, and completion rule.
- [ ] T022 [P0] Generate LLM E2E bridge tool definitions from production descriptors or a shared contract source.
  Target: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPToolDefinitionFactory.java`.
- [ ] T023 [P0] Remove duplicated E2E-only tool schema construction where production descriptors can be reused.
- [ ] T024 [P1] Add tests proving bridge tool definitions match production descriptors for official tools.
- [ ] T025 [P1] Add tests proving protocol bridge actions keep their separate MCP method schemas.

## Phase 3: Golden Contract and Schema Drift Protection

- [ ] T030 [P0] Add golden contract fixtures or schema snapshots for `shardingsphere://capabilities`.
- [ ] T031 [P0] Add golden contract coverage for resources and resource templates.
- [ ] T032 [P0] Add golden contract coverage for tool input schemas and output schemas.
- [ ] T033 [P0] Add golden contract coverage for prompts and completion targets.
- [ ] T034 [P0] Add golden contract coverage for error recovery payloads and next actions.
- [ ] T035 [P0] Add golden contract coverage for workflow plan, apply, and validate payloads.
- [ ] T036 [P1] Add a contract test that fails if legacy public aliases such as `target_tool`, `target_resource`, `required_arguments`, or `action_kind` return.
- [ ] T037 [P1] Add contract tests for protocol camelCase versus ShardingSphere-owned snake_case fields.
- [ ] T038 [P1] Document golden snapshot update rules inside test names and helper APIs, not comments.

## Phase 4: Recovery Self-Healing

- [ ] T040 [P0] Refine recovery categories for missing context, unsupported target, invalid enum, unsafe SQL, stale workflow, unavailable runtime, and terminal operator action.
  Target: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`.
- [ ] T041 [P0] Ensure each recoverable error exposes one primary next path unless a user choice is required.
- [ ] T042 [P0] Add recovery E2E for missing database to resource or completion retry.
- [ ] T043 [P0] Add recovery E2E for unsupported resource to supported resource retry.
- [ ] T044 [P0] Add recovery E2E for invalid enum to suggested value retry.
- [ ] T045 [P0] Add recovery E2E for wrong SQL tool to corrected tool retry.
- [ ] T046 [P0] Add recovery E2E for stale or unknown `plan_id` to completion, resource lookup, or re-plan.
- [ ] T047 [P1] Add LLM metric assertions that one expected recoverable error is not counted as an invalid call.
- [ ] T048 [P1] Extract recovery-family builders only if `MCPErrorConverter` grows beyond clear local reasoning.

## Phase 5: Safety and Approval Boundaries

- [ ] T050 [P0] Add natural side-effect SQL scenarios that require preview before execution.
- [ ] T051 [P0] Add negative tests proving `execute_update` execution without explicit approval is rejected or not reachable from model scenarios.
- [ ] T052 [P0] Add workflow apply scenarios for `preview`, `manual-only`, and explicit approved execution paths.
- [ ] T053 [P0] Add approval-violation metrics for SQL and workflow tool calls.
- [ ] T053a [P0] Ensure extended scenarios hard-fail if unapproved real side effects are not blocked.
- [ ] T054 [P1] Add complex SQL classification examples for CTE, transaction, savepoint, metadata introspection, and unsupported statements.
- [ ] T055 [P1] Document unsupported complex SQL as fail-safe behavior through executable tests, not comments.

## Phase 6: Code Boundary Hardening

- [ ] T060 [P1] Re-audit `MCPErrorConverter` after recovery tasks and split only stable recovery families.
- [ ] T061 [P1] Re-audit `MCPDescriptorCatalogPayloadBuilder` after contract tasks and split only stable model-contract families.
- [ ] T062 [P1] Replace recurring raw map builders with typed payload builders where contract drift risk is real.
- [ ] T063 [P1] Keep one-off payload assembly local when extraction would add indirection without reducing reading cost.
- [ ] T064 [P1] Add focused tests for any new typed builders or contract factories.

## Phase 7: Runtime Diagnostics and Packaged Readiness

- [ ] T070 [P2] Add safe runtime diagnostic categories for missing JDBC driver, authentication failure, connection timeout, invalid configuration, and unavailable database.
- [ ] T071 [P2] Add programmatic E2E for secret-free runtime diagnostics.
- [ ] T072 [P2] Add packaged HTTP diagnostic smoke where practical.
- [ ] T073 [P2] Add packaged STDIO diagnostic smoke where practical without making default E2E too heavy.
- [ ] T074 [P2] Add assertions that diagnostics and LLM artifacts do not contain JDBC passwords, bearer tokens, raw environment values, or stack traces.
- [ ] T075 [P2] Add operator-facing next actions for terminal runtime failures.

## Phase 8: Verification and Final Score

- [ ] T080 Run `git branch --show-current` and confirm `001-shardingsphere-mcp`.
- [ ] T081 Run `git diff --check`.
- [ ] T082 Run scoped MCP production module tests.
- [ ] T083 Run scoped MCP E2E tests.
- [ ] T084 Run scoped Checkstyle with `-Pcheck`.
- [ ] T085 Run opt-in live LLM usability suite after E2E starts Ollama and pulls `qwen3:1.7b`; otherwise record the exact Docker/Ollama failure reason.
- [ ] T085a Confirm extended scorecards are generated and deterministic extended assertions fail hard when violated.
- [ ] T086 Update `scorecard.md` from 78/100 to 100/100 only after all mandatory gates pass.
- [ ] T087 Update `specs/007-mcp-llm-product-quality-100/requirements.md` with final decisions and residual risks.

## Dependencies

- Phase 0 blocks all implementation.
- Phase 1 and Phase 3 are the highest-priority path to measurable product quality.
- Phase 4 depends on Phase 3 contracts so recovery drift is observable.
- Phase 5 can run in parallel with Phase 4 after scenario tags exist.
- Phase 6 should run after the recovery and contract shapes stabilize.
- Phase 7 can run after the diagnostic contract shape is agreed.
- Phase 8 closes the score only after implementation and verification.
