+++
pre = "<b>4.1.3. </b>"
title = "Unsupported Items"
weight = 3
+++

## DataSource Interface

* Do not support timeout related operations

## Connection Interface

* Do not support operations of stored procedure, function and cursor
* Do not support native SQL
* Do not support savepoint related operations
* Do not support Schema/Catalog operation
* Do not support self-defined type mapping

## Statement and PreparedStatement Interface

* Do not support statements that return multiple result sets (stored procedures, multiple pieces of non-SELECT data)
* Do not support the operation of international characters

## ResultSet Interface

* Do not support getting result set pointer position
* Do not support changing result pointer position through none-next method
* Do not support revising the content of result set
* Do not support acquiring international characters
* Do not support getting Array

## JDBC 4.1

* Do not support new functions of JDBC 4.1 interface

For all the unsupported methods, please read `org.apache.shardingsphere.driver.jdbc.unsupported` package.
