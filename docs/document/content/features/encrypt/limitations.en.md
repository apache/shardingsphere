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
