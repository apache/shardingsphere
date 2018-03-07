+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "Use Limits"
weight = 5
prev = "/01-start/features"
next = "/01-start/sql-supported"

+++

## The list of unsupported items in JDBC

Sharding-JDBC currently supports common JDBC methods.

### The DataSource interface

- Do not support methods related to timeout.

### The Connection interface

- Does not support stored procedures, functions, cursor operation
- Does not Native SQL
- Does not support savepoint related operations
- Does not support Schema / Catalog operation
- Does not support Custom type mapping

### The interface of Statement and PreparedStatement

- Does not support statements that return multiple result sets (That is, stored procedures)
- Does not support using international characters

### The ResultSet interface

- Does not support getting result set pointer position
- Does not support changing the position of the result pointer by none-next methods
- Does not support modifying the content of result set 
- Does not support using international characters
- Does not support getting Array


### The interface of JDBC 4.1

- Does not support new interface features in JDBC 4.1.

Learn more about the unsupported items, please refer to io.shardingjdbc.core.jdbc.unsupported.

## The limits of SQL statement

###  Support some kinds of subqueries
###  Does not support HAVING
###  Does not support OR，UNION 和 UNION ALL
###  Does not support special INSERT
Each INSERT statement can only contain one row of data. And does not support the statement that multiple rows of data are contained in INSERT...VALUES.
###  Does not support DISTINCT Aggregation
###  Does not support dual virtual table
###  Does not support SELECT LAST_INSERT_ID()
###  Does not support CASE WHEN
