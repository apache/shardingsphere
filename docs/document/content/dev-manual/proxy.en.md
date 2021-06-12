+++
pre = "<b>5.11. </b>"
title = "Proxy"
weight = 11
chapter = true
+++

## DatabaseProtocolFrontendEngine

| *SPI Name*                       | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| DatabaseProtocolFrontendEngine   | Regulate parse and adapter protocol of database access for ShardingSphere-Proxy |

| *Implementation Class*   | *Description*                                                                   |
| ------------------------ | ------------------------------------------------------------------------------- |
| MySQLFrontendEngine      | Base on MySQL database protocol                                                 |
| PostgreSQLFrontendEngine | Base on PostgreSQL database protocol                                            |

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

## AuthorityProvideAlgorithm

| *SPI Name*                       | *Description*                 |
| ------------------------------- | ------------------------------ |
| AuthorityProvideAlgorithm       | User authority loading logic   |

| *Implementation Class*                             | *Type*                   | *Description*                                                                                                          |
| -------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------- |
| NativeAuthorityProviderAlgorithm                   | NATIVE                   | Persist user authority defined in server.yaml into the backend database. An admin user will be created if not existed. |
| AllPrivilegesPermittedAuthorityProviderAlgorithm   | ALL_PRIVILEGES_PERMITTED | All privileges granted to user by default (No authentication). Will not interact with the actual database.             |
