+++
title = "Rule Change Flow"
weight = 1
+++

The rule change flow guides users through requirement confirmation, preview, apply, and validation when official DistSQL-only features create reviewable database governance changes.
It is not a standalone business feature. Users usually enter this flow from a concrete data encryption, data masking, broadcast, readwrite-splitting, shadow, or sharding task.

## When to Use

- Create, alter, or drop ShardingSphere rules.
- Review rule DistSQL or other change artifacts before a change.
- Export a manual execution package instead of applying changes automatically.
- Validate rule state or workflow execution results according to each feature plugin boundary after execution.

## Basic Flow

| Phase                       | User action                                                                                  | Focus                                                           |
|-----------------------------|----------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| Describe the requirement    | Provide the logical database, table, column, and governance goal.                            | Clear input makes the generated plan more stable.               |
| Provide missing information | Add algorithm choices, parameters, secret placeholders, or execution preferences when asked. | Sensitive values should be provided through protected channels. |
| Review the plan             | Review generated rule DistSQL, change artifacts, and impact scope.                           | Confirm that the plan matches business expectations.            |
| Preview the change          | Ask to preview first without changing runtime state.                                         | Check statements and side effects before execution.             |
| Apply the change            | Confirm automatic execution, or export a manual package for operators.                       | Side-effecting changes must be confirmed.                       |
| Validate the result         | Check rule state or workflow execution results returned by the feature plugin.               | Confirm that the change has taken effect.                       |

## Plan Response Fields

Planning tools return a `plan_id` that links the generated plan to workflow resources, preview, apply, and validation tools.
Use the workflow resource to review the persisted plan before applying it.
Use preview before execution when the user needs one more confirmation step.

Model-facing planning responses may include:

- `summary`: a short model-facing status line that tells whether the plan needs clarification, is ready for preview, or failed.
- `algorithm_recommendations`: candidate algorithms selected from Proxy-visible plugin catalogs or explicit user input.
- `property_requirements`: required or optional properties for the selected algorithms. Missing required properties keep the workflow in clarification instead of generating unsafe artifacts.
- `resources_to_read` and `next_actions`: resource and tool navigation hints for continuing the workflow.
- `distsql_artifacts`: reviewable rule DistSQL generated inside the current feature plugin boundary.

Preview, apply, manual-only export, and validation responses also return `summary` and `next_actions`.
Clients should follow these fields before inventing a replacement call or asking the user for information already present in the payload.

## Change Execution Choices

| User wording                             | What users receive                                         | Focus                                                                                       |
|------------------------------------------|------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| "Preview first, do not execute."         | Change content and impact scope only.                      | Use this to confirm statements, change artifacts, and side effects first.                   |
| "Confirm and execute the previous plan." | The previewed and confirmed change is executed.            | Use this only after the user has completed review.                                          |
| "Export a manual execution package."     | Statements that operators can review and execute manually. | Use this when approval, a change window, or a controlled execution environment is required. |

## Sensitive Inputs

Rule changes may require sensitive fields such as keys or credentials.
These values should not be written into ordinary documents, chat records, or logs.

Recommended handling:

- Use a secret placeholder object in algorithm properties, for example `{"secret_ref": "placeholder://secret-value-1"}`.
- Review rule DistSQL with `execution_mode=preview`; preview responses show only neutral placeholders or `******`.
- If the workflow still contains sensitive placeholders, automatic execution stops before side effects and returns `secret_reference_manual_execution_required`.
- When using a manual execution package, operators replace real values outside MCP and the AI application before executing it.

ShardingSphere-MCP only records the sensitive slot that requires manual replacement; it does not fetch real sensitive values from external systems.

## Review Checklist

- Whether the statements match the expected rule change.
- Whether change artifacts match the current feature plugin's capability boundary.
- Official feature plugins only generate rule DistSQL within their documented boundary and do not generate physical DDL, indexes, migration, backfill, data cleansing, physical metadata probes, or storage unit mutation tasks.
- Whether runtime rules, metadata, or data may be changed.
- Whether manual execution, backup, rollback planning, or business approval is required.
- Whether validation matches the rule-state or workflow-result boundary documented by the corresponding feature plugin.
