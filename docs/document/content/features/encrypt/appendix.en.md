+++
title = "Appendix with SQL operator"
weight = 3
+++

Unsupported SQL：

- The case-insensitive queries are not supported by encrypted fields.
- Comparison operations are not supported for encrypted fields, such as GREATER THAN, LESS THAN, ORDER BY, BETWEEN.
- Calculation operations are not supported for encrypted fields, such as AVG, SUM, and computation expressions.

Other:

- You should keep encrypt column, assisted column and like column in encrypt rule keep same capitalization with column in database. 