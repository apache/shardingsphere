+++
title = "Workflow Basics"
weight = 4
+++

ShardingSphere-MCP feature plugins can use the shared workflow mechanism to implement complex governance tasks.
The MCP runtime provides the common workflow mechanism, while each plugin provides its own business semantics.

## Basic phases

A typical workflow contains:

1. Call the plugin planning tool to create a plan and return `plan_id`.
2. If the response returns `status = clarifying`, provide the missing inputs from `clarification_questions`.
3. If the response returns `status = planned`, review the generated artifacts.
4. Call `database_gateway_apply_workflow` with `execution_mode=preview` first.
5. After reviewing the preview, call with `execution_mode=review-then-execute`, or use `manual-only` to export an artifact package.
6. Call `database_gateway_validate_workflow` to validate the final state.

## Session and plan_id

- `plan_id` is the handle for the current workflow.
- `plan_id` is valid only in the current MCP session.
- `plan`, `apply`, and `validate` must use the same `MCP-Session-Id`.
- The first planning call does not need `plan_id`.
- Follow-up clarification, apply, and validate calls must reuse the same `plan_id`.

## Common statuses

- `clarifying`: more input is required.
- `planned`: the plan is ready and artifacts should be reviewed.
- `completed`: apply has completed.
- `awaiting-manual-execution`: `manual-only` was selected; execute the returned artifacts manually.
- `validated`: validation passed.
- `failed`: the current phase failed; inspect `issues`, `mismatches`, and `recovery_guidance`.

## Execution modes

`database_gateway_apply_workflow` requires an explicit `execution_mode`:

- `preview`: preview artifacts and side-effect scope without changing runtime state.
- `review-then-execute`: execute artifacts after review.
- `manual-only`: export a manual artifact package without automatic execution.

When using `approved_steps` for partial execution, pass only values returned by `preview_artifacts[].approval_step`.
Unknown steps are rejected.

## Sensitive inputs

Plugins may ask for secret fields, such as keys or credentials.
Questions with `secret: true`, `input_type: "secret"`, or field names containing password, token, key, secret, or credential should not be returned in plain text through ordinary forms.

Recommended handling:

- Keep the `plan_id`.
- Obtain the value through a secret manager, protected environment variable, or controlled operations channel.
- Call the original planning tool again with the same `plan_id`.

## Common tools

`database_gateway_apply_workflow` and `database_gateway_validate_workflow` are common workflow tools.
They do not define business semantics; they operate on workflow plans that already exist in the current session.
Feature plugins provide the concrete planning tools.
