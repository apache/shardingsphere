+++
pre = "<b>6.6. </b>"
title = "Proxy"
weight = 6
chapter = true
+++

## SPI Interface

### DatabaseProtocolFrontendEngine

Fully-qualified class name: [`org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine`](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-spi/src/main/java/org/apache/shardingsphere/proxy/frontend/spi/DatabaseProtocolFrontendEngine.java)

| *SPI Name*                       | *Description*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | Protocols for ShardingSphere-Proxy to parse and adapt for accessing databases |

### AuthorityProvideAlgorithm

Fully-qualified class name: [`org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-api/src/main/java/org/apache/shardingsphere/authority/spi/AuthorityProviderAlgorithm.java)

| *SPI Name*                       | *Description*                    |
| ------------------------------- | ---------------------------- |
| AuthorityProviderAlgorithm      | Loading logic for user permission|


## Example

### DatabaseProtocolFrontendEngine

| *Known implementation class*               | *Description*                      |
| ------------------------ | ---------------------------- |
| [MySQLFrontendEngine](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-mysql/src/main/java/org/apache/shardingsphere/proxy/frontend/mysql/MySQLFrontendEngine.java)      | implementation based on database protocols of MySQL |
| [PostgreSQLFrontendEngine](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-postgresql/src/main/java/org/apache/shardingsphere/proxy/frontend/postgresql/PostgreSQLFrontendEngine.java) |  implementation based on database protocols of PostgreSQL |
| [OpenGaussFrontendEngine](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-opengauss/src/main/java/org/apache/shardingsphere/proxy/frontend/opengauss/OpenGaussFrontendEngine.java)  |  implementation based on database protocols of openGauss|

### AuthorityProvideAlgorithm

| *Known implementation class*                                          | *Type*            | *Description*                                                                          |
|-----------------------------------------------------| ----------------  |----------------------------------------------------------------------------------- |
| [AllPermittedPrivilegesProviderAlgorithm](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/simple/AllPermittedPrivilegesProviderAlgorithm.java)             | ALL_PERMITTED     | Grant all permissions by default (no forensics), no interaction with the actual database |
| [DatabasePermittedPrivilegesProviderAlgorithm](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/database/DatabasePermittedPrivilegesProviderAlgorithm.java)          | DATABASE_PERMITTED| Permissions configured by user-database-mappings |
