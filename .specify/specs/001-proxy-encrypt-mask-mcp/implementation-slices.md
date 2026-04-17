# Detailed Design: Implementation Slices

## 1. 目标

这份文档把 `tasks.md` 的任务清单进一步压缩成可并行实施的实现切片。

它主要解决四个问题：

- 哪些能力可以独立开工；
- 哪些文件范围应尽量由同一实现切片负责；
- 哪些切片是阻塞项；
- 每个切片完成后，最小验收结果是什么。

## 2. 切片原则

- 先打通只读面，再做规划，再做执行，再做验证。
- 优先让每个切片拥有清晰的写入边界，减少并行冲突。
- 每个切片都要有独立可见的交付物，不做纯中间层空转。
- 优先按用户价值和实现依赖切片，不按代码层次机械切片。

## 3. 推荐实施顺序

1. Slice A：只读资源与工作流骨架
2. Slice B：规划模型、错误语义与会话上下文
3. Slice C：算法推荐与参数采集
4. Slice D：命名、DDL、DistSQL 与索引规划
5. Slice E：执行编排与 `manual-only`
6. Slice F：四层验证与统一总结
7. Slice G：文档、对话验收与 e2e 收口

其中：

- `Slice A + Slice B` 是所有后续切片的基础阻塞项。
- `Slice C` 与 `Slice D` 可以在 `Slice B` 完成后并行推进。
- `Slice E` 依赖 `Slice C + Slice D`。
- `Slice F` 依赖 `Slice E` 的工件模型。
- `Slice G` 最后收口。

## 4. 实现切片

### 4.1 Slice A：只读资源与工作流骨架

**目标**

- 先让 MCP 能看见当前规则、插件和目标逻辑元数据。
- 同时建立 workflow tool / resource 的基础注册点。

**对应任务**

- `T001`
- `T002`
- `T003`
- `T004`
- `T005`
- `T006`

**主要写入范围**

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/rule/`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/plugin/`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/RuleInspectionService.java`
- `mcp/core/src/main/resources/META-INF/services/`

**完成标准**

- 能读取指定逻辑库的 encrypt rules
- 能读取指定逻辑库的 mask rules
- 能读取当前 Proxy 可见的 encrypt / mask 插件列表
- 读路径全部基于 Proxy 拓扑

**最小验证**

- 资源 handler 单测
- `RuleInspectionService` 单测

### 4.2 Slice B：规划模型、错误语义与会话上下文

**目标**

- 建立后续所有规划、执行、验证都要共用的数据模型。
- 统一 `issues`、错误码、状态快照和一步一步模式上下文。

**对应任务**

- `T007`
- `T007A`
- `T008`
- `T009`
- `T009A`

**主要写入范围**

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/model/workflow/`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowContextStore.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowValidationService.java`

**完成标准**

- 规划、执行、验证都共用统一模型
- `step-by-step` 模式可保存上下文快照
- `issues` 和错误码可被所有 tool 复用

**最小验证**

- 模型序列化/反序列化测试
- `WorkflowContextStore` 单测
- 错误码映射单测

### 4.3 Slice C：算法推荐与参数采集

**目标**

- 打通自然语言意图到算法候选池、推荐结果、参数模板、敏感参数打码的完整路径。

**对应任务**

- `T010`
- `T011`
- `T011A`
- `T013`
- `T013A`
- `T014`

**主要写入范围**

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/AlgorithmRecommendationService.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/AlgorithmPropertyTemplateService.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/service/workflow/WorkflowPlanningService.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/workflow/PlanEncryptMaskRuleToolHandler.java`

**完成标准**

- 能识别 encrypt / mask
- 能根据能力需求推荐算法
- 能处理内置算法和自定义 SPI 算法
- 能在算法确定后再采集参数
- 能对敏感参数做 review-safe 打码

**最小验证**

- `PlanEncryptMaskRuleToolHandlerTest`
- 算法推荐排序测试
- 缺失属性追问测试

### 4.4 Slice D：命名、DDL、DistSQL 与索引规划

**目标**

- 把 planning 从“算法建议”推进到“可执行工件”。

**对应任务**

- `T012`
- `T016`
- `T017`
- `T020`
- `T021`
- `T029`
- `T030`

**主要写入范围**

- `DerivedColumnNamingService.java`
- `PhysicalDDLPlanningService.java`
- `RuleDistSQLPlanningService.java`
- `IndexPlanningService.java`
- `WorkflowPlanningService.java`

**完成标准**

- 能生成默认派生列名
- 能处理冲突并回传最终命名
- 能生成 DDL / DistSQL / 索引工件
- 能区分 encrypt 与 mask 的工件差异

**最小验证**

- 命名冲突测试
- DDL 生成测试
- DistSQL 生成测试
- 索引规划测试

### 4.5 Slice E：执行编排与 `manual-only`

**目标**

- 打通 review 后执行、自动执行和手工执行三种模式。

**对应任务**

- `T015`
- `T018`
- `T018A`
- `T019`
- `T022`
- `T031`

**主要写入范围**

- `WorkflowExecutionService.java`
- `ApplyEncryptMaskRuleToolHandler.java`
- 与执行摘要相关的 planning / execution 联动代码

**完成标准**

- 能按批准顺序执行 DDL、索引 DDL、DistSQL
- `manual-only` 不执行任何工件
- `manual-only` 能分离 review-safe preview 与 executable artifact package
- 每一步都有步骤级进度输出

**最小验证**

- `ApplyEncryptMaskRuleToolHandlerTest`
- 三种执行模式测试
- 部分工件失败后的状态输出测试

### 4.6 Slice F：四层验证与统一总结

**目标**

- 让 workflow 真正具备“结束态”的可靠定义。

**对应任务**

- `T023`
- `T024`
- `T025`
- `T026`
- `T027`
- `T027A`

**主要写入范围**

- `RuleStateValidationService.java`
- `LogicalMetadataValidationService.java`
- `SQLExecutabilityValidationService.java`
- `ValidateEncryptMaskRuleToolHandler.java`

**完成标准**

- DDL、Rule、Logical Metadata、SQL Executability 四层验证全部可用
- 能输出 `passed` / `failed` / `skipped`
- mismatch 能映射到稳定错误码
- `manual-only` 未执行前不会误报成功

**最小验证**

- `ValidateEncryptMaskRuleToolHandlerTest`
- mismatch 输出测试
- `manual-only` 验证延后测试

### 4.7 Slice G：文档、对话验收与 e2e 收口

**目标**

- 把规格、实现和验收联通，便于交付与后续维护。

**对应任务**

- `T032`
- `T033`
- `T033A`
- `T034`
- `T035`

**主要写入范围**

- `mcp/README.md`
- `mcp/README_ZH.md`
- `mcp/core` 工具/资源契约文档
- e2e 验收脚本或验收说明

**完成标准**

- README 能解释工作流能力、模式和边界
- 对话样例能映射到 e2e 样例
- 模块测试与风格检查通过

**最小验证**

- `./mvnw -pl mcp/core -am test`
- 必要时 Proxy 端到端验收

## 5. 并行建议

在 `Slice A + Slice B` 完成后，推荐并行方式如下：

- Lane 1：`Slice C`
  - 负责人聚焦算法推荐、参数模板、追问逻辑
- Lane 2：`Slice D`
  - 负责人聚焦命名、DDL、DistSQL、索引工件

在 `Slice C + Slice D` 收敛后：

- Lane 3：`Slice E`
  - 聚焦执行状态机和三种执行模式
- Lane 4：`Slice F`
  - 可先搭骨架，但最终联调要等待 `Slice E` 的工件结构稳定

## 6. 风险前置

### 6.1 高风险切片

- `Slice C`
  - 因为涉及自然语言追问、插件能力富化和敏感参数处理
- `Slice E`
  - 因为涉及执行模式、部分失败和 `manual-only` 语义
- `Slice F`
  - 因为“怎么才算成功”会直接影响交付可信度

### 6.2 建议最早联调点

建议在以下节点启动第一轮联调：

- `Slice A` 完成后，先联通只读资源
- `Slice D` 完成后，先看 planning 工件是否符合预期
- `Slice E` 完成后，再做完整工作流联调

## 7. Definition of Ready

一个切片开工前，至少应满足：

- 对应规格文档已稳定
- 依赖切片已完成或有可用 mock/stub
- 写入边界清晰
- 最小验证方法已明确

## 8. Definition of Done

一个切片完成，至少应满足：

- 对应任务全部落地
- 对应单测或验收测试已补齐
- 错误语义与状态输出符合 `error-codes.md`
- 用户可见行为符合 `conversation-examples.md`
- 不引入与当前规格冲突的额外行为
