+++
title = "Data Encryption"
weight = 2
+++

The Data Encryption MCP feature plugin helps users plan, review, apply, and validate data encryption rule changes for ShardingSphere-Proxy logical databases.
Actual encryption capability is provided by ShardingSphere-Proxy and its encryption algorithm plugins.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct database connections. The target database usually does not understand ShardingSphere encryption rule change statements and cannot expose Proxy-visible encryption algorithm plugins or rule state.
- The target logical table and column should be discoverable through JDBC metadata exposed by Proxy. This metadata should not be treated as a complete physical database catalog.

## Use through natural language

Users describe the encryption goal in an AI application that integrates ShardingSphere-MCP.

Examples:

- Check whether `<logic-database>.orders.status` already has an encryption rule.
- List data encryption algorithms available from the current Proxy.
- Plan reversible encryption for `<logic-database>.orders.status` with equality query support, and preview it without execution.
- Continue the previous plan with the AES algorithm and provide the key through a protected channel.
- Confirm and execute the previous encryption rule plan, then validate the result.

Users should review statements, physical columns, index suggestions, and side-effect scope before approving any side-effecting execution.

## Describe an encryption requirement

When using natural language, include the following information when possible:

| Information | Description | Example |
| --- | --- | --- |
| Logical database, table, and column | Specify the ShardingSphere-Proxy logical object to configure. | "Configure encryption for `<logic-database>.orders.status`." |
| Schema or namespace | Recommended for multi-schema logical databases. | "The schema is `public`." |
| Operation type | Create, alter, or drop an encryption rule. | "Create an encryption rule" or "drop the encryption rule for this column." |
| Encryption goal | Describe whether reversible encryption, equality query, or LIKE query is required. | "Use reversible encryption and support equality queries." |
| Algorithm preference | Specify an algorithm, or let MCP recommend one from algorithms available from Proxy. | "List data encryption algorithms available from the current Proxy." or "Prefer AES." |
| Algorithm properties | Sensitive values such as keys should be supplied through protected channels. | "The key is supplied through a protected channel." |
| Index suggestions | Whether physical index suggestions for assisted query columns are allowed. | "Allow index suggestions for the assisted query column." |

## Create, alter, and drop rules

| Operation | Natural language example | Content to review |
| --- | --- | --- |
| Create | "Plan reversible encryption for `orders.status`, support equality queries, and preview first." | The new encryption rule, possible physical derived columns, and index suggestions. |
| Alter | "Change the previous encryption plan to use AES, then preview it again." | The altered encryption rule, whether sibling encrypted columns are preserved, and physical change suggestions. |
| Drop | "Drop the encryption rule for `orders.status` and preview the impact first." | Whether the target column rule is dropped, whether sibling encrypted columns are preserved, and which physical objects need manual cleanup. |

## Review the encryption plan

After a plan is generated, review:

- Whether the statements match the expected create, alter, or drop operation.
- Whether physical derived columns are required.
- Whether index suggestions are generated and suitable for the current physical table structure.
- Whether keys, credentials, or other sensitive parameters are passed only through placeholders or protected channels.
- Whether query capability, runtime rules, or existing business SQL may be affected.

## Review Derived Columns and Index Suggestions

Encryption rules may need physical derived columns to store ciphertext or support queries.
When a plan contains derived columns or index suggestions, users should confirm whether the real physical table can accept new columns or indexes, and whether the generated names match operational conventions.

- `*_cipher` stores ciphertext and is the default derived column for encryption rules.
- If equality query is required, the plan may contain `*_assisted_query` and corresponding index suggestions.
- If LIKE query is required, `*_like_query` is generated for LIKE query scenarios.
- If the plan provides adjusted physical column names, confirm that they do not conflict with existing physical objects.

## Preview, apply, and validate

Preview first, then review statements, physical columns, index suggestions, and side-effect scope before execution.

| Phase | Natural language example | User focus |
| --- | --- | --- |
| Preview | "Preview the previous encryption rule plan without executing it." | Inspect statements, physical columns, and index suggestions before execution. |
| Execute | "Confirm and execute the previous plan." | Confirm that the side-effecting change has been reviewed. |
| Manual execution | "Export a manual execution package without automatic execution." | Let operators review and execute in a controlled environment. |
| Validate | "Validate whether the previous encryption rule has taken effect." | Check rule state, logical metadata, physical column suggestions, and SQL executability. |

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct database connections.

### Capability boundaries

- ShardingSphere-MCP does not provide encryption algorithms and does not replace the user's judgment on whether an encryption strategy satisfies business security requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Dropping an encryption rule removes the rule only. It does not restore historical plaintext data, and physical derived columns or indexes still require manual cleanup when they are no longer needed.

### Metadata boundaries

- ShardingSphere-MCP generates derived-column, index, and column-type suggestions from logical metadata exposed by Proxy. It does not inspect every physical database directly.
- Review generated physical change statements against the real physical table structure before applying them.

### ShardingSphere capability boundaries

- Existing data migration or backfill is not handled.

### Identifier handling boundaries

- ShardingSphere-MCP handles quoted, case-sensitive, keyword, whitespace, and Unicode object names. To keep generated SQL or rule change statements reviewable, object name content must not contain backticks, NUL, carriage returns, or line feeds.
