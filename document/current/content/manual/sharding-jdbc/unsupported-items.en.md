+++
pre = "<b>4.1.3. </b>"
toc = true
title = "Unsupported JDBC Items"
weight = 3
+++

## DataSource

- Do not support methods related to timeout

## Connection

- Do not support stored procedures, functions, cursor operation
- Do not Native SQL
- Do not support savepoint related operations
- Do not support Schema / Catalog operation
- Do not support Custom type mapping

## Statement & PreparedStatement

- Do not support statements that return multiple result sets (That is, stored procedures)
- Do not support using international characters

## ResultSet

- Do not support getting result set pointer position
- Do not support changing the position of the result pointer by none-next methods
- Do not support modifying the content of result set 
- Do not support using international characters
- Do not support getting Array

## New interface of JDBC 4.1

- Do not support new interface for JDBC 4.1

More details about unsupported items, please refer package `org.apache.shardingsphere.shardingjdbc.jdbc.unsupported`.
