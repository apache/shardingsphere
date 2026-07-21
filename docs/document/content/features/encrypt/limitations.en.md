+++
title = "Limitations"
weight = 2
+++

- You need to process the original data on stocks in the database by yourself.
- The `like` query supports %, _, but currently does not support escape.
- Case insensitive queries are not supported for the encrypted fields.
- Comparison operations are not supported for encrypted fields, such as `GREATER THAN`, `LESS THAN`, `ORDER BY`, `BETWEEN`.
- Calculation operations are not supported for encrypted fields, such as `AVG`, `SUM`, and computation expressions.
- Not support simultaneous execution of multiple SQL statements separated by `;`.
- When projection subquery contains encrypt column, you must use alias.

## SQL Server OPENQUERY encryption

Encrypt rewrite for `OPENQUERY` only supports a narrow pass-through shape:

```sql
UPDATE OPENQUERY (linked_server, 'SELECT <columns> FROM [<schema>.]<table> [WHERE ...]')
SET <encrypt_column> = <literal_or_parameter>
```

The following are not supported:

- `SELECT` list items that are string literals or expressions.
- Identifiers that contain spaces inside brackets, such as `[Human Resources]`.
- Three-part table names, such as `db.schema.table`.
- Comma-separated table sources.
- `JOIN`, `CROSS APPLY`, `OUTER APPLY`.
- `UNION`, `UNION ALL`, `EXCEPT`, `INTERSECT`.
- Predicates after `WHERE` that reference encrypted columns.
- Assignment expressions other than literals or parameter markers, such as `SET col = UPPER('x')`.
- Physical column names that contain `]`.
