+++
toc = true
title = "JDBC"
weight = 1
prev = "/02-sharding/usage-standard/"
next = "/02-sharding/usage-standard/SQL/"
+++

## The list of supported items in JDBC

We support all the items except the items in the below list.

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
