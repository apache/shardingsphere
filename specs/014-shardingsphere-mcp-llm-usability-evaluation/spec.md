# Feature Specification: ShardingSphere MCP LLM Usability and Comfort Evaluation

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-06  
**Status**: Design draft  
**Input**: User description:
"大模型是如何使用这个MCP能用的非常舒服的？怎么去断定有什么指标吗？",
"愿意，用speckit细化你的设计，但不允许切换分支"

## Scope Statement

本 Speckit 只设计一件事：
把 “ShardingSphere MCP 对大模型来说到底好不好用、顺不顺、稳不稳”
从主观印象收敛成可重复、可量化、可比较的评估体系。

本轮范围限定为当前 MCP 能力面的可用性评估，
不修改现有 public MCP surface：

- 基于现有 `resource`、`tool`、`execute_query` 能力做评估设计
- 沿用现有 `test/e2e/mcp` 中已经存在的真实模型、真实 MCP、真实 JDBC runtime 路径
- 把评估拆成 `resource`、`tool`、`query` 三个维度
- 定义最小可执行场景包、评分模型、失败分类、回归阈值和 artifact 形状
- 第一阶段只覆盖只读任务与当前已有的 H2 / MySQL runtime
- 评估目标是回答：
  - 模型是否容易选对入口
  - 模型是否会在 `resource` 与 `tool` 之间反复试错
  - 模型是否能在少量往返下拿到正确结果
  - 模型在失败后是否容易恢复

本轮不做：

- 不修改现有 resource / tool 契约
- 不把 `resource` 和 `tool` 合并成新抽象
- 不做模型排行榜、跨 provider 横评或成本优化专项
- 不把写操作、DDL、DCL、多轮复杂规划纳入第一阶段评估
- 不切换分支

## Problem Statement

当前仓库已经具备两类重要验证：

- deterministic MCP contract / runtime E2E
- 最小真实模型驱动的 MCP smoke

它们能证明：

- server 是否能正常工作
- 模型是否能走通一条真实的 discovery + query 路径

但它们还不能回答更关键的产品问题：

1. 模型是不是很容易选对 `resource` 或 `tool`
2. 当前 `resource` / `tool` 边界是否足够清晰
3. 一个看似“能跑通”的变更，是否让模型试错次数明显变多
4. 当前 MCP 对模型来说到底是 “能用”，还是 “用起来舒服”
5. 后续 PR 或重构是否引入了可用性回退

换句话说，
当前系统缺少的是一套 usability contract，
而不是再多一条功能性 smoke。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 维护者可以量化模型使用 MCP 的顺滑度 (Priority: P1)

作为 MCP 维护者，我希望用一组固定场景得到一份结构化 scorecard，
这样我可以明确知道模型是在低试错、低往返下完成任务，
还是只是勉强撞对结果。

**Why this priority**: 如果不能量化 “舒服”，
后续所有关于 resource / tool 设计的讨论都只能停留在主观感受。

**Independent Test**: 运行最小 usability scenario pack，
输出 machine-readable scorecard 和 human-readable summary，
其中必须包含任务成功率、首次正确动作率、无效调用率和平均 MCP 往返数。

**Acceptance Scenarios**:

1. **Given** 一个只需要结构上下文的任务，
   **When** 模型完成任务，
   **Then** 结果中必须能判断模型是否通过正确 resource 建立上下文，
   而不是绕到无关 tool 或直接猜测。
2. **Given** 一个需要 discovery 后再执行只读 SQL 的任务，
   **When** 模型完成任务，
   **Then** 结果中必须能判断模型是否以合理的动作序列完成任务，
   并统计总往返数与无效调用数。
3. **Given** 一个 scenario pack 执行结束，
   **When** 产出评估结果，
   **Then** 系统必须输出总体分数、维度分数、逐场景结果和失败分类。

---

### User Story 2 - 维护者可以识别 resource / tool 边界是否让模型困惑 (Priority: P1)

作为 MCP 维护者，我希望评估结果能明确指出
“模型是不会用” 还是 “接口边界让它很难用”，
这样我才能知道应该优化命名、schema、resource 结构还是场景引导。

**Why this priority**: 当前最核心的问题不是功能缺失，
而是 `resource`、`tool`、`query` 三种入口对模型来说是否边界清楚。

**Independent Test**: 运行专门覆盖 `resource-first`、`tool-first`、
`mixed discovery + query` 的场景集合，
并对 “错误入口选择” 与 “不必要往返” 做专门分类。

**Acceptance Scenarios**:

1. **Given** 一个只需了解 capability 或 schema 结构的任务，
   **When** 模型优先去调 `execute_query` 或无关 metadata tool，
   **Then** 结果必须标记为 boundary confusion，而不是只记成普通失败。
2. **Given** 一个必须最终执行 SQL 的任务，
   **When** 模型只反复读取 resource 而没有进入正确 tool path，
   **Then** 结果必须标记为 wrong interaction strategy。
3. **Given** 一个任务最终成功，
   **When** 成功之前存在多次错误入口选择或重复调用，
   **Then** scorecard 必须反映成功但不顺滑，而不是把它算成 “完美通过”。

---

### User Story 3 - Reviewer 可以比较一次变更是否让模型更难使用 (Priority: P2)

作为 reviewer，我希望把新 run 与基线 run 做结构化比较，
这样我可以判断一次契约、命名或 payload 变化
是否引入了 usability regression。

**Why this priority**: 没有比较基线时，
“平均 3 次往返” 到底算好还是退步很难判断。

**Independent Test**: 给定两份评估结果，
comparison 逻辑能输出回归项、保持项和提升项，
并根据预算阈值决定是否标记失败。

**Acceptance Scenarios**:

1. **Given** candidate run 的任务成功率下降超过预算，
   **When** 做结果比较，
   **Then** 必须明确标记为 regression。
2. **Given** candidate run 功能上仍然成功，
   **When** 首次正确动作率和无效调用率显著变差，
   **Then** 比较结果必须仍然提示 usability regression。
3. **Given** candidate run 提升了 `resource` 使用命中率，
   **When** 比较结果输出，
   **Then** 必须能把这一提升单独展示出来，而不是只给一个总分。

### Edge Cases

- 模型不调用任何 MCP 能力，直接猜答案，且答案碰巧正确
- 模型先读了大量无关 resource，最终虽然成功但 token / 往返成本异常高
- 模型在 `list_tables`、`describe_table`、`search_metadata` 之间反复试错
- 模型参数 JSON 不合法，但第二次能修正
- 模型执行了只读之外的 SQL，需要被安全拒绝
- H2 与 MySQL 在 schema 语义上不完全一致，导致 discovery 路径不同
- resource 返回成功，但模型对内容利用率极低
- 最终答案正确，但 tool trace 不满足场景要求
- 一次 run 中某个 scenario 因网络或模型冷启动失败，需要与纯契约失败区分

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 定义一套面向 ShardingSphere MCP 的 LLM usability evaluation framework，
  用于量化模型是否能顺滑地使用 `resource`、`tool` 和 `execute_query`。
- **FR-002**: evaluation framework MUST 把任务分为至少三类维度：
  `resource`、`tool` 和 `query`，并提供总体分数与分维度分数。
- **FR-003**: evaluation framework MUST 定义最小 baseline scenario pack，
  其中 MUST 覆盖 H2 与 MySQL 两类 runtime。
- **FR-004**: baseline scenario pack MUST 至少覆盖：
  resource-only context task、tool-only metadata task、
  discovery-then-query mixed task、failure-recovery task。
- **FR-005**: 每个 scenario MUST 声明：
  场景目的、允许能力、必需能力、禁止能力、期望最终答案和失败分类规则。
- **FR-006**: 系统 MUST 记录完整交互轨迹，
  包括 turn 序号、动作类型、resource URI 或 tool name、
  参数摘要、执行结果摘要、是否无效、是否恢复成功和耗时信息。
- **FR-007**: scorecard MUST 至少计算以下指标：
  任务成功率、首次正确动作率、无效调用率、平均 MCP 往返数、
  query answer fidelity、boundary confusion rate。
- **FR-008**: 对 resource-required 的场景，
  系统 MUST 额外计算 resource utility / resource hit 相关指标。
- **FR-009**: 对包含失败后修正路径的场景，
  系统 MUST 计算 recovery rate，而不是只输出最终 pass / fail。
- **FR-010**: 系统 MUST 对失败进行明确分类，
  至少包括：
  `boundary_confusion`、`invalid_arguments`、`unexpected_tool_or_resource`、
  `unsafe_query_attempt`、`final_answer_mismatch`、`max_turns_exceeded`、
  `environment_failure`。
- **FR-011**: evaluation 输出 MUST 同时包含 machine-readable JSON scorecard
  和 human-readable Markdown summary。
- **FR-012**: comparison 模式 MUST 支持 candidate run 与 baseline run 的指标对比，
  并根据 regression budget 做通过 / 失败判定。
- **FR-013**: regression budget MUST 支持至少对以下指标设阈值：
  任务成功率、首次正确动作率、无效调用率、平均 MCP 往返数、
  query answer fidelity、boundary confusion rate。
- **FR-014**: 第一阶段 evaluation SHOULD 复用现有
  `LLME2EScenario`、`LLMMCPConversationRunner`、
  `MCPToolTraceRecord`、`LLME2EArtifactBundle` 等测试资产，
  而不是另起一套完全独立的框架。
- **FR-015**: evaluation framework MUST 不修改现有 public MCP capability surface、
  resource URI、tool name 或 `execute_query` contract。
- **FR-016**: 第一阶段 MUST 只覆盖只读任务；
  写操作、DDL、DCL 和多语句场景 MUST 仅作为安全拒绝场景出现。
- **FR-017**: baseline 运行模式 SHOULD 先面向本地复现与 nightly / manual lane，
  不在第一轮变成强制 PR gate。
- **FR-018**: evaluation artifact MUST 写入隔离目录，
  以避免并发 run 相互覆盖。
- **FR-019**: 本设计落地时 MUST 提供至少一个最小场景包和一个扩展场景包；
  最小场景包用于稳定回归，扩展场景包用于边界分析。
- **FR-020**: 本轮 Speckit 设计与后续实现 MUST 在当前分支工作树中完成，
  不允许切换分支。

### Key Entities *(include if feature involves data)*

- **LLMUsabilityEvaluationSuite**: 一组可重复执行的 scenario、阈值和输出规则。
- **LLMUsabilityScenario**: 单个评估任务，定义场景意图、允许动作、必需动作和成功标准。
- **LLMUsabilityTrace**: 单个 scenario 执行期间的完整 MCP 动作轨迹。
- **LLMUsabilityActionRecord**: 一次 `resources/read`、`resources/list`、`tools/call`
  或其他 MCP 关键动作的记录。
- **LLMUsabilityScenarioResult**: 单个 scenario 的最终判定、失败分类和维度统计。
- **LLMUsabilityDimensionScore**: `resource`、`tool`、`query` 其中一类维度的聚合结果。
- **LLMUsabilityScorecard**: 整个 suite 的总分、分维度分数、指标、回归结论和摘要。
- **LLMUsabilityRegressionBudget**: 定义哪些指标允许下降、下降多少以及何时判定为 regression。

### Assumptions

- 当前 `test/e2e/mcp` 已有真实模型、真实 runtime 和 artifact 记录基础能力，
  因而 usability evaluation 可以建立在现有资产之上。
- 第一阶段优先回答 “当前设计顺不顺”，
  不追求一次性覆盖全部数据库或全部复杂对话模式。
- 只读 H2 / MySQL 场景已经足够暴露大部分 metadata discovery、
  capability 判断和 query 路径的可用性问题。
- `resource` 与 `tool` 在产品语义上继续保持分离；
  evaluation 的职责是测边界是否清楚，不是改边界。

## Non-Goals

- 不引入新的 MCP public resource / tool
- 不重写现有 LLM runner
- 不做跨模型成本、速度、胜率排行榜
- 不在本轮把 MongoDB / TiDB / ClickHouse 等外部实现纳入自动化基线
- 不把 usability evaluation 直接升级成主 CI 的阻塞 gate
- 不借此做无关命名调整、测试清理或 transport 重构

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 仓库中存在一套独立的 usability evaluation spec、plan、research、
  data model、acceptance contract 和 tasks 文档，可指导后续实现。
- **SC-002**: 最小 scenario pack 的 contract 明确规定了至少 12 个场景，
  且覆盖 `resource`、`tool`、`query` 与 failure-recovery 四类任务。
- **SC-003**: scorecard contract 明确包含并定义了：
  `task_success_rate`、`first_correct_action_rate`、
  `invalid_call_rate`、`average_round_trips`、
  `query_answer_fidelity` 和 `boundary_confusion_rate`。
- **SC-004**: regression budget contract 明确规定了至少 6 个关键指标的通过阈值。
- **SC-005**: 本设计明确要求输出 machine-readable JSON 和 Markdown summary，
  并包含逐场景结果、维度结果和比较结果。
- **SC-006**: 本设计不引入任何 MCP public surface 变更，
  后续实现可在 `test/e2e/mcp` 范围内增量落地。
