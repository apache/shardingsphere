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

# Tasks: MCP Encrypt/Mask Scoped Scorecard 100

**Input**: `.specify/specs/020-mcp-encrypt-mask-scoped-100/spec.md`, `plan.md`, `scorecard.md`, `source-map.md`
**Prerequisites**: Current branch remains `001-shardingsphere-mcp`; branch-changing commands are forbidden.
**Tests**: Required for every Java, YAML descriptor, MCP protocol, workflow, E2E, distribution, or documentation claim that moves a dimension toward 100.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names exact primary file or package paths.
- Every coding task must start with a coverage/branch checklist and finish with scoped tests plus style checks.

## Non-Negotiable Invariants

- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not upgrade MCP Java SDK `1.1.2`.
- Do not add MCP icons or `Tool.execution`.
- Do not expand functional completeness beyond encrypt and mask.
- Do not mark a score dimension 100 without evidence.
- Do not edit generated paths such as `target/`.
- Prefer readable local cleanup over broad abstractions.
- Cover touched production public methods and reachable branches through public APIs and varied inputs.
- Do not test private methods by reflection.
- Do not make production methods public only for test coverage.
- Migrate direct static or constructor mocking only when it improves readability or reduces leak risk.
- Run Codex CLI cross-model review only with a read-only sandbox and after confirming the exact invocation.

---

## Phase 1: Scoped Baseline and Governance

- [ ] T001 [US1] Confirm branch before implementation with `git branch --show-current` and record `001-shardingsphere-mcp`.
  Path: repository root
- [ ] T002 [US1] Update `mcp/README.md` and `mcp/README_ZH.md` so SDK `1.1.2`, MCP `2025-11-25`, icons non-goal, and encrypt/mask-only scoring are explicit.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`
- [ ] T003 [US1] Reconcile historical Speckit 019 100/100 claims as previous evidence, not automatic closure for this scoped package.
  Paths: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`, `.specify/specs/020-mcp-encrypt-mask-scoped-100/scorecard.md`
- [ ] T004 [US1] Create an evidence ledger for every score dimension before implementation starts.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/scorecard.md`

**Checkpoint**: The score target is stable and irrelevant work is excluded.

---

## Phase 2: MCP Protocol Conformity 95 -> 100

- [ ] T010 [P] [US2] Add focused tests proving declared capabilities for resources, tools, prompts, and completions under SDK `1.1.2`.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactoryTest.java`
- [ ] T011 [P] [US2] Add HTTP transport tests for `2025-11-25` protocol header, POST, GET, DELETE, session id, unsupported content type, and missing header negative cases.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`
- [ ] T012 [US2] Add payload tests proving schema-conforming `structuredContent` plus serialized JSON text fallback for tool results with `outputSchema`.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtilsTest.java`
- [ ] T013 [US2] Add descriptor validation tests proving optional non-goal fields are not required for scoring and unsupported public aliases remain rejected.
  Path: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidatorTest.java`
- [ ] T014 [US2] Record that compatibility tests for non-`2025-11-25` protocol revisions are intentionally not required.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/source-map.md`

**Score closure**: MCP protocol conformity can move to 100 after T010 through T014 pass and evidence is recorded.

---

## Phase 3: Encrypt/Mask Functional Completeness 91 -> 100

- [ ] T020 [P] [US3] Build an encrypt branch matrix for create, alter, drop non-goal, missing algorithm,
  assisted query, like query, rule conflict, missing metadata, validation failure, and success.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/workflow-coverage.md`
- [ ] T021 [P] [US3] Build a mask branch matrix for create, alter, drop, algorithm missing, field semantics missing, existing rule conflict, metadata unavailable, validation failure, and success.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/workflow-coverage.md`
- [ ] T022 [P] [US3] Add or refresh encrypt descriptor, resource, prompt, completion, and planning tests.
  Path: `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/`
- [ ] T023 [P] [US3] Add or refresh mask descriptor, resource, prompt, completion, and planning tests.
  Path: `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/`
- [ ] T024 [US3] Add encrypt workflow preview, approval apply, validation layer, and recovery payload tests through public tool/workflow APIs.
  Paths: `mcp/features/encrypt/src/test/java/`, `mcp/core/src/test/java/`
- [ ] T025 [US3] Add mask workflow preview, approval apply, validation layer, drop, and recovery payload tests through public tool/workflow APIs.
  Paths: `mcp/features/mask/src/test/java/`, `mcp/core/src/test/java/`
- [ ] T026 [US3] Add product-path E2E coverage for encrypt and mask Proxy workflows where runtime infrastructure is available.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/`

**Score closure**: Functional completeness can move to 100 after branch matrices and focused unit/E2E evidence pass.

---

## Phase 4: AI Usability and MCP Ergonomics 91 -> 100

- [ ] T030 [P] [US3] Rebuild the mcp-builder evaluation XML into ten read-only, independent, complex, realistic, verifiable, and stable encrypt/mask questions.
  Path: `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`
- [ ] T031 [P] [US3] Strengthen evaluation artifact validation so shallow exact-name questions, destructive questions, and unverifiable answers fail.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/MCPBuilderEvaluationArtifactTest.java`
- [ ] T032 [US3] Test that encrypt/mask tool responses return stable `next_actions`, resource links, and reusable required arguments.
  Paths: `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`
- [ ] T033 [US3] Add prompt tests for inspect, plan encrypt, plan mask, safe SQL execution, and workflow recovery guidance.
  Paths: `mcp/support/src/test/java/`, `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`

**Score closure**: AI usability can move to 100 after evaluation and response-shape tests pass.

---

## Phase 5: Safety and Approval Control 90 -> 100

- [ ] T040 [P] [US3] Add negative tests proving side-effecting workflow apply cannot bypass preview and explicit approval.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/`
- [ ] T041 [P] [US3] Add session isolation tests for plan IDs, approvals, completion suggestions, and DELETE cleanup.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`
- [ ] T042 [US3] Add redaction tests proving secrets do not appear in elicitation, recovery payloads, workflow artifacts, or logs.
  Paths: `mcp/bootstrap/src/test/java/`, `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`
- [ ] T043 [US3] Add authorization and origin fail-closed tests for token missing, invalid token, invalid origin, and no token passthrough.
  Paths: `mcp/bootstrap/src/test/java/`, `test/e2e/mcp/src/test/java/`
- [ ] T044 [US3] Add SQL identifier and literal safety tests for generated physical DDL and DistSQL in encrypt/mask planning.
  Paths: `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`

**Score closure**: Safety can move to 100 after all approval, redaction, session, and fail-closed tests pass.

---

## Phase 6: Architecture Cleanliness and Implementation Elegance 88/89 -> 100

- [ ] T050 [P] [US4] Review workflow payload construction and extract only repeated field-name or next-action builders that improve readability.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/`, `mcp/features/encrypt/src/main/java/`, `mcp/features/mask/src/main/java/`
- [ ] T051 [P] [US4] Review feature-to-core dependencies and document or test that encrypt/mask details do not leak into bootstrap or generic descriptor code.
  Paths: `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/bootstrap`
- [ ] T052 [US4] Add architecture or boundary tests where a lightweight test can protect dependency direction without introducing a new framework.
  Path: `mcp/*/src/test/java/`
- [ ] T053 [US4] Remove dead compatibility shims or stale alias handling only when current tests prove they are unused and outside the narrowed scope.
  Paths: `mcp/support`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`
- [ ] T054 [US4] Record why broader framework extraction was rejected when local cleanup is sufficient.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/architecture-evidence.md`

**Score closure**: Architecture and elegance can move to 100 after local cleanup is verified and no over-design is introduced.

---

## Phase 7: Code Cleanliness 83 -> 100

- [ ] T060 [P] [US4] Search MCP tests for direct private method reflection and replace with public API coverage or field-only `Plugins.getMemberAccessor()` access where unavoidable.
  Command: `rg "getDeclaredMethod|setAccessible|invoke\\(" mcp test/e2e/mcp`
- [ ] T061 [P] [US4] Search MCP tests for direct `mockStatic` and `mockConstruction`; migrate practical cases to `AutoMockExtension` and document bounded exceptions.
  Command: `rg "mockStatic|mockConstruction" mcp test/e2e/mcp`
- [ ] T062 [P] [US4] Replace broad `containsString` assertions in touched MCP tests with structured assertions for JSON, schema, resource, and tool response payloads.
  Command: `rg "containsString" mcp test/e2e/mcp`
- [ ] T063 [US4] Review `CHECKSTYLE:OFF` in MCP paths and remove or document each remaining suppression.
  Command: `rg "CHECKSTYLE:OFF|CHECKSTYLE:ON" mcp test/e2e/mcp`
- [ ] T064 [US4] Run focused Checkstyle and Spotless after cleanup.
  Command: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check`

**Score closure**: Code cleanliness can move to 100 after searches show no unhandled violations and style gates pass.

---

## Phase 8: Test Coverage and Quality 84 -> 100

- [ ] T070 [US4] Create a public-method coverage map for touched production classes.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/test-coverage-map.md`
- [ ] T071 [US4] Map every utility branch and workflow branch to exactly one owning test or document it as unreachable.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/test-coverage-map.md`
- [ ] T072 [US4] Run scoped unit tests for MCP modules with specified test classes when changes are narrow.
  Command: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] T073 [US4] Run Jacoco report or check for modules whose dimension closure depends on branch coverage.
  Command: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -Djacoco.skip=false test jacoco:report`
- [ ] T074 [US5] Run default MCP E2E lane and record exit code, duration, and report paths.
  Command: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`

**Score closure**: Test coverage can move to 100 after coverage maps, tests, and Jacoco evidence are current.

---

## Phase 9: Documentation, Operations, Performance 84/87 -> 100

- [ ] T080 [P] [US5] Align docs so old `next_actions` aliases and stale AI-friendly requirements no longer conflict with validator behavior.
  Paths: `docs/mcp/`, `mcp/README.md`, `mcp/README_ZH.md`
- [ ] T081 [P] [US5] Add or refresh encrypt/mask quickstart steps for discover, plan, preview, approve apply, validate, and recover.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`, `.specify/specs/020-mcp-encrypt-mask-scoped-100/quickstart.md`
- [ ] T082 [US5] Add performance budgets for descriptor loading, metadata search, workflow planning, completion, default E2E, and distribution smoke.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/performance-budget.md`
- [ ] T083 [US5] Run packaged distribution smoke and record startup/configuration evidence when infrastructure is available.
  Paths: `distribution/mcp`, `test/e2e/mcp`
- [ ] T084 [US5] Record opt-in Proxy/MySQL/STDIO/LLM lanes separately from default-lane closure.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/e2e-evidence.md`
- [ ] T085 [US5] Document Docker/Testcontainers prerequisites for local opt-in MySQL, Proxy, STDIO, distribution, and Ollama LLM lanes.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/e2e-evidence.md`
- [ ] T086 [US5] Change LLM E2E runtime support so score-closing LLM lanes always start Docker-owned Ollama.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/OllamaLLMRuntimeSupport.java`
- [ ] T087 [US5] Keep external OpenAI-compatible endpoints only behind an explicit debug mode that cannot close score evidence.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/OllamaLLMRuntimeSupport.java`
- [ ] T088 [US5] Add tests proving the default LLM lane is Docker-owned and external endpoint reuse is debug-only.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/OllamaLLMRuntimeSupportTest.java`
- [ ] T089 [US5] Update LLM docs so local reproduction no longer asks users to pre-run or configure an external LLM endpoint for score evidence.
  Paths: `mcp/README.md`, `mcp/README_ZH.md`, `.specify/specs/020-mcp-encrypt-mask-scoped-100/llm-docker-runtime-analysis.md`

**Score closure**: Documentation, operations, performance, and reliability can move to 100 after docs and evidence files are current.

---

## Phase 10: Final Score Closure

- [ ] T090 [US1] Update `scorecard.md` only after every mapped task has passing evidence.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/scorecard.md`
- [ ] T091 [US1] Run final scoped unit, E2E, Checkstyle, Spotless, and Jacoco commands required by completed tasks.
  Path: repository root
- [ ] T092 [US1] Verify the branch did not change.
  Command: `git branch --show-current`
- [ ] T093 [US1] Run `git status --short` and ensure only intentional files are modified.
  Path: repository root
- [ ] T094 [US1] Record final evidence and update all ten score dimensions to `100/100`.
  Path: `.specify/specs/020-mcp-encrypt-mask-scoped-100/scorecard.md`

## Dependencies

- Phase 1 blocks all score changes.
- Phase 2 blocks MCP protocol closure.
- Phase 3 blocks functional completeness closure.
- Phase 4 depends on stable response shapes from Phase 3.
- Phase 5 can run in parallel with Phase 3 after workflow entry points are known.
- Phases 6 and 7 should be incremental and only touch code needed for score closure.
- Phase 8 blocks final coverage and quality claims.
- Phase 9 blocks documentation, operations, and performance claims.
- Phase 10 blocks every final 100/100 claim.
