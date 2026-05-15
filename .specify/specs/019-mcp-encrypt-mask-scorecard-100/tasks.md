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

# Tasks: MCP Encrypt/Mask Scorecard 100

**Input**: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/spec.md`, `plan.md`, `source-map.md`, `scorecard.md`
**Prerequisites**: Current branch remains `001-shardingsphere-mcp`; branch-changing Speckit commands are forbidden.
**Tests**: Required for every Java, YAML descriptor, MCP protocol, workflow, E2E, distribution, or documentation claim that moves a dimension toward 100.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names the primary file or package path.
- Every coding task must start with a branch/path coverage checklist and finish with scoped tests plus style checks.

## Non-Negotiable Invariants

- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not mark any dimension 100 without command, artifact, contract, or official-source evidence.
- Functional completeness is scoped to encrypt and mask only.
- Markdown is not a mandatory MCP tool-result format.
- Existing ShardingSphere-Proxy, explicit approval, and no-data-migration boundaries remain intact.
- Generated paths such as `target/` must not be edited.

---

## Phase 1: Baseline and Governance

- [x] T001 Confirm current branch remains `001-shardingsphere-mcp` without running a branch-changing command.
  Path: repository root
- [x] T002 [P] Record official MCP and MCP Java SDK sources used by this package.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/source-map.md`
- [x] T003 [P] Record the current 84/100 baseline and 12 active dimensions.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`
- [x] T004 [P] Record Markdown as optional prompt/report readability, not a required MCP tool-result format.
  Paths: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/spec.md`, `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`
- [x] T005 [P] Add a repo-visible requirements handoff for this package.
  Path: `specs/010-mcp-encrypt-mask-scorecard-100/requirements.md`

**Checkpoint**: Current requirements are Speckit-managed without switching branches.

---

## Phase 2: Protocol and Descriptor Evidence

- [ ] T010 [P] [US4] Refresh source-driven MCP protocol evidence for lifecycle, transports, authorization, tools, resources, prompts,
  completion, pagination, structured content, output schema, and optional capabilities.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/source-map.md`
- [ ] T011 [P] [US4] Revalidate MCP Java SDK `1.1.2` support and SDK-deferred fields before any descriptor/API changes.
  Paths: `mcp/bootstrap/pom.xml`, `.specify/specs/019-mcp-encrypt-mask-scorecard-100/source-map.md`
- [ ] T012 [US4] Prove tool results use `structuredContent` plus serialized JSON text fallback and do not require Markdown.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`,
  `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtilsTest.java`
- [ ] T013 [US4] Prove unimplemented optional MCP capabilities are absent, disabled, or documented as future scope without being advertised as implemented.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`,
  `mcp/README.md`, `mcp/README_ZH.md`

---

## Phase 3: Encrypt and Mask Functional Completeness

- [ ] T020 [P] [US2] Map encrypt workflow branches to one test each: create, alter, drop limitation, algorithm missing, assisted query,
  like query, existing rule conflict, metadata unavailable, validation failure, and successful validation.
  Path: `mcp/features/encrypt/src/test/java/org/apache/shardingsphere/mcp/feature/encrypt/`
- [ ] T021 [P] [US2] Map mask workflow branches to one test each: create, alter, drop, algorithm missing, field semantics missing,
  existing rule conflict, metadata unavailable, validation failure, and successful validation.
  Path: `mcp/features/mask/src/test/java/org/apache/shardingsphere/mcp/feature/mask/`
- [ ] T022 [US2] Add or revalidate focused tests for encrypt resources, prompts, completions, descriptor validation, planning, apply, validate, and recovery payloads.
  Paths: `mcp/features/encrypt`, `mcp/core`, `mcp/bootstrap`
- [ ] T023 [US2] Add or revalidate focused tests for mask resources, prompts, completions, descriptor validation, planning, apply, validate, and recovery payloads.
  Paths: `mcp/features/mask`, `mcp/core`, `mcp/bootstrap`
- [ ] T024 [US2] Update the scorecard only after encrypt/mask functionality has passing unit and product-path E2E evidence.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`

---

## Phase 4: Safety, Correctness, and Security

- [ ] T030 [P] [US2] Prove preview-before-apply and explicit approval for all side-effecting workflow operations.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow`, `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`
- [ ] T031 [P] [US2] Prove session isolation for workflow plan IDs, approval, completion suggestions, and DELETE cleanup.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`
- [ ] T032 [US2] Prove secret-safe elicitation and redaction for encrypt/mask planning and recovery payloads.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java`, `mcp/features/encrypt`, `mcp/features/mask`
- [ ] T033 [US2] Prove OAuth/static authorization, origin policy, no-token-passthrough, and fail-closed negative cases remain current.
  Paths: `mcp/bootstrap`, `test/e2e/mcp`
- [ ] T034 [US2] Prove SQL safety and workflow SQL identifier/literal handling for encrypt/mask physical DDL and DistSQL planning.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow`, `mcp/features/encrypt`, `mcp/features/mask`

---

## Phase 5: Architecture, Elegance, and Code Cleanliness

- [ ] T040 [P] [US1] Search production code for nullable returns in MCP modules and either refactor, justify framework-required nulls, or keep code cleanliness below 100.
  Path: `mcp`
- [ ] T041 [P] [US1] Search tests for direct `mockStatic` and `mockConstruction`; migrate touched cases to AutoMockExtension where practical or document justified try-with-resources exceptions.
  Paths: `mcp`, `test/e2e/mcp`
- [ ] T042 [US1] Reduce stringly typed workflow payload duplication where it directly affects readability or testability, without introducing broad refactors.
  Paths: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow`, `mcp/features/encrypt`, `mcp/features/mask`
- [ ] T043 [US1] Review custom input-schema validation and SQL scanning boundaries; add source-backed limitations and tests instead of speculative rewrites.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java`, `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/`
- [ ] T044 [US1] Update documentation to reconcile historical 100/100 Speckit claims with this current checkpoint.
  Paths: `.specify/specs/012-mcp-scorecard-perfect-100`, `.specify/specs/016-mcp-contract-e2e-gap-triage`, `.specify/specs/019-mcp-encrypt-mask-scorecard-100`

---

## Phase 6: E2E, LLM Evaluation, and Operations

- [ ] T050 [P] [US3] Rebuild the mcp-builder evaluation XML into ten complex, read-only, independent, realistic, verifiable, and stable questions.
  Path: `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`
- [ ] T051 [P] [US3] Strengthen the evaluation artifact validator so it rejects shallow exact-name questions and missing multi-step evidence.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/MCPBuilderEvaluationArtifactTest.java`
- [ ] T052 [US3] Run and record default H2/HTTP E2E evidence.
  Path: `test/e2e/mcp`
- [ ] T053 [US3] Run and record opt-in MySQL, STDIO, distribution, packaged runtime, and LLM lanes when infrastructure is available.
  Paths: `test/e2e/mcp`, `distribution/mcp`
- [ ] T054 [US3] Add or refresh performance budgets for descriptor loading, metadata search, request scope creation, workflow planning, and E2E lane duration.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`

---

## Phase 7: Final Verification

- [ ] T060 Run scoped unit tests for touched MCP modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] T061 Run scoped E2E tests for touched product-path classes.
  Command: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`
- [ ] T062 Run Checkstyle for touched modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check`
- [ ] T063 Run Spotless check for touched modules.
  Command: `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:check`
- [ ] T064 Run branch/status verification and record that no branch switch occurred.
  Path: repository root
- [ ] T065 Update every score dimension to 100 only after T060 through T064 and all dimension-specific evidence pass.
  Path: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`

## Dependencies

- Phase 1 blocks all score changes.
- Phase 2 blocks protocol and MCP ergonomics closure.
- Phases 3 and 4 may proceed in parallel when write scopes are disjoint.
- Phase 5 should be applied incrementally after affected implementation paths are known.
- Phase 6 blocks E2E realism, operations maturity, and LLM evaluation closure.
- Phase 7 blocks every 100/100 claim.
