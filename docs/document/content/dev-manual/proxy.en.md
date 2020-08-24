+++
pre = "<b>5.10. </b>"
title = "Proxy"
weight = 10
chapter = true
+++

## DatabaseProtocolFrontendEngine

| *SPI Name*                       | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| DatabaseProtocolFrontendEngine   | Regulate parse and adapter protocol of database access for ShardingSphere-Proxy |

| *Implementation Class*           | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| MySQLProtocolFrontendEngine      | Base on MySQL database protocol                                                 |
| PostgreSQLProtocolFrontendEngine | Base on postgreSQL database protocol                                            |

## JDBCDriverURLRecognizer

| *SPI Name*              | *Description*                              |
| ----------------------- | ------------------------------------------ |
| JDBCDriverURLRecognizer | Use JDBC driver to execute SQL             |

| *Implementation Class*  | *Description*                              |
| ----------------------- | ------------------------------------------ |
| MySQLRecognizer         |  Use MySQL JDBC driver to execute SQL      |
| PostgreSQLRecognizer    |  Use PostgreSQL JDBC driver to execute SQL |
| OracleRecognizer        |  Use Oracle JDBC driver to execute SQL     |
| SQLServerRecognizer     |  Use SQLServer JDBC driver to execute SQL  |
| H2Recognizer            |  Use H2 JDBC driver to execute SQL         |
