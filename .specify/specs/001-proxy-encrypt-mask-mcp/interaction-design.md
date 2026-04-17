# Detailed Design: Interaction Flow and State Machine

## 1. 目标

这份文档把 PRD 中“先列步骤、再追问、再执行、再校验”的要求，细化为可实现的交互状态机。

它回答四个问题：

- MCP 与用户的每一轮交互如何推进；
- `all-at-once` 与 `step-by-step` 两种交付模式如何分叉；
- `auto-execute`、`review-then-execute`、`manual-only` 三种执行模式如何落地；
- 加密、脱敏、删除三类流程分别在哪些节点暂停、确认、继续。

## 2. 设计原则

- 用户第一眼必须看到全局步骤清单，而不是直接看到 SQL。
- 交互主视角始终是逻辑库、逻辑表、逻辑列。
- MCP 只追问缺失的关键信息，不重复索要已经确认过的上下文。
- 所有执行前都必须有可审阅的工件。
- 所有执行后都必须有结构化验证结果。
- `step-by-step` 模式下，上下文由服务端会话保存，用户不需要重复描述。

## 3. 用户可见全局步骤

任意一次工作流，都必须先返回以下步骤清单，再进入细节：

1. 确认目标对象与操作类型
2. 盘点当前规则、插件与逻辑元数据
3. 追问缺失条件并确定算法
4. 采集算法参数并生成命名方案
5. 生成 DDL / DistSQL / 索引计划
6. 审阅并确认执行方式
7. 执行或输出手工工件
8. 验证并汇总结果

如果是 `mask drop`，则步骤 4 和步骤 5 可以退化为“规则变更规划”，不强制产生物理 DDL。

## 4. 统一状态机

### 4.1 内部主状态

- `intaking`
- `discovering`
- `clarifying`
- `selecting-algorithm`
- `collecting-properties`
- `planning-artifacts`
- `awaiting-review`
- `awaiting-execution`
- `awaiting-manual-execution`
- `executing`
- `validating`
- `completed`
- `failed`

### 4.2 状态流转

标准成功路径：

`intaking -> discovering -> clarifying -> selecting-algorithm -> collecting-properties -> planning-artifacts -> awaiting-review -> executing/awaiting-manual-execution -> validating -> completed`

失败路径：

`... -> failed`

暂停路径：

- `step-by-step` 模式允许在 `clarifying`、`collecting-properties`、`awaiting-review`、`awaiting-execution`、`awaiting-manual-execution` 之后暂停。
- `all-at-once` 模式只在“需要用户补信息”或“需要用户审批”时暂停。

## 5. 交付模式设计

### 5.1 `all-at-once`

适合用户希望一轮自然语言尽量走完的场景。

- MCP 会尽量在一轮里补齐追问。
- 只有在缺少关键信息或需要审批时才停下。
- 如果用户已经明确说“一起做”，MCP 默认在同一上下文中连续推进到 review 或执行阶段。

### 5.2 `step-by-step`

适合用户想分阶段确认的场景。

- 每个大步骤结束后暂停。
- MCP 返回“当前完成到哪一步、下一步要确认什么”。
- 已确认内容写入服务端上下文。
- 用户下一轮只需说“继续”“执行下一步”“改成某算法”等增量指令。

## 6. 执行模式设计

### 6.1 `auto-execute`

- 审阅通过后，MCP 自动顺序执行已批准工件。
- 默认执行顺序：
  1. 物理列 DDL
  2. 索引 DDL
  3. 规则 DistSQL
  4. 执行后验证

### 6.2 `review-then-execute`

- MCP 先展示完整工件，再等待显式批准。
- 批准后执行顺序与 `auto-execute` 相同。
- 这是默认推荐模式，因为可兼顾可控性与易用性。

### 6.3 `manual-only`

- MCP 只生成并展示工件，不自动执行。
- MCP 仍然返回推荐执行顺序、验证 SQL 与验证步骤。
- 用户声明手工执行完成后，MCP 再进入验证阶段。

## 7. 典型交互流程

### 7.1 Encrypt Create / Alter

1. 用户描述目标列和加密意图。
2. MCP 返回全局步骤清单。
3. MCP 读取当前 Proxy 逻辑元数据、现有 encrypt 规则、可见插件。
4. 如自然语言未说清查询需求，MCP 追问：
   - 是否需要解密展示
   - 是否需要等值查询
   - 是否需要模糊查询
5. MCP 推荐算法或接受用户指定算法。
6. 算法确定后，MCP 进入参数采集阶段。
7. MCP 生成派生列命名方案、物理 DDL、索引计划和 DistSQL。
8. MCP 返回 review 结果：
   - 逻辑列
   - 算法与理由
   - 最终派生列名
   - SQL / DistSQL 工件
   - 执行方式
9. 用户批准后，MCP 执行或停止在手工阶段。
10. MCP 做四层验证并给出总结。

### 7.2 Mask Create / Alter

1. 用户描述字段脱敏目标。
2. MCP 返回全局步骤清单。
3. MCP 读取现有 mask 规则与插件。
4. 如需要，MCP 追问字段语义和展示要求。
5. MCP 推荐或接受用户指定 mask 算法。
6. 算法确定后采集必要参数。
7. MCP 生成 rule-first 计划，通常不含物理 DDL。
8. 审阅、执行、验证。

### 7.3 Mask Drop

1. 用户提出删除脱敏规则。
2. MCP 返回全局步骤清单。
3. MCP 读取当前 mask 规则并确认目标规则存在。
4. MCP 生成 rule-only 的 drop DistSQL。
5. 审阅后执行。
6. 验证规则已删除、逻辑元数据状态正常、逻辑 SQL 可执行。

## 8. 追问规则

### 8.1 必追问项

以下信息缺失时，不允许直接生成可执行工件：

- `database`
- `table`
- `column`
- `intentType`
- `operationType`
- 加密场景下的查询能力要求
- 已选算法需要的必填参数
- 用户选择的执行模式

### 8.2 尽量不追问项

以下内容优先通过现有元数据与默认策略推断，而不是增加用户负担：

- 默认派生列名
- 默认派生列类型
- 索引命名基础
- 是否存在命名冲突
- 当前规则是否已存在

## 9. 服务端上下文

`step-by-step` 模式下，MCP 至少要保存以下上下文：

- `planId`
- 当前数据库、表、列
- 已识别的 `intentType` 与 `operationType`
- 已确认的算法
- 已确认的非敏感参数
- 已确认的敏感参数引用
- 最终派生列命名结果
- 当前所处步骤
- 上次返回给用户的待确认项
- 选择的交付模式与执行模式

上下文只做运行态保存，不做审计落库。

## 10. 审阅输出规范

在真正执行前，review 输出至少包含：

- 当前目标对象
- 当前操作类型
- 推荐或选定算法与理由
- 最终派生列名称
- 将执行或将生成的 DDL / DistSQL
- 索引建议或索引 DDL
- 风险提醒
- 验证计划

敏感参数只显示打码后的摘要，不在默认 review 中明文回显。

## 11. 失败与中断处理

### 11.1 规划阶段失败

典型原因：

- 无法从 Proxy 逻辑视图识别目标对象
- 算法能力与用户需求冲突
- 自定义 SPI 插件可见但能力信息不足且用户拒绝确认
- 参数缺失导致无法生成可执行工件

处理方式：

- 明确告诉用户失败发生在哪一步
- 返回已确认上下文
- 返回建议下一步

### 11.2 执行阶段失败

典型原因：

- 物理 DDL 权限不足
- DistSQL 执行失败
- 索引 DDL 失败

处理方式：

- 返回已执行与未执行工件清单
- 停止后续自动步骤
- 允许用户转为 `manual-only` 或修正后重试

### 11.3 验证阶段失败

典型原因：

- 规则状态与计划不一致
- 逻辑元数据未刷新到预期状态
- 逻辑 SQL 无法执行

处理方式：

- 验证报告必须区分 `failed` 与 `skipped`
- 返回 mismatch 详情和下一步建议

## 12. 对实现的约束

- 不允许跳过全局步骤清单直接执行。
- 不允许在算法未确定前收集全量密钥参数。
- 不允许在 `manual-only` 模式下自动执行任何工件。
- 不允许把物理视图当成默认用户主视角。
- 不允许把一次失败简单汇总成“执行失败”而不给步骤级状态。
