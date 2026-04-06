# Implementation Plan: ShardingSphere MCP LLM Usability and Comfort Evaluation

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-06 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/014-shardingsphere-mcp-llm-usability-evaluation/spec.md)
**Input**: Feature specification from `/specs/014-shardingsphere-mcp-llm-usability-evaluation/spec.md`

## Summary

本特性不改变 MCP contract，
而是为当前 contract 增加一层稳定的 usability evaluation：

- 用固定场景包衡量模型是否“用得舒服”
- 用 scorecard 把 `resource`、`tool`、`query` 三个维度分开评分
- 用 regression budget 判断一次变更是否让模型更难使用
- 复用现有 `test/e2e/mcp` 的真实模型和真实 runtime 能力

第一阶段只做只读 H2 / MySQL baseline，
不切换分支，不扩大到主流程 gate。

## Technical Context

**Language/Version**: Java 17 in `test/e2e/mcp`  
**Primary Dependencies**: repository-owned MCP runtime, existing LLM chat client, existing MCP tool client, JUnit 5  
**Storage**: JSON / Markdown artifacts under isolated run directories  
**Testing**: usability scenario tests, scorecard calculator tests, regression-comparison tests, existing smoke tests as lower layer  
**Target Platform**: local and CI MCP runtime using H2 / MySQL read-only scenarios  
**Project Type**: Java monorepo evaluation additions under `test/e2e/mcp` plus Speckit docs under `specs/014-*`  
**Constraints**: no branch switch; no public MCP surface changes; read-only first; reuse existing LLM E2E assets where possible  
**Scale/Scope**: `test/e2e/mcp` usability scenario modeling, scoring, reporting, regression thresholds, and docs

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  本轮只新增评估层，不改 resource、tool、query 的 public 行为。
- **Gate 2 - Readability and simplicity**: PASS  
  把 “舒服” 收敛成稳定指标，符合
  [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L8)
  到
  [CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md#L13)
  所强调的 readability、consistency、simplicity 和 abstraction。
- **Gate 3 - Traceable impact**: PASS  
  输出会包含 scenario contract、scorecard JSON、Markdown summary 和 regression 结果。
- **Gate 4 - Verification path exists**: PASS  
  现有 `test/e2e/mcp` 已经具备真实模型和真实 runtime 基础，
  可以增量叠加场景、评分和比较逻辑。
- **Gate 5 - Risk is explicit**: PASS  
  最大风险是把“舒服”定义得过于主观或过重；
  设计已将其收敛为有限指标和分阶段 adoption。

## Hard Constraint Checklist

- 不切换分支
- 第一阶段不改任何 public MCP surface
- 第一阶段只覆盖只读任务和安全拒绝场景
- 评估结果必须区分功能失败与 usability regression
- 必须把 `resource`、`tool`、`query` 分维度统计
- 必须复用现有 `test/e2e/mcp` 基础设施，避免重复造框架
- 必须输出 JSON scorecard 和 Markdown summary
- baseline 必须覆盖 H2 与 MySQL
- 第一阶段不进入强制 PR gate

## Project Structure

### Documentation (this feature)

```text
specs/014-shardingsphere-mcp-llm-usability-evaluation/
├── contracts/
│   └── llm-usability-acceptance-contract.md
├── data-model.md
├── plan.md
├── quickstart.md
├── research.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
├── LLME2EScenario.java
├── LLMMCPConversationRunner.java
├── MCPToolTraceRecord.java
├── LLME2EArtifactBundle.java
├── LLMStructuredAnswer.java
├── ProductionLLMH2SmokeE2ETest.java
└── ProductionLLMMySQLSmokeE2ETest.java
```

### Proposed Additions (implementation target)

```text
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
├── LLMUsabilityScenario.java
├── LLMUsabilityActionRecord.java
├── LLMUsabilityTrace.java
├── LLMUsabilityScenarioResult.java
├── LLMUsabilityDimensionScore.java
├── LLMUsabilityScorecard.java
├── LLMUsabilityRegressionBudget.java
├── LLMUsabilityMetricCalculator.java
├── LLMUsabilityReportWriter.java
├── LLMUsabilityRegressionChecker.java
├── LLMUsabilityScenarioCatalog.java
├── LLMUsabilityMetricCalculatorTest.java
├── LLMUsabilityRegressionCheckerTest.java
└── LLMUsabilitySuiteE2ETest.java
```

**Structure Decision**: 继续把 usability evaluation 放在 `test/e2e/mcp`，
因为它本质上是对真实模型和真实 MCP 的验收层，
不是 production runtime 的新模块。

## Design Decisions

### 1. 用可观测代理指标定义 “舒服”

- 不引入主观打分字段
- 通过成功率、首次正确动作率、无效调用率、平均往返数、
  answer fidelity 和 boundary confusion rate 表达舒适度
- “成功但很绕” 必须和 “成功且顺滑” 区分开

### 2. 保持 `resource` 与 `tool` 分开评估，而不是合并

- `resource` 用来评估 context hit、resource utility 和不必要读取
- `tool` 用来评估入口选择、参数有效性和抖动
- `query` 单独评估最终 SQL 路径和答案忠实度

### 3. 先做最小 12-scenario baseline，再做扩展场景包

- 最小场景包用于稳定回归
- 扩展场景包用于边界混淆和恢复路径分析
- 这样可以避免一开始就把评估做成资源过重的综合 benchmark

### 4. 复用现有 LLM E2E 资产，不重写主对话 loop

- `LLMMCPConversationRunner` 继续作为底层对话执行器
- usability 层只新增场景 contract、指标计算、结果写出和比较逻辑
- 这样能最大程度降低与现有 smoke 的分叉风险

### 5. 输出两类结果：运行结果和比较结果

- 单次运行输出 scorecard JSON + Markdown summary
- 比较运行输出 regression report
- review 时优先看 regression budget，而不是原始日志

### 6. adoption 分阶段

- 第一步：本地和 manual / nightly
- 第二步：作为 advisory lane 进入 PR
- 第三步：只有在阈值稳定后，才考虑局部 gate

## Metric Checklist

1. `task_success_rate`
   目标：衡量 end-to-end 完成能力
2. `first_correct_action_rate`
   目标：衡量入口命名和边界是否清晰
3. `invalid_call_rate`
   目标：衡量 schema、描述和能力边界是否容易被误用
4. `average_round_trips`
   目标：衡量完成任务需要多少 MCP 试错
5. `query_answer_fidelity`
   目标：衡量查询结果是否被正确理解
6. `boundary_confusion_rate`
   目标：衡量 resource/tool 设计是否让模型困惑
7. `resource_hit_rate`
   目标：衡量 resource-required 场景中是否能命中正确上下文
8. `recovery_rate`
   目标：衡量一次错误后是否容易恢复

## Implementation Strategy

1. 在现有 `LLME2EScenario` 旁边引入 usability scenario 和 scorecard 模型。
2. 对现有 tool trace 和 artifact bundle 做最小扩展，
   让它们能表达 action validity、latency、boundary classification。
3. 固化最小 baseline scenario pack，
   覆盖 H2 / MySQL 的 resource、tool、query、failure-recovery 任务。
4. 实现指标计算器和 scorecard writer，
   产出 JSON + Markdown summary。
5. 实现 regression checker，
   支持 candidate vs baseline 的比较。
6. 最后把 quickstart、nightly 入口和 artifact 约定补齐。

## Validation Strategy

- **Scenario and score model tests**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilityMetricCalculatorTest,LLMUsabilityRegressionCheckerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Baseline usability suite**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Existing lower-layer smoke**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionLLMH2SmokeE2ETest,ProductionLLMMySQLSmokeE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl test/e2e/mcp -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

## Rollout Notes

- 第一阶段的目标不是证明模型“很聪明”，
  而是判断当前 MCP surface 是否容易被模型正确使用。
- 如果第一阶段发现最大问题集中在命名、schema 或 resource 粒度，
  应优先优化 surface clarity，再考虑扩大场景数。
- usability evaluation 是产品验收层，不是替代 deterministic contract tests。
