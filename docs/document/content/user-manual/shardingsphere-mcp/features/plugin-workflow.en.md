+++
title = "Rule Change Flow"
weight = 1
+++

The rule change flow guides users through requirement confirmation, preview, apply, and validation when features such as data encryption or data masking create reviewable database governance changes.
It is not a standalone business feature. Users usually enter this flow from a concrete data encryption or data masking task.

## When to Use

- Create, alter, or drop ShardingSphere rules.
- Review statements, physical columns, or index suggestions before a change.
- Export a manual execution package instead of applying changes automatically.
- Validate rule state, logical metadata, and SQL executability after execution.

## Basic Flow

| Phase | User action | Focus |
| --- | --- | --- |
| Describe the requirement | Provide the logical database, table, column, and governance goal. | Clear input makes the generated plan more stable. |
| Provide missing information | Add algorithm choices, parameters, secret placeholders, or execution preferences when asked. | Sensitive values should be provided through protected channels. |
| Review the plan | Review generated statements, physical columns or index suggestions, and impact scope. | Confirm that the plan matches business expectations. |
| Preview the change | Ask to preview first without changing runtime state. | Check statements and side effects before execution. |
| Apply the change | Confirm automatic execution, or export a manual package for operators. | Side-effecting changes must be confirmed. |
| Validate the result | Inspect rule state, metadata, and SQL executability after execution. | Confirm that the change has taken effect. |

## Change Execution Choices

| User wording | What users receive | Focus |
| --- | --- | --- |
| "Preview first, do not execute." | Change content and impact scope only. | Use this to confirm statements, physical columns, index suggestions, and side effects first. |
| "Confirm and execute the previous plan." | The previewed and confirmed change is executed. | Use this only after the user has completed review. |
| "Export a manual execution package." | Statements that operators can review and execute manually. | Use this when approval, a change window, or a controlled execution environment is required. |

## Sensitive Inputs

Rule changes may require sensitive fields such as keys or credentials.
These values should not be written into ordinary documents, chat records, or logs.

Recommended handling:

- Keep placeholders in the plan.
- Let the AI application or operator obtain real values through a secret manager, protected environment variable, or controlled operations channel.
- Replace placeholders and execute in a controlled environment.

ShardingSphere-MCP does not read secret managers directly.
When using a manual package, placeholders can remain in the statements and be replaced by the operator in a controlled environment.

## Review Checklist

- Whether the statements match the expected rule change.
- Whether physical column or index suggestions fit the current physical table structure.
- Whether runtime rules, metadata, or data may be changed.
- Whether manual execution, backup, rollback planning, or business approval is required.
- Whether validation covers rule state, logical metadata, and SQL executability.
