+++
title = "Data Encryption"
weight = 2
+++

The Data Encryption MCP feature plugin helps users plan, review, apply, and validate data encryption rule changes for ShardingSphere-Proxy logical databases.
Actual encryption capability is provided by ShardingSphere-Proxy and its encryption algorithm plugins.
This feature only generates and applies encryption rule DistSQL. It does not generate physical DDL, derived-column DDL, index suggestions, or data migration tasks.

## Prerequisites

- The current version supports logical databases exposed by ShardingSphere-Proxy only.
- `runtimeDatabases` should point to Proxy logical databases, not physical storage databases.
- This feature does not apply to direct database connections. The target database usually does not understand ShardingSphere encryption rule change statements and cannot expose Proxy-visible encryption algorithm plugins or rule state.
- Users need to provide the target logical database, table, and column names. Planning does not inspect the real physical table structure.

## Use through natural language

Users describe the encryption goal in an AI application that integrates ShardingSphere-MCP.

Examples:

- Check whether `<logic-database>.orders.status` already has an encryption rule.
- List data encryption algorithms available from the current Proxy.
- Plan reversible encryption for `<logic-database>.orders.status` with equality query support, and preview it without execution.
- Continue the previous plan with the AES algorithm and provide the key through a protected channel.
- Confirm and execute the previous encryption rule plan, then validate the result.

Users should review encryption rule DistSQL, algorithm properties, rule column names, and side-effect scope before approving any side-effecting execution.

## Describe an encryption requirement

When using natural language, include the following information when possible:

| Information                         | Description                                                                                            | Example                                                                              |
|-------------------------------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| Logical database, table, and column | Specify the ShardingSphere-Proxy logical object to configure.                                          | "Configure encryption for `<logic-database>.orders.status`."                         |
| Schema or namespace                 | Recommended for multi-schema logical databases.                                                        | "The schema is `public`."                                                            |
| Operation type                      | Create, alter, or drop an encryption rule.                                                             | "Create an encryption rule" or "drop the encryption rule for this column."           |
| Encryption goal                     | Describe whether reversible encryption, equality query, or LIKE query is required.                     | "Use reversible encryption and support equality queries."                            |
| Algorithm preference                | Specify an algorithm, or let MCP recommend one from algorithms available from Proxy.                   | "List data encryption algorithms available from the current Proxy." or "Prefer AES." |
| Algorithm properties                | Sensitive values such as keys should be supplied through protected channels.                           | "The key is supplied through a protected channel."                                   |
| Rule column names                   | Provide column names used by the rule when cipher, assisted query, or LIKE query columns are required. | "The cipher column name is `status_cipher`."                                         |

## Create, alter, and drop rules

| Operation | Natural language example                                                                       | Content to review                                                                              |
|-----------|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| Create    | "Plan reversible encryption for `orders.status`, support equality queries, and preview first." | The new encryption rule, algorithms, properties, and rule column names.                        |
| Alter     | "Change the previous encryption plan to use AES, then preview it again."                       | The altered encryption rule and whether sibling encrypted columns are preserved.               |
| Drop      | "Drop the encryption rule for `orders.status` and preview the impact first."                   | Whether the target column rule is dropped and whether sibling encrypted columns are preserved. |

## Review the encryption plan

After a plan is generated, review:

- Whether the statements match the expected create, alter, or drop operation.
- Whether logical, cipher, assisted query, or LIKE query column names in the rule DistSQL match expectations.
- Whether keys, credentials, or other sensitive parameters are passed only through placeholders or protected channels.
- Whether query capability, runtime rules, or existing business SQL may be affected.

## Review Rule Column Names

Encryption rules may reference cipher, assisted query, or LIKE query columns.
These column names are part of rule DistSQL only. ShardingSphere-MCP does not generate physical DDL for them or check whether the real physical table already has matching columns.

- `*_cipher` is commonly used as the cipher column name.
- If equality query is required, configure an assisted query column and assisted query algorithm in the rule.
- If LIKE query is required, configure a LIKE query column and LIKE query algorithm in the rule.
- If the real physical table needs new columns, indexes, or historical data processing, handle them outside ShardingSphere-MCP.

## Preview, apply, and validate

Preview first, then review rule DistSQL and side-effect scope before execution.

| Phase            | Natural language example                                          | User focus                                                         |
|------------------|-------------------------------------------------------------------|--------------------------------------------------------------------|
| Preview          | "Preview the previous encryption rule plan without executing it." | Inspect rule DistSQL, algorithms, and properties before execution. |
| Execute          | "Confirm and execute the previous plan."                          | Confirm that the side-effecting change has been reviewed.          |
| Manual execution | "Export a manual execution package without automatic execution."  | Let operators review and execute in a controlled environment.      |
| Validate         | "Validate whether the previous encryption rule has taken effect." | Check rule state and workflow execution result.                    |

For the general review flow of rule changes, see [Rule Change Flow](../plugin-workflow/).

## Limitations

### Supported scope

- Supports ShardingSphere-Proxy logical databases only.
- This feature does not apply to direct database connections.

### Capability boundaries

- ShardingSphere-MCP does not provide encryption algorithms and does not replace the user's judgment on whether an encryption strategy satisfies business security requirements.
- Planning results are reviewable change plans. Execution still requires user confirmation.
- Planning and execution are limited to encryption rule DistSQL. Physical DDL, indexes, data migration, backfill, or data cleansing tasks are not generated or executed.
- Dropping an encryption rule removes the rule only. It does not restore historical plaintext data, and unused physical columns or indexes still require manual cleanup.

### Metadata boundaries

- ShardingSphere-MCP plans rule DistSQL from Proxy-visible rule and algorithm state. Planning does not read or infer the real physical table structure.
- If a physical column referenced by the rule does not exist, users should handle DDL and data processing outside ShardingSphere-MCP.

### ShardingSphere capability boundaries

- Existing data migration, backfill, or data cleansing is not handled.

### Identifier handling boundaries

- ShardingSphere-MCP handles quoted, case-sensitive, keyword, whitespace, and Unicode object names. To keep generated SQL or rule change statements reviewable, object name content must not contain backticks, NUL, carriage returns, or line feeds.
