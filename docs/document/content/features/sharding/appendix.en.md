+++
title = "Appendix with SQL operator"
weight = 3
+++

Unsupported SQL:

- CASE WHEN contains sub-query
- Logical table names are used in CASE WHEN( Please use an alias)
- INSERT INTO tbl_name (col1, col2, …) SELECT * FROM tbl_name WHERE col3 = ?（The SELECT clause does not support * and the built-in distributed primary key generator）
- REPLACE INTO tbl_name (col1, col2, …) SELECT * FROM tbl_name WHERE col3 = ?（The SELECT clause does not support * and the built-in distributed primary key generator）
- SELECT MAX(tbl_name.col1) FROM tbl_name (If the query column is a function expression, use the table alias instead of the table name）
