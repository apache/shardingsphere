+++
title = "Data Masking"
weight = 3
+++

The Data Masking MCP feature plugin helps users plan, review, apply, and validate data masking rule changes for ShardingSphere-Proxy logical databases.
Mask rules apply directly to logical columns and do not generate physical derived columns used by the Encrypt feature.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct physical database connections. A physical database usually does not understand ShardingSphere masking DistSQL and cannot expose Proxy-visible masking algorithm plugins or rule state.
- The target logical table and column should be discoverable through JDBC metadata exposed by Proxy. This metadata should not be treated as a complete physical database catalog.

## Use through natural language

Users describe the masking goal in the MCP client.

Examples:

- Check whether `<logic-database>.orders.phone` already has a masking rule.
- Plan phone-number masking for `<logic-database>.orders.phone`, keep the first 3 and last 4 characters, and preview it without execution.
- Adjust the previous plan to use `*` as the replacement character.
- Confirm and execute the previous masking rule plan, then validate the result.

Users should review DistSQL and side-effect scope before approving any side-effecting execution.

## Describe a masking requirement

When using natural language, include the following information when possible:

| Information | Description | Example |
| --- | --- | --- |
| Logical database, table, and column | Specify the ShardingSphere-Proxy logical object to configure. | "Configure masking for `<logic-database>.orders.phone`." |
| Schema or namespace | Recommended for multi-schema logical databases. | "The schema is `public`." |
| Operation type | Create, alter, or drop a masking rule. | "Create a masking rule" or "drop the masking rule for this column." |
| Masking goal | Describe retained characters, replacement characters, or other masking effects. | "Keep the first 3 and last 4 phone-number characters, and replace the middle part with `*`." |
| Algorithm preference | Specify an algorithm, or let MCP recommend one from available algorithms. | "Prefer the keep-first-n-last-m algorithm." |
| Algorithm properties | Provide retained character counts and replacement characters. | "Keep the first 3 and last 4 characters, and use `*` as the replacement character." |

## Create, alter, and drop rules

| Operation | Natural language example | Plan content |
| --- | --- | --- |
| Create | "Plan phone-number masking for `orders.phone` and preview it without execution." | Generates DistSQL for adding the rule. |
| Alter | "Change the previous masking rule to keep the first 3 and last 4 characters." | Generates DistSQL that preserves sibling column rules on the same table. |
| Drop | "Drop the masking rule for `orders.phone` and preview the impact first." | Generates DistSQL to drop the target column rule. Sibling masking columns on the same table are preserved. |

## Review the masking plan

After a plan is generated, review:

- Whether DistSQL matches the expected create, alter, or drop operation.
- Whether the masking algorithm and properties satisfy business compliance requirements.
- Whether queries through Proxy no longer apply masking to the column after dropping a rule.
- Whether runtime rules or existing business SQL may be affected.

## Apply and validate

Preview first, then review DistSQL and side-effect scope before execution.

| Phase | Natural language example | User focus |
| --- | --- | --- |
| Preview | "Preview the previous masking rule plan without executing it." | Inspect DistSQL and side-effect scope before execution. |
| Execute | "Confirm and execute the previous plan." | Confirm that the side-effecting change has been reviewed. |
| Manual execution | "Export a manual execution package without automatic execution." | Let operators review and execute in a controlled environment. |
| Validate | "Validate whether the previous masking rule has taken effect." | Check rule state, logical metadata, and SQL executability. |

For the general review flow of plugin changes, see [Plugin Workflows](../plugin-workflow/).

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct physical database connections.

### MCP plugin boundaries

- The MCP Server does not implement masking algorithms and does not replace the user's judgment on whether a masking strategy satisfies business compliance requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Dropping a masking rule removes the rule only. Later queries through Proxy no longer apply that masking rule to the column.

### Proxy-visible metadata boundaries

- Logical column and rule validation are based on what Proxy exposes.
- Direct physical database connections can execute ordinary SQL only and do not represent masking rule state.

### ShardingSphere feature boundaries

- Automatic rollback is not provided.

### Planner input limits

- Identifier content must not contain backticks, NUL, carriage-return, or line-feed characters because they cannot be rendered as reviewable SQL.
