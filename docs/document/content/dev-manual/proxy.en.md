+++
pre = "<b>6.6. </b>"
title = "Proxy"
weight = 6
chapter = true
+++

## DatabaseProtocolFrontendEngine

| *SPI Name*                       | *Description*                                                                   |
| -------------------------------- | ------------------------------------------------------------------------------- |
| DatabaseProtocolFrontendEngine   | Regulate parse and adapter protocol of database access for ShardingSphere-Proxy |

| *Implementation Class*   | *Description*                        |
| ------------------------ | ------------------------------------ |
| MySQLFrontendEngine      | Base on MySQL database protocol      |
| PostgreSQLFrontendEngine | Base on PostgreSQL database protocol |
| OpenGaussFrontendEngine  | Base on openGauss database protocol  |

## AuthorityProviderAlgorithm

| *SPI Name*                       | *Description*                  |
| -------------------------------  | ------------------------------ |
| AuthorityProviderAlgorithm       | User authority loading logic   |

| *Implementation Class*                       | *Type*           | *Description*                                                                                                         |
|----------------------------------------------| ---------------- | --------------------------------------------------------------------------------------------------------------------- |
| AllPermittedProviderAlgorithm      | ALL_PERMITTED    | All privileges granted to user by default (No authentication). Will not interact with the actual database             |
| DatabasePermittedProviderAlgorithm | DATABASE_PERMITTED | Permissions configured through the attribute user-database-mappings                                                     |
