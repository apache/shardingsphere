+++
title = "Data Encryption"
weight = 2
+++

The Data Encryption MCP feature plugin helps users plan, review, apply, and validate data encryption rule changes for ShardingSphere-Proxy logical databases.
Actual encryption capability is provided by ShardingSphere-Proxy and its encryption algorithm plugins.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct physical database connections. A physical database usually does not understand ShardingSphere encryption DistSQL and cannot expose Proxy-visible encryption algorithm plugins or rule state.
- The target logical table and column should be discoverable through JDBC metadata exposed by Proxy. This metadata should not be treated as a complete physical database catalog.

## Use through natural language

Users describe the encryption goal in an AI application that integrates ShardingSphere-MCP.

Examples:

- Check whether `<logic-database>.orders.status` already has an encryption rule.
- Plan reversible encryption for `<logic-database>.orders.status` with equality query support, and preview it without execution.
- Continue the previous plan with the AES algorithm and provide the key through a protected channel.
- Confirm and execute the previous encryption rule plan, then validate the result.

Users should review DistSQL, DDL, index suggestions, and side-effect scope before approving any side-effecting execution.

## Describe an encryption requirement

When using natural language, include the following information when possible:

| Information | Description | Example |
| --- | --- | --- |
| Logical database, table, and column | Specify the ShardingSphere-Proxy logical object to configure. | "Configure encryption for `<logic-database>.orders.status`." |
| Schema or namespace | Recommended for multi-schema logical databases. | "The schema is `public`." |
| Operation type | Create, alter, or drop an encryption rule. | "Create an encryption rule" or "drop the encryption rule for this column." |
| Encryption goal | Describe whether reversible encryption, equality query, or LIKE query is required. | "Use reversible encryption and support equality queries." |
| Algorithm preference | Specify an algorithm, or let MCP recommend one from available algorithms. | "Prefer AES." |
| Algorithm properties | Sensitive values such as keys should be supplied through protected channels. | "The key is supplied through a protected channel." |
| Index suggestions | Whether physical index suggestions for assisted query columns are allowed. | "Allow index suggestions for the assisted query column." |

## Create, alter, and drop rules

| Operation | Natural language example | Plan content |
| --- | --- | --- |
| Create | "Plan reversible encryption for `orders.status`, support equality queries, and preview first." | Generates new rule DistSQL, and physical derived-column DDL or index suggestions when needed. |
| Alter | "Change the previous encryption plan to use AES, then preview it again." | Generates alter-rule DistSQL that keeps sibling column rules on the same table and updates DDL or index suggestions when needed. |
| Drop | "Drop the encryption rule for `orders.status` and preview the impact first." | Generates DistSQL to drop the target column rule. Sibling encrypted columns on the same table are preserved. |

## Review the encryption plan

After a plan is generated, review:

- Whether DistSQL matches the expected create, alter, or drop operation.
- Whether physical derived columns are required.
- Whether index suggestions are generated and suitable for the current physical table structure.
- Whether keys, credentials, or other sensitive parameters are passed only through placeholders or protected channels.
- Whether query capability, runtime rules, or existing business SQL may be affected.

## Derived columns and index plans

Encryption rules may need physical derived columns to store ciphertext or support queries.
MCP creates derived-column suggestions from the logical column, user intent, and existing rules.

- `*_cipher` stores ciphertext and is the default derived column for encryption rules.
- If equality query is required, `*_assisted_query` is generated. Its index plan is generated when index DDL is allowed.
- If LIKE query is required, `*_like_query` is generated for LIKE query scenarios.
- If a default column name conflicts, the system appends a numeric suffix and returns the final name in the plan.

## Preview, apply, and validate

Preview first, then review DistSQL, DDL, index plans, and side-effect scope before execution.

| Phase | Natural language example | User focus |
| --- | --- | --- |
| Preview | "Preview the previous encryption rule plan without executing it." | Inspect DistSQL, DDL, and index suggestions before execution. |
| Execute | "Confirm and execute the previous plan." | Confirm that the side-effecting change has been reviewed. |
| Manual execution | "Export a manual execution package without automatic execution." | Let operators review and execute in a controlled environment. |
| Validate | "Validate whether the previous encryption rule has taken effect." | Check rule state, logical metadata, physical column suggestions, and SQL executability. |

For the general review flow of plugin changes, see [Plugin Workflows](../plugin-workflow/).

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct physical database connections.

### MCP plugin boundaries

- The MCP Server does not implement encryption algorithms and does not replace the user's judgment on whether an encryption strategy satisfies business security requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Dropping an encryption rule removes the rule only. It does not restore historical plaintext data, and physical derived columns or indexes still require manual cleanup when they are no longer needed.

### Proxy-visible metadata boundaries

- MCP generates derived-column, index, and column-type suggestions from logical metadata exposed by Proxy. It does not inspect every physical database directly.
- Review generated DDL against the real physical table structure before applying it.

### ShardingSphere feature boundaries

- Existing data migration or backfill is not handled.

### SQL generation boundaries

- MCP handles quoted, case-sensitive, keyword, whitespace, and Unicode identifiers. To keep generated SQL or DistSQL reviewable, identifier content must not contain backticks, NUL, carriage returns, or line feeds.
