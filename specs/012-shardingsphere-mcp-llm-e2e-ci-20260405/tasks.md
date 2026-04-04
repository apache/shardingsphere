# Tasks: ShardingSphere MCP Minimal LLM-Driven E2E Validation

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md, contracts/llm-e2e-acceptance-contract.md

**Tests**: Include dedicated helper tests, one model-driven smoke test, and one workflow-level acceptance path.

**Organization**: Tasks are grouped by user story so the model-driven smoke path,
the deterministic assertion layer, and the CI workflow can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the isolated LLM E2E scope before implementation starts.

- [X] T001 Add the LLM E2E scope, non-goals, and success criteria to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/spec.md`
- [X] T002 [P] Record workflow, runtime, and runner decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/research.md`
- [X] T003 [P] Capture scenario, profile, trace, and artifact models in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/data-model.md`
- [X] T004 [P] Freeze the acceptance contract in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/contracts/llm-e2e-acceptance-contract.md`
- [X] T005 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/plan.md`
- [X] T006 [P] Add local reproduction notes to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/quickstart.md`

---

## Phase 2: Foundational (Shared Runner and Artifact Infrastructure)

**Purpose**: Land the minimum reusable pieces before the model-driven smoke test is added.

**CRITICAL**: No model smoke test should land before the scenario, model profile,
artifact bundle, and trace model exist.

- [ ] T007 Add scenario/profile loading in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EConfiguration.java`
- [ ] T008 [P] Add the artifact model in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EArtifactBundle.java`
- [ ] T009 [P] Add the final-answer model in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMStructuredAnswer.java`
- [ ] T010 [P] Add tool-trace modeling in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/MCPToolTraceRecord.java`
- [ ] T011 [P] Add dedicated configuration tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EConfigurationTest.java`

**Checkpoint**: The repo has shared scenario, profile, trace, and artifact models that are independent of the actual model provider.

---

## Phase 3: User Story 1 - GitHub Actions 上的真实模型 smoke 能通过 MCP 访问数据库 (Priority: P1)

**Goal**: Add one real model-driven smoke scenario that proves MCP discovery and read-only query execution against the packaged demo runtime.

**Independent Test**: `ProductionLLMSmokeE2ETest` runs against a local model service and packaged MCP distribution,
and passes only when the model actually uses discovery plus `execute_query`.

### Tests for User Story 1

- [ ] T012 [P] [US1] Add model-client helper tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMChatModelClientTest.java`
- [ ] T013 [P] [US1] Add conversation-runner tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMMCPConversationRunnerTest.java`
- [ ] T014 [P] [US1] Add the real model-driven smoke in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/ProductionLLMSmokeE2ETest.java`

### Implementation for User Story 1

- [ ] T015 [US1] Add the model-service client in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMChatModelClient.java`
- [ ] T016 [P] [US1] Add the MCP conversation loop in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMMCPConversationRunner.java`
- [ ] T017 [P] [US1] Add shared packaged-runtime startup support in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/AbstractLLMMCPE2ETest.java`
- [ ] T018 [P] [US1] Add the canonical smoke prompts in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/resources/llm/minimal-smoke-system-prompt.md`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/resources/llm/minimal-smoke-user-prompt.md`
- [ ] T019 [P] [US1] Add the expected structured answer fixture in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/resources/llm/expected/minimal-smoke-response.json`

**Checkpoint**: The repo can run one real-model smoke scenario proving discovery and read-only query over MCP.

---

## Phase 4: User Story 2 - 失败要可诊断、结果要可判定 (Priority: P1)

**Goal**: Make LLM-driven failures classifiable and reproducible rather than opaque.

**Independent Test**: Helper tests and smoke assertions prove missing tools, invalid JSON, unsafe SQL, and runtime failures become explicit reports with complete artifacts.

### Tests for User Story 2

- [ ] T020 [P] [US2] Add artifact-bundle tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EArtifactBundleTest.java`
- [ ] T021 [P] [US2] Add final-answer assertion tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMStructuredAnswerTest.java`
- [ ] T022 [P] [US2] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/ProductionLLMSmokeE2ETest.java`
  with failure classification assertions

### Implementation for User Story 2

- [ ] T023 [US2] Add assertion-report writing in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EAssertionReport.java`
- [ ] T024 [P] [US2] Add artifact writing and isolated run-directory handling in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EArtifactWriter.java`
- [ ] T025 [P] [US2] Add SQL safety and required-tool validation in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMMCPConversationRunner.java`
- [ ] T026 [P] [US2] Ensure MCP runtime logs are captured and attached from `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/AbstractLLMMCPE2ETest.java`

**Checkpoint**: Every LLM-driven failure yields a classified assertion result and a complete artifact bundle.

---

## Phase 5: User Story 3 - 资源受控且本地可复现 (Priority: P2)

**Goal**: Add an independent workflow plus local docs so the LLM smoke lane can run without destabilizing the main CI.

**Independent Test**: The dedicated workflow starts the local model service, warms it up, runs the smoke test, and uploads artifacts; the README quickstart reproduces the same contract locally.

### Tests for User Story 3

- [ ] T027 [P] [US3] Add workflow acceptance assertions by extending `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/ProductionLLMSmokeE2ETest.java`
  to honor workflow-provided environment variables

### Implementation for User Story 3

- [ ] T028 [US3] Add the independent GitHub Actions workflow in `/Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows/mcp-llm-e2e.yml`
- [ ] T029 [P] [US3] Update operator notes in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [ ] T030 [P] [US3] Update Chinese operator notes in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

**Checkpoint**: The LLM smoke lane has its own workflow, local quickstart, and operator-facing reproduction notes.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency, style, and acceptance verification.

- [ ] T031 [P] Run targeted helper verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T032 [P] Run the real model-driven smoke in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T033 [P] Run scoped style checks in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T034 [P] Reconcile final implementation notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed in this design round.
- **Foundational (Phase 2)**: Blocks the smoke test because scenario, trace, and artifacts must exist first.
- **User Story 1 (Phase 3)**: Depends on Phase 2.
- **User Story 2 (Phase 4)**: Depends on User Story 1 so failures can be classified on top of a working smoke loop.
- **User Story 3 (Phase 5)**: Depends on User Story 1 and should reuse the same smoke contract rather than inventing a second one.
- **Polish (Phase 6)**: Runs after the chosen slice is complete.

### Parallel Opportunities

- `T008`, `T009`, and `T010` can run in parallel after configuration shape is frozen.
- `T012`, `T013`, and `T018` can run in parallel while the conversation loop is being built.
- `T020`, `T021`, and `T024` can run in parallel as artifact and assertion helpers.
- `T029` and `T030` can run in parallel once the workflow contract stabilizes.

## Implementation Strategy

### MVP First

1. Land scenario/profile/artifact models.
2. Land the repository-owned model client and conversation runner.
3. Pass one real-model smoke test against the packaged demo runtime.
4. Add artifact writing and workflow orchestration.

### Incremental Delivery

1. Freeze the contract and quickstart.
2. Land foundational helper classes and tests.
3. Land the model-driven smoke loop.
4. Land failure diagnostics and isolated artifacts.
5. Land the independent workflow and docs.

## Notes

- 第一轮 review 重点是：
  - 是否真的是模型驱动
  - 是否真的是 MCP + 真实数据库
  - 是否足够轻量且可诊断
- 如果实现过程中发现模型服务选择需要单独 spike，应把 provider 细节拆出，不得破坏本轮最小 smoke contract。
