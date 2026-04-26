# Contracts: MCP Tools and Resources for Encrypt and Mask Workflow

## 1. 设计原则

- Tool 负责编排、审批、执行与验证。
- Resource 负责“当前状态只读查看”。
- 规则与算法的真实语义尽量复用现有 DistSQL 能力，不复制一套平行协议。
- 所有 Tool 都必须显式接收 `database`。
- 所有 Tool 都运行在连接 Proxy 的 MCP 拓扑上。
- Tool 必须优先消费上游结构化意图；原始自然语言仅作为补充上下文。

## 2. Resource Contracts

### 2.1 `shardingsphere://databases/{database}/encrypt-rules`

**用途**

- 返回指定逻辑库下的全部加密规则摘要。

**最小返回字段**

- `table`
- `logic_column`
- `cipher_column`
- `assisted_query_column`
- `like_query_column`
- `encryptor_type`
- `encryptor_props`
- `assisted_query_type`
- `assisted_query_props`
- `like_query_type`
- `like_query_props`

### 2.2 `shardingsphere://databases/{database}/encrypt-rules/{table}`

**用途**

- 返回指定逻辑表的加密规则详情。

### 2.3 `shardingsphere://databases/{database}/mask-rules`

**用途**

- 返回指定逻辑库下的全部脱敏规则摘要。

**最小返回字段**

- `table`
- `column`
- `algorithm_type`
- `algorithm_props`

### 2.4 `shardingsphere://databases/{database}/mask-rules/{table}`

**用途**

- 返回指定逻辑表的脱敏规则详情。

### 2.5 `shardingsphere://plugins/encrypt-algorithms`

**用途**

- 返回当前 Proxy 可见的加密算法插件列表。
- 这是 MCP 富化后的算法视图，不等同于原始 `SHOW ENCRYPT ALGORITHM PLUGINS` 结果集。

**最小返回字段**

- `type`
- `type_aliases`
- `description`
- `source`
- `supports_decrypt`
- `supports_equivalent_filter`
- `supports_like`

### 2.6 `shardingsphere://plugins/mask-algorithms`

**用途**

- 返回当前 Proxy 可见的脱敏算法插件列表。
- 这是 MCP 富化后的算法视图，不等同于原始 `SHOW MASK ALGORITHM PLUGINS` 结果集。

**最小返回字段**

- `type`
- `type_aliases`
- `description`
- `source`

## 3. Tool Contracts

## 3.1 `plan_encrypt_mask_rule`

**用途**

- 把上游结构化意图和必要追问结果转成可审阅、可分步执行的计划。

**输入**

```json
{
  "database": "order_db",
  "schema": "public",
  "table": "t_order",
  "column": "phone",
  "feature_type": "encrypt",
  "operation_type": "create",
  "raw_user_request": "给手机号加密并支持等值查询",
  "structured_intent_evidence": {
    "requires_decrypt": true,
    "requires_equality_filter": true
  },
  "delivery_mode": "all-at-once",
  "execution_mode": "review-then-execute",
  "allow_index_ddl": true,
  "user_overrides": {
    "cipher_column_name": null,
    "algorithm_type": null
  }
}
```

**输出**

```json
{
  "plan_id": "plan-001",
  "status": "planned",
  "pending_questions": [],
  "issues": [],
  "global_steps": [],
  "algorithm_recommendations": [],
  "property_requirements": [],
  "masked_property_preview": {},
  "derived_column_plan": {},
  "ddl_artifacts": [],
  "distsql_artifacts": [],
  "index_plan": [],
  "validation_strategy": {}
}
```

**行为约束**

- 缺失关键信息时必须返回 `pending_questions`，不能直接产出可执行工件。
- `issues` 中的 warning / error 必须携带稳定错误码。
- 必须先返回 `global_steps`。
- 算法确定后，如仍缺少必填属性，`pending_questions` 必须转入属性采集，而不是直接生成最终工件。
- review 默认只返回 `masked_property_preview`，不默认明文回显敏感参数。
- 必须把最终命名方案回传给用户。
- `encrypt` 与 `mask` 在 V1 都允许 `create` / `alter` / `drop`。
- encrypt alter / drop 不生成 cleanup DDL；如果存在遗留物理工件，由用户自行处理。

## 3.2 `apply_encrypt_mask_rule`

**用途**

- 在用户确认后执行计划中的 DDL / DistSQL。

**输入**

```json
{
  "plan_id": "plan-001",
  "database": "order_db",
  "execution_mode": "review-then-execute",
  "approved_steps": [
    "ddl",
    "index_ddl",
    "rule_distsql"
  ]
}
```

**输出**

```json
{
  "status": "executing",
  "issues": [],
  "step_results": [],
  "executed_ddl": [],
  "executed_distsql": [],
  "skipped_artifacts": [],
  "manual_artifact_package": {}
}
```

**行为约束**

- `manual-only` 模式下不得自动执行任何工件。
- `manual-only` 模式下必须允许返回可执行工件包，同时默认 review 视图继续对敏感参数打码。
- 任何执行失败都必须通过 `issues` 返回阶段、建议动作与是否可重试。
- 每一步必须返回可读进度。
- 当某一步失败时必须给出已完成、未完成和建议后续动作。
- encrypt drop 与收缩式 alter 不生成 cleanup DDL。

## 3.3 `validate_encrypt_mask_rule`

**用途**

- 对已执行或手工执行后的结果进行四层验证。

**输入**

```json
{
  "plan_id": "plan-001",
  "database": "order_db",
  "table": "t_order",
  "column": "phone",
  "feature_type": "encrypt",
  "operation_type": "drop",
  "expected_artifacts": {
    "ddl": [],
    "distsql": []
  }
}
```

**输出**

```json
{
  "status": "completed",
  "issues": [],
  "ddl_validation": {},
  "rule_validation": {},
  "logical_metadata_validation": {},
  "sql_executability_validation": {},
  "overall_status": "passed",
  "mismatches": []
}
```

**行为约束**

- 不要求做数据正确性验证。
- 不要求做历史数据迁移校验。
- 必须明确区分 `passed`、`failed`、`skipped`。
- mismatch 必须能映射到稳定错误码。
- 逻辑元数据校验必须基于 Proxy 逻辑视图。
- encrypt drop 必须继续验证规则删除、逻辑元数据和 SQL 可执行性。

## 4. 推荐的 Tool 与 Resource 分工

- 读当前状态：
  - 优先走 Resource
- 生成计划：
  - 走 `plan_encrypt_mask_rule`
- 执行计划：
  - 走 `apply_encrypt_mask_rule`
- 执行后校验：
  - 走 `validate_encrypt_mask_rule`
- 低层 SQL / DistSQL 执行：
  - 仍可复用 `execute_query`，但不直接暴露给规则规划层

## 5. 非目标

- 不定义历史数据迁移 Tool。
- 不定义回滚 Tool。
- 不定义审计落库接口。
- 不把样本数据读取做成默认必经步骤。
- 不把完全自由文本的强语义理解放在 MCP 内完成。
- 不定义 cleanup DDL 规划或执行接口。
