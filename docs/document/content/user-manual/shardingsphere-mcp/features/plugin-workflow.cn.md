+++
title = "插件工作流"
weight = 3
+++

插件工作流是 ShardingSphere-MCP 处理多步骤治理变更的共享机制，不是一个独立的业务功能。
功能插件负责理解具体业务、生成 `plan_id` 和变更产物；MCP Server 负责保存当前会话中的计划，并提供统一的预览、执行、导出和校验阶段接口。

用户不会为了读取元数据、搜索对象或执行只读 SQL 单独使用插件工作流。
只有当插件规划工具返回 `plan_id` 时，模型或客户端才需要继续按照本页完成审查、应用和校验。
本页说明插件工作流，是因为这些阶段工具会出现在 MCP 工具列表中，并且不同插件共享同一套状态、执行模式和敏感输入处理方式。
具体可规划的业务能力仍以各功能插件页面为准。

## 基本阶段

一个典型工作流包含：

1. 调用功能插件规划工具，生成计划并返回 `plan_id`。
2. 如果返回 `status = clarifying`，按 `clarification_questions` 补齐缺失输入。
3. 如果返回 `status = planned`，审查生成的变更产物。
4. 模型或客户端调用 `database_gateway_apply_workflow`，并先使用 `execution_mode=preview`。
5. 用户审查预览结果后，模型或客户端使用 `execution_mode=review-then-execute` 执行，或使用 `manual-only` 导出人工执行包。
6. 模型或客户端调用 `database_gateway_validate_workflow` 校验最终状态。

## 会话与 plan_id

- `plan_id` 是当前工作流的句柄。
- `plan_id` 只在当前 MCP 会话内有效。
- `plan`、`apply`、`validate` 必须使用同一个 `MCP-Session-Id`。
- 第一次规划调用不需要传 `plan_id`。
- 后续补问、执行和校验必须继续传同一个 `plan_id`。

## 常见状态

| 状态 | 说明 | 下一步 |
| --- | --- | --- |
| `clarifying` | 信息不足，需要补齐参数。 | 使用同一个 `plan_id` 再次调用原规划工具。 |
| `planned` | 计划已生成，可以审查变更产物。 | 预览或导出人工执行包。 |
| `completed` | 已执行完成。 | 调用校验工具确认最终状态。 |
| `awaiting-manual-execution` | 选择了 `manual-only`。 | 人工执行返回的变更产物后再校验。 |
| `validated` | 校验已通过。 | 向用户返回结果。 |
| `failed` | 当前阶段失败。 | 查看 `issues`、`mismatches` 和 `recovery_guidance`。 |

## 执行模式

`database_gateway_apply_workflow` 必须显式传入 `execution_mode`：

| 执行模式 | 是否修改运行时状态 | 用途 |
| --- | --- | --- |
| `preview` | 否 | 只预览变更产物和副作用范围。 |
| `review-then-execute` | 是 | 审查后由 MCP Server 执行变更产物。 |
| `manual-only` | 否 | 不自动执行，返回人工执行包。 |

如果使用 `approved_steps` 分步执行，只能使用预览结果返回的 `preview_artifacts[].approval_step` 值。
未知步骤会被拒绝。

## 敏感输入

插件可能要求补充敏感字段，例如密钥或凭证。
带有 `secret: true`、`input_type: "secret"`，或字段名包含 password、token、key、secret、credential 的补问，不应通过普通表单明文回传。

推荐处理方式：

- 保留 `plan_id`。
- 客户端或运维侧通过密钥管理系统、受保护环境变量或运维控制通道取得值。
- 用同一个 `plan_id` 再次调用原规划工具，并只在受保护的 MCP 调用中传入敏感字段。

ShardingSphere-MCP 不直接读取密钥管理系统。
如果使用人工执行包，可以在返回的 DistSQL 中保留占位符，并由执行人员在受控环境替换。

示例：

```json
{
  "name": "database_gateway_plan_encrypt_rule",
  "arguments": {
    "plan_id": "${PLAN_ID}",
    "primary_algorithm_properties": {
      "aes-key-value": "${VALUE_FROM_SECRET_MANAGER}"
    }
  }
}
```

## 阶段工具

`database_gateway_apply_workflow` 和 `database_gateway_validate_workflow` 会出现在 MCP 工具列表中，因此需要在文档中说明。
它们不是独立业务功能，而是插件规划返回 `plan_id` 后使用的阶段接口。
用户通常不需要直接选择它们；模型或客户端会在审查、执行和校验阶段调用。

| 工具 | 实际作用 | 使用时机 |
| --- | --- | --- |
| `database_gateway_apply_workflow` | 对已有 `plan_id` 做预览、审查后执行，或导出人工执行包；它不创建加密、脱敏等业务计划。 | 插件规划工具返回 `status = planned` 后。 |
| `database_gateway_validate_workflow` | 根据可见元数据、规则状态和生成产物校验计划或执行结果；它不执行业务变更。 | 自动执行或人工执行完成后。 |

它们不决定业务语义，只处理当前会话中已存在的工作流计划。
具体规划工具由功能插件提供。
