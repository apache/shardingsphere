+++
title = "Capability Catalog"
weight = 2
+++

This page describes the database tasks that users can complete through natural language, and the usage boundaries when connecting to ShardingSphere-Proxy or a regular database.
For protocol-level integration details, see the [Developer Appendix](../developer-appendix/).

## Connection Targets

### Connecting to ShardingSphere-Proxy

Use this mode to inspect ShardingSphere logical database structure, read rule state, run controlled SQL, or create reviewable rule change plans.

Available tasks include:

- Inspecting logical databases, schemas, tables, views, columns, indexes, and sequences.
- Searching metadata objects such as tables, views, columns, and indexes.
- Running read-only SQL queries.
- Previewing SQL that may change data, metadata, or rules.
- Planning, reviewing, applying, and validating data encryption or data masking rule changes.

Usage boundaries:

- Users see logical metadata exposed by Proxy. It is not equivalent to the complete catalog of every physical database.
- Tasks that depend on ShardingSphere rules, algorithms, or rule change statements apply only to Proxy connections.
- Side-effecting tasks should be previewed or planned first, then reviewed before execution.

### Connecting Directly to a Database

Use this mode when ShardingSphere-MCP acts as a controlled access entry for a regular database, including metadata inspection, object search, and restricted SQL execution.

Available tasks include:

- Inspecting databases, schemas, tables, views, columns, indexes, and sequences.
- Searching metadata objects.
- Running read-only SQL queries.
- Previewing or executing ordinary DML, DDL, and DCL after explicit authorization.

Usage boundaries:

- ShardingSphere rule state is not available.
- Feature plugins that depend on ShardingSphere rules, such as data encryption and data masking, do not apply.
- Returned metadata comes from the target database itself and does not represent a ShardingSphere logical rule view.

## Metadata Inspection

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| List accessible databases | "List the logical databases that can be accessed." | Proxy or regular database | Confirm that database names match the configuration. |
| Inspect schemas or namespaces | "Show schemas in `<logic-database>`." | Proxy or regular database | For multi-schema databases, confirm the target schema first. |
| Inspect tables or views | "List tables and views in `<schema-name>`." | Proxy or regular database | Proxy connections show logical objects. |
| Inspect columns | "Show columns and column types for `<table-name>`." | Proxy or regular database | Column types follow metadata visible from the connection target. |
| Inspect indexes | "Show indexes for `<table-name>`." | Proxy or regular database | With Proxy connections, index information may differ from the full physical database structure. |
| Inspect sequences | "List sequences in `<schema-name>`." | Proxy or regular database | Available only when the connection target exposes sequence metadata. |

## Metadata Search

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| Search objects by name | "Find tables whose names contain `order`." | Proxy or regular database | Useful when the full object name is unknown. |
| Search by object type | "Find tables and views whose names contain `user`." | Proxy or regular database | Narrow the search to tables, views, columns, or other object types. |
| Continue from search results | "Open the `orders` table found earlier and show columns and indexes." | Proxy or regular database | Search results can provide context for follow-up natural-language tasks. |

## SQL Execution

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| Run a read-only query | "Query the first 10 rows from `orders`." | Proxy or regular database | Use for sample data inspection or SQL result validation. |
| Limit returned rows | "Query the first 100 rows from `orders` and do not return more." | Proxy or regular database | Avoid returning too much data. |
| Preview side-effecting SQL | "Preview this change SQL without executing it." | Proxy or regular database | Review impact before execution. |
| Execute side-effecting SQL after confirmation | "Confirm and execute the SQL that was just previewed." | Proxy or regular database | Requires confirmation that side effects were reviewed. |

## ShardingSphere Rule Changes

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| Check existing rules | "Check whether `orders.phone` already has a masking rule." | Proxy only | Rule state comes from ShardingSphere-Proxy. |
| Plan a data encryption rule | "Plan reversible encryption for `orders.status` and preview it without execution." | Proxy only | Review the rule change, physical columns, and index suggestions. |
| Plan a data masking rule | "Plan phone-number masking for `orders.phone`, keep the first 3 and last 4 characters, and preview it without execution." | Proxy only | Review the masking algorithm, properties, and impact scope. |
| Adjust a rule plan | "Change the previous plan to use AES." | Proxy only | Preview again after changing the plan. |
| Apply a rule change | "Confirm and execute the previous rule change plan." | Proxy only | Side-effecting; review must be completed before execution. |
| Validate a rule change | "Validate whether the previous masking rule has taken effect." | Proxy only | Check rule state, metadata, and SQL executability. |

For detailed usage, see [Data Encryption](../features/encrypt/) and [Data Masking](../features/mask/).
