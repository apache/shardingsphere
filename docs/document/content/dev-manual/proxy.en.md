+++
pre = "<b>5.6. </b>"
title = "Proxy"
weight = 6
chapter = true
+++

## DatabaseProtocolFrontendEngine

### Fully-qualified class name

[`org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/proxy/frontend/spi/src/main/java/org/apache/shardingsphere/proxy/frontend/spi/DatabaseProtocolFrontendEngine.java)

### Definition

Protocols for ShardingSphere-Proxy to parse and adapt for accessing databases.

### Implementation classes

| *Configuration Type* | *Description*                          | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                       |
| -------------------- | -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| MySQL                | Protocol implementation for MySQL      | [`org.apache.shardingsphere.proxy.frontend.mysql.MySQLFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/proxy/frontend/mysql/src/main/java/org/apache/shardingsphere/proxy/frontend/mysql/MySQLFrontendEngine.java)                          |
| PostgreSQL           | Protocol implementation for PostgreSQL | [`org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/proxy/frontend/postgresql/src/main/java/org/apache/shardingsphere/proxy/frontend/postgresql/PostgreSQLFrontendEngine.java) |
| openGauss            | Protocol implementation for openGauss  | [`org.apache.shardingsphere.proxy.frontend.opengauss.OpenGaussFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/proxy/frontend/opengauss/src/main/java/org/apache/shardingsphere/proxy/frontend/opengauss/OpenGaussFrontendEngine.java)      |

## AuthorityProvideAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/api/src/main/java/org/apache/shardingsphere/authority/spi/AuthorityProviderAlgorithm.java)

### Definition

Loading logic for user permission.

### Implementation classes

| *Configuration Type* | *Description*                                    | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                                                    |
| -------------------- | ------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ALL_PERMITTED        | Grant all permissions by default (no forensics)  | [`org.apache.shardingsphere.authority.provider.simple.AllPermittedPrivilegesProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/provider/simple/AllPermittedPrivilegesProviderAlgorithm.java)               |
| DATABASE_PERMITTED   | Permissions configured by user-database-mappings | [`org.apache.shardingsphere.authority.provider.database.DatabasePermittedPrivilegesProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/provider/database/DatabasePermittedPrivilegesProviderAlgorithm.java) |
