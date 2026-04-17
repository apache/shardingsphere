# Data Model: ShardingSphere-Proxy Encrypt and Mask MCP V1

## 1. 建模原则

- 模型以逻辑视图为主，不以物理列为主。
- 模型必须区分“规划产物”和“执行结果”，避免一步一步模式下状态混淆。
- 模型必须把审批模式、命名冲突策略、校验结果显式化。
- V1 只覆盖单数据库、单表、单列，但数据结构应允许未来扩展到多列。

## 2. 核心实体

### 2.1 WorkflowRequest

一次自然语言工作流请求的标准化输入。

- `database`: 必填，逻辑数据库名
- `schema`: 可选，逻辑 schema
- `table`: 必填，逻辑表名
- `column`: 必填，逻辑列名
- `intentType`: 必填，`encrypt` 或 `mask`
- `operationType`: 必填，`create` / `alter` / `drop`
- `naturalLanguageIntent`: 必填，用户原始描述
- `deliveryMode`: 必填，`all-at-once` 或 `step-by-step`
- `executionMode`: 必填，`auto-execute` / `review-then-execute` / `manual-only`
- `allowSampleData`: 必填，布尔值
- `allowIndexDDL`: 必填，布尔值
- `allowPhysicalDDLAutoExecute`: 必填，布尔值
- `userOverrides`: 可选，用户对算法、命名、执行顺序的覆盖项

### 2.2 ClarifiedIntent

由追问阶段补齐后的、可直接进入规划阶段的结构化意图。

- `intentType`
- `operationType`
- `requiresDecrypt`: 加密场景是否要求可逆
- `requiresEqualityFilter`: 是否要求等值查询能力
- `requiresLikeQuery`: 是否要求模糊查询能力
- `requiresMaskPreview`: 脱敏场景是否需要特定展示效果
- `fieldSemantics`: 字段语义，例如手机号、身份证、姓名、地址
- `reasoningNotes`: 推荐依据说明
- `unresolvedQuestions`: 未解决问题列表

### 2.3 InteractionPlan

整个交互过程的步骤总表，是用户第一眼必须看到的对象。

- `planId`
- `summary`
- `steps`: 有序步骤集合
- `currentStep`
- `approvalCheckpoints`
- `deliveryMode`
- `executionMode`
- `validationStrategy`

`steps` 的最小标准阶段：

1. 上下文确认
2. 规则与算法盘点
3. 缺失信息追问
4. 命名与 DDL / DistSQL 规划
5. 审阅与确认
6. 执行
7. 验证
8. 总结输出

### 2.4 AlgorithmCandidate

候选算法的统一表示。

- `algorithmType`
- `algorithmCategory`: `encrypt` 或 `mask`
- `source`: `builtin` 或 `custom-spi`
- `supportsDecrypt`
- `supportsEquivalentFilter`
- `supportsLike`
- `propsTemplate`: 需要补齐的属性模板
- `recommendationScore`
- `recommendationReason`
- `riskNotes`

### 2.5 AlgorithmPropertyRequirement

算法参数模板的统一表示。

- `propertyKey`
- `required`
- `secret`
- `defaultValue`
- `description`
- `validationHint`

### 2.6 DerivedColumnPlan

仅在加密场景出现，表示逻辑列到物理派生列的规划结果。

- `logicalColumn`
- `cipherColumnName`
- `assistedQueryColumnName`
- `likeQueryColumnName`
- `cipherColumnRequired`
- `assistedQueryColumnRequired`
- `likeQueryColumnRequired`
- `dataTypeStrategy`: 固定为 `shardingsphere-default`
- `resolvedNames`: 最终命名结果
- `nameCollisions`: 冲突详情
- `namingSource`: `default` / `user-override` / `auto-renamed`

### 2.7 IndexPlan

面向查询型派生列的索引规划。

- `table`
- `column`
- `indexName`
- `indexType`: V1 先保留为普通索引建议
- `reason`
- `ddl`
- `selected`: 用户是否同意纳入执行

### 2.8 RulePlan

一条将要创建、修改或删除的规则定义。

- `ruleType`: `encrypt` 或 `mask`
- `operationType`: `create` / `alter` / `drop`
- `database`
- `table`
- `logicalColumn`
- `algorithmBindings`
- `distSQL`
- `dependsOnDDL`: 是否依赖物理 DDL 先完成

### 2.9 DDLArtifact

工作流生成的物理 SQL 工件。

- `artifactId`
- `artifactType`: `add-column` / `drop-column` / `create-index` / `drop-index`
- `sql`
- `reviewRequired`
- `executed`
- `executionOrder`
- `targetObjects`

### 2.10 WorkflowContextSnapshot

一步一步模式下保存在服务端的上下文快照。

- `planId`
- `workflowStatus`
- `confirmedRequest`
- `clarifiedIntent`
- `selectedAlgorithm`
- `confirmedProperties`
- `maskedSecretPropertySummary`
- `derivedColumnPlan`
- `pendingQuestions`
- `currentStep`

### 2.11 ValidationReport

工作流完成后的四层验证结果。

- `ddlValidation`
- `ruleValidation`
- `logicalMetadataValidation`
- `sqlExecutabilityValidation`
- `overallStatus`
- `mismatches`
- `suggestedNextActions`

其中每个子验证至少包含：

- `status`: `passed` / `failed` / `skipped`
- `evidence`
- `details`

### 2.12 WorkflowIssue

工作流中的统一问题对象。

- `code`
- `severity`
- `stage`
- `message`
- `userAction`
- `retryable`
- `details`
- `relatedArtifacts`

### 2.13 ExecutionSummary

一次工作流最终返回给用户的汇总对象。

- `requestSummary`
- `finalAlgorithms`
- `finalNames`
- `selectedExecutionMode`
- `generatedDDLArtifacts`
- `generatedRuleArtifacts`
- `validationReport`
- `issues`
- `manualFollowUps`

## 3. 关键枚举

### 3.1 IntentType

- `encrypt`
- `mask`

### 3.2 OperationType

- `create`
- `alter`
- `drop`

V1 约束：

- `encrypt` 仅允许 `create` / `alter`
- `mask` 允许 `create` / `alter` / `drop`

### 3.3 DeliveryMode

- `all-at-once`
- `step-by-step`

### 3.4 ExecutionMode

- `auto-execute`
- `review-then-execute`
- `manual-only`

### 3.5 WorkflowStatus

- `clarifying`
- `planned`
- `awaiting-review`
- `awaiting-execution-approval`
- `executing`
- `awaiting-manual-execution`
- `validating`
- `completed`
- `failed`

## 4. 状态流转

### 4.1 一起做模式

`clarifying -> planned -> awaiting-review -> executing -> validating -> completed`

如果任何一步被拒绝或校验失败：

`... -> failed`

### 4.2 一步一步模式

`clarifying -> planned -> awaiting-review -> awaiting-execution-approval -> executing -> validating -> completed`

每一段之间都允许暂停，等待用户下一次确认。

### 4.3 仅手工执行模式

`clarifying -> planned -> awaiting-review -> awaiting-manual-execution -> validating(可延后触发) -> completed`

## 5. 实体关系

- `WorkflowRequest` 经过澄清后生成一个 `ClarifiedIntent`
- `ClarifiedIntent` 生成一个 `InteractionPlan`
- `InteractionPlan` 可产生零到多个 `AlgorithmCandidate`
- `encrypt` 场景会生成一个 `DerivedColumnPlan`
- `DerivedColumnPlan` 可产生零到多个 `DDLArtifact`
- `RulePlan` 与 `DDLArtifact` 共同组成可执行工件
- 执行后生成一个 `ValidationReport`
- `ValidationReport` 与所有执行工件共同汇总成 `ExecutionSummary`

## 6. 业务约束

### 6.1 通用约束

- `database` 不能为空。
- `table` 和 `column` 在规划前必须能从逻辑元数据中解析出来。
- `manual-only` 模式下，任何 DDL / DistSQL 的 `executed` 都必须为 `false`。
- `step-by-step` 模式下，必须保留 `currentStep` 与待确认阶段。

### 6.2 加密约束

- `requiresDecrypt=true` 时，候选算法必须支持解密。
- `requiresEqualityFilter=true` 时，必须提供辅助查询能力或能证明主算法支持等值查询。
- `requiresLikeQuery=true` 时，必须提供模糊查询能力或能证明主算法支持模糊查询。
- `DerivedColumnPlan` 的物理列命名默认使用：
  - `*_cipher`
  - `*_assisted_query`
  - `*_like_query`
- 命名冲突时必须追加数字后缀。
- 不默认复用现存同名列。

### 6.3 脱敏约束

- 脱敏规则默认不依赖物理列 DDL。
- 算法推荐必须结合字段语义与展示诉求。
- 修改或删除脱敏规则时，仍需做规则与逻辑 SQL 可执行性验证。

## 7. 非持久化说明

V1 不要求审计落库，也不要求持久化保存工作流状态。
因此上述对象默认是一次会话内的运行态模型，而不是新引入的持久化表结构。
对一步一步模式，建议以 `sessionId + planId` 维持服务端上下文快照。
