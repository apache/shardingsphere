+++
title = "工作流基础"
weight = 4
+++

ShardingSphere-MCP 的功能插件可以通过共享工作流机制实现复杂治理任务。
MCP Server 提供通用工作流机制，插件提供具体业务语义。

## 基本阶段

一个典型工作流包含：

1. 调用插件自己的规划工具，生成计划并返回 `plan_id`。
2. 如果返回 `status = clarifying`，按 `clarification_questions` 补齐缺失输入。
3. 如果返回 `status = planned`，审查生成的变更产物。
4. 调用 `database_gateway_apply_workflow` 并先使用 `execution_mode=preview`。
5. 审查预览结果后，使用 `execution_mode=review-then-execute` 执行，或使用 `manual-only` 导出人工执行包。
6. 调用 `database_gateway_validate_workflow` 校验最终状态。

## 会话与 plan_id

- `plan_id` 是当前工作流的句柄。
- `plan_id` 只在当前 MCP 会话内有效。
- `plan`、`apply`、`validate` 必须使用同一个 `MCP-Session-Id`。
- 第一次规划调用不需要传 `plan_id`。
- 后续补问、执行和校验必须继续传同一个 `plan_id`。

## 常见状态

- `clarifying`：信息不足，需要补齐参数。
- `planned`：计划已生成，可以审查变更产物。
- `completed`：已执行完成。
- `awaiting-manual-execution`：选择了 `manual-only`，需要人工执行返回的变更产物。
- `validated`：校验已通过。
- `failed`：当前阶段失败，应查看 `issues`、`mismatches` 和 `recovery_guidance`。

## 执行模式

`database_gateway_apply_workflow` 必须显式传入 `execution_mode`：

- `preview`：只预览变更产物和副作用范围，不修改服务状态。
- `review-then-execute`：在审查后执行变更产物。
- `manual-only`：不自动执行，返回人工执行包。

如果使用 `approved_steps` 分步执行，只能使用预览结果返回的 `preview_artifacts[].approval_step` 值。
未知步骤会被拒绝。

## 敏感输入

插件可能要求补充敏感字段，例如密钥或凭证。
带有 `secret: true`、`input_type: "secret"`，或字段名包含 password、token、key、secret、credential 的补问，不应通过普通表单明文回传。

推荐处理方式：

- 保留 `plan_id`。
- 通过密钥管理系统、受保护环境变量或运维控制通道取得值。
- 用同一个 `plan_id` 再次调用原规划工具。

## 通用工具

`database_gateway_apply_workflow` 和 `database_gateway_validate_workflow` 是通用工作流工具。
它们不决定业务语义，只处理当前会话中已存在的工作流计划。
具体规划工具由功能插件提供。
