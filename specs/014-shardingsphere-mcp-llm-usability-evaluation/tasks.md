# Tasks: ShardingSphere MCP LLM Usability and Comfort Evaluation

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md, `contracts/llm-usability-acceptance-contract.md`

**Tests**: Include dedicated metric-calculator tests, regression-comparison tests, and one minimal usability suite over the existing LLM MCP runner.

**Organization**: Tasks are grouped by user story so objective scoring, boundary diagnosis, and regression comparison can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the usability evaluation contract before implementation starts.

- [X] T001 Add the scope, user stories, metrics, and success criteria to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/spec.md`
- [X] T002 [P] Record metric and adoption decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/research.md`
- [X] T003 [P] Capture suite, scenario, trace, and scorecard models in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/data-model.md`
- [X] T004 [P] Freeze thresholds and regression rules in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/contracts/llm-usability-acceptance-contract.md`
- [X] T005 [P] Freeze implementation strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/plan.md`
- [X] T006 [P] Add local execution targets to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/quickstart.md`

---

## Phase 2: Foundational (Shared Models and Reporting Infrastructure)

**Purpose**: Land the reusable score and comparison primitives before scenario-specific logic.

**CRITICAL**: No usability suite should land before scorecard, scenario result, and regression-budget models exist.

- [ ] T007 Add scenario modeling in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScenario.java`
- [ ] T008 [P] Add action trace modeling in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityActionRecord.java`
- [ ] T009 [P] Add trace aggregation in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityTrace.java`
- [ ] T010 [P] Add scenario-result modeling in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScenarioResult.java`
- [ ] T011 [P] Add dimension-score and scorecard models in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityDimensionScore.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScorecard.java`
- [ ] T012 [P] Add regression-budget and comparison models in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityRegressionBudget.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityComparisonResult.java`

**Checkpoint**: The repo has shared usability models independent of any single scenario pack.

---

## Phase 3: User Story 1 - 维护者可以量化模型使用 MCP 的顺滑度 (Priority: P1)

**Goal**: Add a minimal baseline scenario pack and calculate core usability metrics.

**Independent Test**: `LLMUsabilityMetricCalculatorTest` and `LLMUsabilitySuiteE2ETest`
produce a scorecard containing success rate, first correct action rate, invalid call rate,
average round trips, answer fidelity, and boundary confusion rate.

### Tests for User Story 1

- [ ] T013 [P] [US1] Add metric-calculator tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityMetricCalculatorTest.java`
- [ ] T014 [P] [US1] Add scenario-catalog tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScenarioCatalogTest.java`
- [ ] T015 [P] [US1] Add the minimal usability suite test in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilitySuiteE2ETest.java`

### Implementation for User Story 1

- [ ] T016 [US1] Add the scenario catalog in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScenarioCatalog.java`
- [ ] T017 [P] [US1] Add metric calculation logic in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityMetricCalculator.java`
- [ ] T018 [P] [US1] Extend trace capture from `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMMCPConversationRunner.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/MCPToolTraceRecord.java`
  to support usability classifications
- [ ] T019 [P] [US1] Add scorecard writing in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityReportWriter.java`

**Checkpoint**: The repo can run a minimal baseline suite and produce a usability scorecard.

---

## Phase 4: User Story 2 - 维护者可以识别 resource / tool 边界是否让模型困惑 (Priority: P1)

**Goal**: Classify wrong-entry behavior, unnecessary detours, and degraded successes.

**Independent Test**: Boundary-focused scenarios clearly distinguish ordinary failure from resource/tool boundary confusion.

### Tests for User Story 2

- [ ] T020 [P] [US2] Add boundary-classification tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityBoundaryClassificationTest.java`
- [ ] T021 [P] [US2] Extend runner tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMMCPConversationRunnerTest.java`
  to cover degraded-success and wrong-interaction-strategy paths

### Implementation for User Story 2

- [ ] T022 [US2] Add boundary confusion classification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityMetricCalculator.java`
- [ ] T023 [P] [US2] Add resource-hit and recovery-rate calculations in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityMetricCalculator.java`
- [ ] T024 [P] [US2] Add extended boundary-analysis scenarios in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityScenarioCatalog.java`

**Checkpoint**: Successful-but-awkward runs are visibly worse than clean passes, and boundary confusion is explicit.

---

## Phase 5: User Story 3 - Reviewer 可以比较一次变更是否让模型更难使用 (Priority: P2)

**Goal**: Compare candidate vs baseline runs and surface usability regression.

**Independent Test**: A comparison helper flags regressions when critical metrics cross budget thresholds.

### Tests for User Story 3

- [ ] T025 [P] [US3] Add regression-checker tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityRegressionCheckerTest.java`
- [ ] T026 [P] [US3] Add report-writer tests in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityReportWriterTest.java`

### Implementation for User Story 3

- [ ] T027 [US3] Add comparison logic in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityRegressionChecker.java`
- [ ] T028 [P] [US3] Add comparison artifact output in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/LLMUsabilityReportWriter.java`
- [ ] T029 [P] [US3] Document nightly / advisory execution notes in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

**Checkpoint**: Reviewers can compare two runs without manually diffing raw traces.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency, scoped verification, and adoption readiness.

- [ ] T030 [P] Run scoped usability helper verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T031 [P] Run the minimal usability suite in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T032 [P] Run lower-layer smoke regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T033 [P] Run scoped style checks in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [ ] T034 [P] Reconcile final notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed in this design round.
- **Foundational (Phase 2)**: Blocks all scenario execution and comparison.
- **User Story 1 (Phase 3)**: Depends on Phase 2.
- **User Story 2 (Phase 4)**: Depends on User Story 1, because boundary confusion is scored on top of a working baseline suite.
- **User Story 3 (Phase 5)**: Depends on User Story 1 and Phase 2 scorecard models.
- **Polish (Phase 6)**: Runs after the chosen slice is complete.

### Parallel Opportunities

- `T008`, `T009`, `T010`, and `T011` can run in parallel once the model shapes are frozen.
- `T013`, `T014`, and `T018` can run in parallel while the scenario catalog is being added.
- `T020`, `T021`, and `T024` can run in parallel after the minimal pack exists.
- `T025`, `T026`, and `T028` can run in parallel after the scorecard JSON shape is stable.

## Implementation Strategy

### MVP First

1. Land usability models.
2. Land the minimal baseline scenario catalog.
3. Land scorecard calculation and writing.
4. Land comparison logic.

### Incremental Delivery

1. Freeze the contract and docs.
2. Add foundational score and trace models.
3. Add the minimal suite and scorecard output.
4. Add boundary-confusion classification.
5. Add regression comparison and docs.

## Notes

- 第一轮 review 重点看三点：
  - 指标是否真的能表达 “舒服”
  - baseline 12 场景是否覆盖关键交互路径
  - regression budget 是否足够敏感但不过度噪音
