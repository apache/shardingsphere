# Tasks: ShardingSphere MCP Statement Classification Semantics

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/019-shardingsphere-mcp-statement-classification-semantics/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Include explicit classifier, facade, response-payload, and executor tests because this feature is mainly about product contract correctness.

**Organization**: Tasks are grouped by user story so `WITH` classification, write-vs-result-shape decoupling, and contract alignment can land in controlled slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Product Contract Framing)

**Purpose**: 先把位分类的产品语义写清楚，再进入实现设计。

- [ ] T001 Add `WITH` statement semantics notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md`
- [ ] T002 [P] Add classification-semantics notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T003 [P] Add detailed classification wording to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- [ ] T004 [P] Reconcile top-level MCP spec wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
- [ ] T005 [P] Reconcile response/data-model wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/data-model.md`
- [ ] T006 [P] Reconcile success payload wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

---

## Phase 2: Foundational (Semantic Classification Core)

**Purpose**: 先让系统能正确识别副作用语义，这会阻塞后续返回形状与审计对齐。

**CRITICAL**: No user story work should be considered complete until this phase is done.

- [ ] T007 Refactor semantic classification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifier.java`
- [ ] T008 [P] Extend classification output in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/ClassificationResult.java`
- [ ] T009 [P] Add or wire parser-backed semantic detection in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifier.java`
- [ ] T010 [P] Add counterexample coverage for `WITH` semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifierTest.java`

**Checkpoint**: `WITH` 不再默认等于 `QUERY`，data-modifying CTE 能被正确识别为写操作语义。

---

## Phase 3: User Story 1 - Agent 能识别 `WITH` 是不是写操作 (Priority: P1)

**Goal**: capability gate 和执行分支都基于真实副作用，而不是首关键字。

**Independent Test**: SQL Server CTE-prefixed DML 与普通查询 CTE 都能得到正确 `statement class`。

### Tests for User Story 1

- [ ] T011 [P] [US1] Add SQL Server CTE-prefixed DML cases to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifierTest.java`
- [ ] T012 [P] [US1] Add capability-gate classification assertions to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeTest.java`

### Implementation for User Story 1

- [ ] T013 [US1] Consume semantic `statement class` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacade.java`
- [ ] T014 [US1] Align audit marker generation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/audit/AuditRecorder.java`

**Checkpoint**: `WITH` 前缀写操作不再走查询治理语义。

---

## Phase 4: User Story 2 - 写操作与返回形状分开表达 (Priority: P1)

**Goal**: 支持 `DML + result_set` 这种真实产品语义，不再把返回结果集误当成只读查询。

**Independent Test**: PostgreSQL / openGauss data-modifying CTE
在成功返回中可同时表达写语义与结果集形状。

### Tests for User Story 2

- [ ] T015 [P] [US2] Add data-modifying CTE classification cases to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/StatementClassifierTest.java`
- [ ] T016 [P] [US2] Add response payload assertions to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/protocol/SQLExecutionResponseTest.java`
- [ ] T017 [P] [US2] Add executor behavior coverage to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutorTest.java`

### Implementation for User Story 2

- [ ] T018 [US2] Decouple execution branching from fixed result-shape assumptions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPJdbcStatementExecutor.java`
- [ ] T019 [US2] Extend success payload semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/response/SQLExecutionResponse.java`
- [ ] T020 [US2] Propagate semantic fields through the facade in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacade.java`

**Checkpoint**: 系统可以诚实表达 `statement_class = DML` 且 `result_kind = result_set`。

---

## Phase 5: User Story 3 - 合同、审计和文档都按语义分类对齐 (Priority: P2)

**Goal**: 把位分类话术从实现细节提升为正式产品契约。

**Independent Test**: 文档、顶层 spec、tool contract 与 audit 行为不再使用 “`WITH` = `QUERY`” 的隐含规则。

### Tests for User Story 3

- [ ] T021 [P] [US3] Add facade/audit regression assertions to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/execute/MCPSQLExecutionFacadeTest.java`

### Implementation for User Story 3

- [ ] T022 [P] [US3] Update PRD wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md`
- [ ] T023 [P] [US3] Update technical design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T024 [P] [US3] Update detailed design wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- [ ] T025 [P] [US3] Update top-level MCP spec wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
- [ ] T026 [P] [US3] Update top-level data model wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/data-model.md`
- [ ] T027 [US3] Update execute-query success contract in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

**Checkpoint**: 产品文档和执行链都统一采用语义分类口径。

---

## Phase 6: Polish & Verification

**Purpose**: 收口最后的测试、文档和风格检查。

- [ ] T028 [P] Run classifier scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T029 [P] Run facade/executor scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T030 [P] Run response-payload scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T031 [P] Run scoped style checks in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [ ] T032 [P] Reconcile final wording across `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md` and `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because capability gate 和治理路径首先需要正确读写分类。
- **User Story 2 (P1)**: Depends on US1 semantic classification being stable enough to separate write semantics from result shape.
- **User Story 3 (P2)**: Depends on US1/US2 output semantics being stable enough to document.

### Parallel Opportunities

- `T002` to `T006` can run in parallel after `T001`.
- `T008` to `T010` can run in parallel after `T007`.
- `T011` and `T012` can run in parallel for US1.
- `T015` to `T017` can run in parallel for US2.
- `T022` to `T027` can run in parallel for document alignment.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 so `WITH` 不再把写操作伪装成查询。
3. Validate that capability gate and audit consume semantic `statement class`.

### Incremental Delivery

1. 修正 `WITH` / CTE 的语义分类。
2. 拆开写语义与返回形状。
3. 对齐 success payload。
4. 对齐顶层规格与产品文档。

## Notes

- 这份任务列表刻意把“简单前缀修补”排除在推荐方案之外。
- 最重要的成功信号不是多支持了一种语法，
  而是产品终于能诚实表达写操作、副作用和返回形状。
- 如果实现阶段选择不增加 `statement_class` 到 payload，
  必须在设计评审里解释如何避免 data-modifying CTE 被误读成只读查询。
