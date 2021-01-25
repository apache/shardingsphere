+++
pre = "<b>5.11. </b>"
title = "Proxy"
weight = 11
chapter = true
+++

## DatabaseProtocolFrontendEngine

| *SPI 名称*                       | *详细说明*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | 用于ShardingSphere-Proxy解析与适配访问数据库的协议 |

| *已知实现类*              | *详细说明*                                      |
| ------------------------ | ---------------------------------------------- |
| MySQLFrontendEngine      | 基于 MySQL 的数据库协议实现                      |
| PostgreSQLFrontendEngine | 基于 PostgreSQL 的SQL 解析器实现                 |

## JDBCDriverURLRecognizer

| *SPI 名称*               | *详细说明*                           |
| ----------------------- | ------------------------------------ |
| JDBCDriverURLRecognizer | 使用 JDBC 驱动执行 SQL                |

| *已知实现类*             | *详细说明*                           |
| ----------------------- | ----------------------------------- |
| MySQLRecognizer         |  使用 MySQL 的 JDBC 驱动执行 SQL      |
| PostgreSQLRecognizer    |  使用 PostgreSQL 的 JDBC 驱动执行 SQL |
| OracleRecognizer        |  使用 Oracle 的 JDBC 驱动执行 SQL     |
| SQLServerRecognizer     |  使用 SQLServer 的 JDBC 驱动执行 SQL  |
| H2Recognizer            |  使用 H2 的 JDBC 驱动执行 SQL         |
