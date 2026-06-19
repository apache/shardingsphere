+++
title = "Data Masking"
weight = 3
+++

The Data Masking MCP feature plugin helps users plan, review, apply, and validate data masking rule changes for ShardingSphere-Proxy logical databases.
Mask rules apply directly to logical columns. This feature only generates and applies masking rule DistSQL. It does not generate physical DDL, index suggestions, data migration, or extra probing SQL.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct database connections. The target database usually does not understand ShardingSphere masking rule change statements and cannot expose Proxy-visible masking algorithm plugins or rule state.
- Users need to provide the target logical database, table, and column names. Planning does not inspect the real physical table structure.

## Use through natural language

Users describe the masking goal in an AI application that integrates ShardingSphere-MCP.

Examples:

- Check whether `<logic-database>.orders.phone` already has a masking rule.
- List data masking algorithms available from the current Proxy.
- Plan phone-number masking for `<logic-database>.orders.phone`, keep the first 3 and last 4 characters, and preview it without execution.
- Adjust the previous plan to use `*` as the replacement character.
- Confirm and execute the previous masking rule plan, then validate the result.

Users should review masking rule DistSQL, algorithm properties, and side-effect scope before approving any side-effecting execution.

## Describe a masking requirement

When using natural language, include the following information when possible:

| Information                         | Description                                                                          | Example                                                                                                         |
|-------------------------------------|--------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| Logical database, table, and column | Specify the ShardingSphere-Proxy logical object to configure.                        | "Configure masking for `<logic-database>.orders.phone`."                                                        |
| Schema or namespace                 | Recommended for multi-schema logical databases.                                      | "The schema is `public`."                                                                                       |
| Operation type                      | Create, alter, or drop a masking rule.                                               | "Create a masking rule" or "drop the masking rule for this column."                                             |
| Masking goal                        | Describe retained characters, replacement characters, or other masking effects.      | "Keep the first 3 and last 4 phone-number characters, and replace the middle part with `*`."                    |
| Algorithm preference                | Specify an algorithm, or let MCP recommend one from algorithms available from Proxy. | "List data masking algorithms available from the current Proxy." or "Prefer the keep-first-n-last-m algorithm." |
| Algorithm properties                | Provide retained character counts and replacement characters.                        | "Keep the first 3 and last 4 characters, and use `*` as the replacement character."                             |

## Create, alter, and drop rules

| Operation | Natural language example                                                         | Content to review                                                                            |
|-----------|----------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| Create    | "Plan phone-number masking for `orders.phone` and preview it without execution." | The new masking rule, masking algorithm, and properties.                                     |
| Alter     | "Change the previous masking rule to keep the first 3 and last 4 characters."    | The altered masking rule and whether sibling masking columns are preserved.                  |
| Drop      | "Drop the masking rule for `orders.phone` and preview the impact first."         | Whether the target column rule is dropped and whether sibling masking columns are preserved. |

## Review the masking plan

After a plan is generated, review:

- Whether the statements match the expected create, alter, or drop operation.
- Whether the masking algorithm and properties satisfy business compliance requirements.
- Whether queries through Proxy no longer apply masking to the column after dropping a rule.
- Whether runtime rules or existing business SQL may be affected.
- Whether no extra DDL, index, data processing, or SQL executability probing task is expected from ShardingSphere-MCP.

## Sensitive parameter handling

Some masking algorithm parameters may need to be supplied by operators in a controlled way, such as replacement characters or custom algorithm properties.
Use a secret reference object in algorithm properties:

```json
{
  "primary_algorithm_properties": {
    "replace-char": {
      "secret_ref": "placeholder://secret-value-1"
    }
  }
}
```

The `secret_ref` in a placeholder object only marks a sensitive slot for manual replacement.
Planning, preview, execution results, and validation output show only neutral placeholders or `******`; they do not echo `secret_ref` or real sensitive values.
If a rule change still contains sensitive placeholders, automatic execution returns `secret_reference_manual_execution_required` before side effects. Operators should replace real values outside MCP and the AI application, then execute manually.

## Apply and validate

Preview first, then review rule DistSQL and side-effect scope before execution.

| Phase            | Natural language example                                         | User focus                                                        |
|------------------|------------------------------------------------------------------|-------------------------------------------------------------------|
| Preview          | "Preview the previous masking rule plan without executing it."   | Inspect rule DistSQL, algorithm, and properties before execution. |
| Execute          | "Confirm and execute the previous plan."                         | Confirm that the side-effecting change has been reviewed.         |
| Manual execution | "Export a manual execution package without automatic execution." | Let operators review and execute in a controlled environment.     |
| Validate         | "Validate whether the previous masking rule has taken effect."   | Check rule state and workflow execution result.                   |

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct database connections.

### Capability boundaries

- ShardingSphere-MCP does not provide masking algorithms and does not replace the user's judgment on whether a masking strategy satisfies business compliance requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Planning and execution are limited to masking rule DistSQL. Physical DDL, indexes, data migration, backfill, data cleansing, or SQL executability probing tasks are not generated or executed.
- Dropping a masking rule removes the rule only. Later queries through Proxy no longer apply that masking rule to the column.

### Metadata boundaries

- Rule planning is based on Proxy-visible rule and algorithm state. Planning does not read or infer the real physical table structure.
- Direct database connections can execute ordinary SQL only and do not represent masking rule state.

### Identifier handling boundaries

- ShardingSphere-MCP handles quoted, case-sensitive, keyword, whitespace, and Unicode object names. To keep generated SQL or rule change statements reviewable, object name content must not contain backticks, NUL, carriage returns, or line feeds.
