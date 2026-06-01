+++
title = "Plugin Workflows"
weight = 3
+++

Plugin workflows are the shared mechanism that ShardingSphere-MCP uses for multi-step governance changes. They are not standalone business features.
Feature plugins understand concrete business semantics and create `plan_id` plus change artifacts. The MCP Server stores the current-session plan and provides shared phase interfaces for preview, apply, export, and validation.

Users do not use plugin workflows just to read metadata, search objects, or run read-only SQL.
Follow this page only after a plugin planning tool returns `plan_id`; then the model or client can review, apply, and validate that plan.
This page explains plugin workflows because these phase tools appear in the MCP tool list, and multiple plugins share the same state model, execution modes, and sensitive-input handling.
The concrete planning capabilities are still documented on the corresponding feature plugin pages.

## Basic phases

A typical workflow contains:

1. Call the feature plugin planning tool to create a plan and return `plan_id`.
2. If the response returns `status = clarifying`, provide the missing inputs from `clarification_questions`.
3. If the response returns `status = planned`, review the generated change artifacts.
4. The model or client calls `database_gateway_apply_workflow` with `execution_mode=preview` first.
5. After the user reviews the preview, the model or client calls with `execution_mode=review-then-execute`, or uses `manual-only` to export a manual execution package.
6. The model or client calls `database_gateway_validate_workflow` to validate the final state.

## Session and plan_id

- `plan_id` is the handle for the current workflow.
- `plan_id` is valid only in the current MCP session.
- `plan`, `apply`, and `validate` must use the same `MCP-Session-Id`.
- The first planning call does not need `plan_id`.
- Follow-up clarification, apply, and validate calls must reuse the same `plan_id`.

## Common statuses

| Status | Meaning | Next step |
| --- | --- | --- |
| `clarifying` | More input is required. | Call the original planning tool again with the same `plan_id`. |
| `planned` | The plan is ready and change artifacts should be reviewed. | Preview the plan or export a manual package. |
| `completed` | Apply has completed. | Call the validation tool to confirm the final state. |
| `awaiting-manual-execution` | `manual-only` was selected. | Execute the returned artifacts manually, then validate. |
| `validated` | Validation passed. | Return the result to the user. |
| `failed` | The current phase failed. | Inspect `issues`, `mismatches`, and `recovery_guidance`. |

## Execution modes

`database_gateway_apply_workflow` requires an explicit `execution_mode`:

| Execution mode | Changes runtime state | Purpose |
| --- | --- | --- |
| `preview` | No | Preview change artifacts and side-effect scope only. |
| `review-then-execute` | Yes | Execute change artifacts through the MCP Server after review. |
| `manual-only` | No | Export a manual artifact package without automatic execution. |

When using `approved_steps` for partial execution, pass only values returned by `preview_artifacts[].approval_step`.
Unknown steps are rejected.

## Sensitive inputs

Plugins may ask for secret fields, such as keys or credentials.
Questions with `secret: true`, `input_type: "secret"`, or field names containing password, token, key, secret, or credential should not be returned in plain text through ordinary forms.

Recommended handling:

- Keep the `plan_id`.
- Let the client or operator obtain the value through a secret manager, protected environment variable, or controlled operations channel.
- Call the original planning tool again with the same `plan_id`, and pass the secret field only through a protected MCP call.

ShardingSphere-MCP does not read secret managers directly.
When using a manual package, keep placeholders in the returned DistSQL and let the operator replace them in a controlled environment.

Example:

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

## Phase tools

`database_gateway_apply_workflow` and `database_gateway_validate_workflow` appear in the MCP tool list, so they need to be documented.
They are not standalone business features. They are phase interfaces used after plugin planning returns `plan_id`.
Users usually do not choose them directly. A model or client calls them during review, apply, and validation phases.

| Tool | Actual role | When to use |
| --- | --- | --- |
| `database_gateway_apply_workflow` | Previews an existing `plan_id`, executes reviewed artifacts, or exports a manual package. It does not create encryption, masking, or other business plans. | After a plugin planning tool returns `status = planned`. |
| `database_gateway_validate_workflow` | Validates a plan or execution result against visible metadata, rule state, and generated artifacts. It does not execute business changes. | After automatic or manual execution finishes. |

They do not define business semantics; they operate on workflow plans that already exist in the current session.
Feature plugins provide the concrete planning tools.
