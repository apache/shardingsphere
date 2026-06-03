+++
title = "Capability Catalog"
weight = 2
+++

This page describes the database tasks that users can complete through natural language, and the usage boundaries when connecting to ShardingSphere-Proxy or using a direct database connection.

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

### Direct Database Connection

A direct database connection means that ShardingSphere-MCP connects to a user-provided database service such as MySQL or PostgreSQL without going through ShardingSphere-Proxy.
Use this mode when ShardingSphere-MCP acts as a controlled access entry for an existing database, including metadata inspection, object search, and restricted SQL execution.

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
| List accessible databases | "List the databases that can be accessed." | Proxy or direct database connection | Confirm that database names match the configuration. |
| Inspect schemas or namespaces | "Show schemas in `<database-name>`." | Proxy or direct database connection | For multi-schema databases, confirm the target schema first. |
| Inspect tables or views | "List tables and views in `<schema-name>`." | Proxy or direct database connection | Proxy connections show logical objects. |
| Inspect columns | "Show columns and column types for `<table-name>`." | Proxy or direct database connection | Column types follow metadata visible from the connection target. |
| Inspect indexes | "Show indexes for `<table-name>`." | Proxy or direct database connection | With Proxy connections, index information may differ from the full physical database structure. |
| Inspect sequences | "List sequences in `<schema-name>`." | Proxy or direct database connection | Available only when the connection target exposes sequence metadata. |

## Metadata Search

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| Search objects by name | "Find tables whose names contain `order`." | Proxy or direct database connection | Useful when the full object name is unknown. |
| Search by object type | "Find tables and views whose names contain `user`." | Proxy or direct database connection | Narrow the search to tables, views, columns, or other object types. |
| Continue from search results | "Open the `orders` table found earlier and show columns and indexes." | Proxy or direct database connection | Search results can provide context for follow-up natural-language tasks. |

## Queries and Ordinary SQL Changes

| Task | Natural language example | Connection target | User focus |
| --- | --- | --- | --- |
| Run a query | "Query the first 10 rows from `orders`." | Proxy or direct database connection | Use for sample data inspection or query result validation. |
| Limit returned rows | "Query the first 100 rows from `orders` and do not return more." | Proxy or direct database connection | Avoid returning too much data. |
| Preview an ordinary SQL change | "Preview this SQL change without executing it." | Proxy or direct database connection | Review impact before execution. |
| Confirm a previewed ordinary SQL change | "Confirm and execute the SQL change that was just previewed." | Proxy or direct database connection | Requires confirmation that side effects were reviewed. |

## Runtime Protection Limits

- When the returned row count is not specified, a query returns at most 100 rows by default.
- A single query can request at most 5000 rows. If the result is truncated, narrow the predicate, reduce the projection, or request fewer rows.
- A query timeout can be requested by the task, up to 300000 milliseconds. When omitted, the Server default behavior is used.
- Each MCP session has a tool-call quota. When the quota is exhausted, close the current session and create a new MCP session.

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
