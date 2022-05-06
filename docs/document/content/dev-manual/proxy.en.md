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

| *Implementation Class*   | *Description*                                                                   |
| ------------------------ | ------------------------------------------------------------------------------- |
| MySQLFrontendEngine      | Base on MySQL database protocol                                                 |
| PostgreSQLFrontendEngine | Base on PostgreSQL database protocol                                            |
| OpenGaussFrontendEngine | Base on openGauss database protocol                                            |

## AuthorityProvideAlgorithm

| *SPI Name*                       | *Description*                 |
| ------------------------------- | ------------------------------ |
| AuthorityProvideAlgorithm       | User authority loading logic   |

| *Implementation Class*                              | *Type*                      | *Description*                                                                                                          |
|-----------------------------------------------------| --------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| NativeAuthorityProviderAlgorithm (Deprecated)       | NATIVE                      | Persist user authority defined in server.yaml into the backend database. An admin user will be created if not existed. |
| AllPrivilegesPermittedAuthorityProviderAlgorithm    | ALL_PRIVILEGES_PERMITTED    | All privileges granted to user by default (No authentication). Will not interact with the actual database.             |
| SchemaPrivilegesPermittedAuthorityProviderAlgorithm | SCHEMA_PRIVILEGES_PERMITTED | Permissions configured through the attribute user-schema-mappings.                                                     |
