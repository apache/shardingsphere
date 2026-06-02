+++
title = "Plugin Workflows"
weight = 1
+++

Plugin workflows guide users through requirement confirmation, preview, apply, and validation when a feature plugin creates a reviewable database governance change.
They are not standalone business features. Users usually enter this flow from feature tasks such as data encryption or data masking.

## When to use

- Create, alter, or drop ShardingSphere rules.
- Review DistSQL, DDL, or index suggestions before a change.
- Export a manual execution package instead of applying changes automatically.
- Validate rule state, logical metadata, and SQL executability after execution.

## Basic flow

| Phase | User action | Focus |
| --- | --- | --- |
| Describe the requirement | Provide the logical database, table, column, and governance goal. | Clear input makes the generated plan more stable. |
| Provide missing information | Add algorithm choices, parameters, secret placeholders, or execution preferences when asked. | Sensitive values should be provided through protected channels. |
| Review the plan | Review generated DistSQL, DDL, index suggestions, and impact scope. | Confirm that the plan matches business expectations. |
| Preview the change | Ask to preview first without changing runtime state. | Check statements and side effects before execution. |
| Apply the change | Confirm automatic execution, or export a manual package for operators. | Side-effecting changes must be confirmed. |
| Validate the result | Inspect rule state, metadata, and SQL executability after execution. | Confirm that the change has taken effect. |

## Execution choices

| Choice | Changes runtime state | Use case |
| --- | --- | --- |
| Preview only | No | Inspect change artifacts and side-effect scope first. |
| Execute after review | Yes | Execute the change through the MCP Server after user confirmation. |
| Manual package | No | Return statements for operators to review and execute manually. |

## Sensitive inputs

Plugins may require sensitive fields such as keys or credentials.
These values should not be written into ordinary documents, chat records, or logs.

Recommended handling:

- Keep placeholders in the plan.
- Let the client or operator obtain real values through a secret manager, protected environment variable, or controlled operations channel.
- Replace placeholders and execute in a controlled environment.

ShardingSphere-MCP does not read secret managers directly.
When using a manual package, placeholders can remain in DistSQL and be replaced by the operator in a controlled environment.

## Review checklist

- Whether DistSQL matches the expected rule change.
- Whether DDL or index suggestions fit the current physical table structure.
- Whether runtime rules, metadata, or data may be changed.
- Whether manual execution, backup, rollback planning, or business approval is required.
- Whether validation covers rule state, logical metadata, and SQL executability.
