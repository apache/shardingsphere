# Data Model: ShardingSphere-Proxy Encrypt and Mask MCP V1

## 1. 建模原则

- 模型以逻辑视图为主，不以物理列为主。
- 模型必须区分“规划产物”和“执行结果”，避免一步一步模式下状态混淆。
- 模型必须把审批模式、命名冲突策略和校验结果显式化。
- 模型必须显式体现“上游结构化意图优先、原始自然语言仅作补充上下文”的职责边界。
- V1 只覆盖单数据库、单表、单列，但数据结构应允许未来扩展到多列。

## 2. 核心实体

### 2.1 WorkflowRequest

一次工作流请求的标准化输入。

- `database`: 必填，逻辑数据库名
- `schema`: 可选，逻辑 schema
- `table`: 必填，逻辑表名
- `column`: 必填，逻辑列名
- `featureType`: 必填，`encrypt` 或 `mask`
- `operationType`: 必填，`create` / `alter` / `drop`
- `rawUserRequest`: 可选，用户原始描述
- `structuredIntentEvidence`: 可选，上游模型已抽取的结构化证据
- `deliveryMode`: 必填，`all-at-once` 或 `step-by-step`
- `executionMode`: 必填，`auto-execute` / `review-then-execute` / `manual-only`
- `allowIndexDDL`: 必填，布尔值
- `allowPhysicalDDLAutoExecute`: 必填，布尔值
- `userOverrides`: 可选，用户对算法、命名、执行顺序的覆盖项

### 2.2 StructuredIntent

由上游模型或追问阶段补齐后的、可直接进入规划阶段的结构化意图。

- `featureType`
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
4. 命名、DDL / DistSQL 规划
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
- `structuredIntent`
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

### 3.1 FeatureType

- `encrypt`
- `mask`

### 3.2 OperationType

- `create`
- `alter`
- `drop`

V1 约束：

- `encrypt` 允许 `create` / `alter` / `drop`
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

如果在任一审批点暂停：

`... -> awaiting-review`
或
`... -> awaiting-manual-execution`

## 5. 建模约束

- 原始自然语言输入不能替代结构化规则字段成为唯一执行依据。
- encrypt alter / drop 不要求 MCP 生成 cleanup 计划，物理清理由用户自行处理。
- `manual-only` 模式必须区分 review-safe 预览与 executable artifacts。
- 逻辑视图验证与 SQL 可执行性验证必须以 Proxy 逻辑视图为准。

## 6. 数据边界说明

- V1 不新增审计落库模型。
- V1 不建模历史数据迁移、回填或 cleanup 对象。
- V1 不把样本数据读取建成完成标准。
