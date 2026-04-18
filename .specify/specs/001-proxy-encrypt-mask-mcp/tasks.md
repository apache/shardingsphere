# Tasks: ShardingSphere-Proxy Encrypt and Mask MCP V1

**Input**: Design documents from `/.specify/specs/001-proxy-encrypt-mask-mcp/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `interaction-design.md`, `algorithm-parameter-design.md`, `validation-matrix.md`, `error-codes.md`, `conversation-examples.md`, `implementation-slices.md`, `acceptance-checklist.md`, `contracts/mcp-tools.md`
**Tests**: `mcp/core` 模块单测必需；必要时补充 Proxy 侧集成验证。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行
- **[Story]**: 对应用户故事
- 描述中包含建议落点文件路径

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 创建工作流包结构：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/`
  与 `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/model/workflow/`
- [ ] T002 在
  `mcp/core/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.tool.handler.ToolHandler`
  注册新的 workflow tool handlers
- [ ] T003 在
  `mcp/core/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler`
  注册 encrypt / mask rule 与 algorithm plugin resource handlers

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 实现规则与算法只读资源层：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/rule/EncryptRulesHandler.java`
  `MaskRulesHandler.java`
  `EncryptRuleHandler.java`
  `MaskRuleHandler.java`
- [ ] T005 [P] 实现插件资源层：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/plugin/EncryptAlgorithmsHandler.java`
  `MaskAlgorithmsHandler.java`
  并补齐 encrypt 能力位富化
- [ ] T006 [P] 实现统一的 DistSQL 读取服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/RuleInspectionService.java`
- [ ] T007 建立工作流请求与计划模型：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/model/workflow/WorkflowRequest.java`
  `StructuredIntent.java`
  `InteractionPlan.java`
  `AlgorithmPropertyRequirement.java`
  `WorkflowContextSnapshot.java`
  `ValidationReport.java`
- [ ] T007A [P] 建立可续做的一步一步模式上下文存储：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowContextStore.java`
  使 `planId` 能在当前服务运行期内恢复已确认上下文与当前步骤
- [ ] T008 建立命名、DDL、DistSQL 与索引工件模型：
  `DerivedColumnPlan.java`
  `RulePlan.java`
  `DDLArtifact.java`
  `IndexPlan.java`
- [ ] T009 实现统一校验与错误语义：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationService.java`
  与对应异常类
- [ ] T009A [P] 建立统一问题模型与错误码目录：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/model/workflow/WorkflowIssue.java`
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/model/workflow/WorkflowIssueCode.java`

**Checkpoint**: 已能读取现状、表达计划模型，并具备统一错误、上下文与校验骨架。

---

## Phase 3: User Story 1 - 基于结构化意图生成安全可审阅的规则计划 (Priority: P1)

**Goal**: 基于上游结构化意图和必要追问生成可审阅的 encrypt / mask 计划。
**Independent Test**: 给定缺失条件的请求，系统必须先追问；给定条件完整的请求，系统必须先返回全局步骤清单与计划工件。

### Tests for User Story 1

- [ ] T010 [P] [US1] 为计划阶段补充单测：
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/workflow/PlanEncryptMaskRuleToolHandlerTest.java`

### Implementation for User Story 1

- [ ] T011 [P] [US1] 实现算法推荐服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/AlgorithmRecommendationService.java`
  接入 plugin discovery、SPI 元数据与实例探测
- [ ] T011A [P] [US1] 实现算法参数模板与敏感参数打码服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/AlgorithmPropertyTemplateService.java`
- [ ] T012 [P] [US1] 实现命名与冲突规避服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/DerivedColumnNamingService.java`
- [ ] T013 [US1] 实现计划编排服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowPlanningService.java`
- [ ] T013A [US1] 在 `WorkflowPlanningService.java` 中接入结构化意图优先、缺失字段追问、全局步骤清单与参数采集状态推进
- [ ] T014 [US1] 实现 MCP tool：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/PlanEncryptMaskRuleToolHandler.java`

**Checkpoint**: 已能稳定产出步骤清单、追问列表、算法建议和可审阅工件。

---

## Phase 4: User Story 2 - 按执行模式应用 DDL 与完整规则生命周期变更 (Priority: P1)

**Goal**: 支持自动执行、审阅后执行、仅生成不执行三种模式，并覆盖 encrypt / mask 的 `create / alter / drop`。
**Independent Test**: 对 encrypt / mask 的 `create / alter / drop` 分别验证三种执行模式，确保 `manual-only` 不自动执行任何工件。

### Tests for User Story 2

- [ ] T015 [P] [US2] 为执行编排补充单测：
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/workflow/ApplyEncryptMaskRuleToolHandlerTest.java`

### Implementation for User Story 2

- [ ] T016 [P] [US2] 实现 DDL 工件生成服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/PhysicalDDLPlanningService.java`
  支持新增列 / 索引
- [ ] T017 [P] [US2] 实现 DistSQL 工件生成服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/RuleDistSQLPlanningService.java`
  覆盖 encrypt / mask 的 `create / alter / drop`
- [ ] T018 [US2] 实现执行编排服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowExecutionService.java`
- [ ] T018A [US2] 在 `WorkflowExecutionService.java` 中实现 `manual-only` 的 review-safe preview 与可执行工件包分离
- [ ] T019 [US2] 实现 MCP tool：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/ApplyEncryptMaskRuleToolHandler.java`

**Checkpoint**: encrypt / mask 的 `create / alter / drop` 已可按审批模式执行或仅生成工件，cleanup 由用户自行处理。

---

## Phase 5: User Story 3 - 保持逻辑视图稳定并处理命名冲突 (Priority: P1)

**Goal**: 逻辑列始终是用户主入口，物理冲突自动改名并回传最终结果。
**Independent Test**: 当 `*_cipher` 等默认名称冲突时，系统必须在计划、执行摘要、验证摘要中返回最终命名并保持一致。

### Tests for User Story 3

- [ ] T020 [P] [US3] 为命名冲突与逻辑视图摘要补充单测：
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/service/workflow/DerivedColumnNamingServiceTest.java`

### Implementation for User Story 3

- [ ] T021 [US3] 在 `DerivedColumnNamingService.java` 中实现默认命名、不复用、数字后缀冲突处理
- [ ] T022 [US3] 在 `WorkflowPlanningService.java` 与 `WorkflowExecutionService.java` 中接入最终命名回传与逻辑视图摘要

**Checkpoint**: 逻辑视图和物理命名工件已经稳定对齐。

---

## Phase 6: User Story 4 - 执行后进行四层验证与统一汇总 (Priority: P2)

**Goal**: 把 DDL、规则、逻辑元数据和逻辑 SQL 可执行性串成一份统一验证结果。
**Independent Test**: 任意执行流完成后，都能生成一份结构化验证报告，并显式暴露 mismatch。

### Tests for User Story 4

- [ ] T023 [P] [US4] 为验证服务补充单测：
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/workflow/ValidateEncryptMaskRuleToolHandlerTest.java`

### Implementation for User Story 4

- [ ] T024 [P] [US4] 实现规则验证服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/RuleStateValidationService.java`
- [ ] T025 [P] [US4] 实现逻辑元数据验证服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/LogicalMetadataValidationService.java`
- [ ] T026 [P] [US4] 实现逻辑 SQL 可执行性验证服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/SQLExecutabilityValidationService.java`
- [ ] T027 [US4] 实现 MCP tool：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/ValidateEncryptMaskRuleToolHandler.java`
- [ ] T027A [US4] 统一 `passed` / `failed` / `skipped` 输出与 mismatch 证据结构，覆盖 encrypt drop 场景

**Checkpoint**: 四层验证已形成统一报告。

---

## Phase 7: User Story 5 - 为查询型派生列生成索引建议或索引 DDL (Priority: P2)

**Goal**: 当启用辅助查询或模糊查询列时，同时规划索引。
**Independent Test**: 对需要 `assisted_query` 或 `like_query` 的加密场景，计划中必须包含索引建议或索引 DDL。

### Tests for User Story 5

- [ ] T028 [P] [US5] 为索引规划补充单测：
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/service/workflow/IndexPlanningServiceTest.java`

### Implementation for User Story 5

- [ ] T029 [P] [US5] 实现索引规划服务：
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/IndexPlanningService.java`
- [ ] T030 [US5] 在 `WorkflowPlanningService.java` 中接入索引工件
- [ ] T031 [US5] 在 `WorkflowExecutionService.java` 中接入索引 DDL 的审批与执行

**Checkpoint**: 规划工件已覆盖查询型派生列的索引需求。

---

## Phase 8: Polish & Cross-Cutting Concerns

- [ ] T032 [P] 补充 MCP 文档：
  `mcp/README.md`
  `mcp/README_ZH.md`
- [ ] T033 [P] 补充 `mcp/core` 模块内的工具 / 资源契约说明与错误码说明
- [ ] T033A [P] 基于 `conversation-examples.md` 补充工作流对话验收样例或 e2e 说明
- [ ] T034 跑通 `./mvnw -pl mcp/core -am test`，修复单测与 Checkstyle 问题
- [ ] T035 必要时补充 Proxy 端到端验收脚本或文档化验收步骤

## Dependencies & Execution Order

- Setup 必须先完成。
- Foundational 阶段阻塞全部用户故事。
- US1 是 US2、US3、US4、US5 的输入来源。
- US2 依赖 US1。
- US3 可与 US2 并行推进，但必须在执行摘要联调前收敛。
- US4 依赖 US2 的执行工件模型。
- US5 依赖 US1 的加密规划模型。
- Polish 依赖所有目标用户故事完成。
