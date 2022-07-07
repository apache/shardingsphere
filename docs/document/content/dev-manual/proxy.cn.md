+++
pre = "<b>6.6. </b>"
title = "代理端"
weight = 6
chapter = true
+++

## SPI 接口

### DatabaseProtocolFrontendEngine

全限定类名：[`org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine`](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-spi/src/main/java/org/apache/shardingsphere/proxy/frontend/spi/DatabaseProtocolFrontendEngine.java)

| *SPI 名称*                       | *详细说明*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | 用于 ShardingSphere-Proxy 解析与适配访问数据库的协议 |

### AuthorityProvideAlgorithm

全限定类名：[`org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-api/src/main/java/org/apache/shardingsphere/authority/spi/AuthorityProviderAlgorithm.java)

| *SPI 名称*                       | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| AuthorityProviderAlgorithm      | 用户权限加载逻辑                |


## 示例

### DatabaseProtocolFrontendEngine

| *已知实现类*               | *详细说明*                      |
| ------------------------ | ---------------------------- |
| [MySQLFrontendEngine](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-mysql/src/main/java/org/apache/shardingsphere/proxy/frontend/mysql/MySQLFrontendEngine.java)      | 基于 MySQL 的数据库协议实现      |
| [PostgreSQLFrontendEngine](https://github.com/apache/shardingsphere/blob/aac0d3026e00575114701be603ec189a02a45747/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-postgresql/src/main/java/org/apache/shardingsphere/proxy/frontend/postgresql/PostgreSQLFrontendEngine.java) | 基于 PostgreSQL 的数据库协议实现 |
| [OpenGaussFrontendEngine](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-opengauss/src/main/java/org/apache/shardingsphere/proxy/frontend/opengauss/OpenGaussFrontendEngine.java)  | 基于 openGauss 的数据库协议实现   |

### AuthorityProvideAlgorithm

| *已知实现类*                                          | *Type*            | *详细说明*                                                                          |
|-----------------------------------------------------| ----------------  |----------------------------------------------------------------------------------- |
| [AllPermittedPrivilegesProviderAlgorithm](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/simple/AllPermittedPrivilegesProviderAlgorithm.java)             | ALL_PERMITTED     | 默认授予所有权限（不鉴权），不会与实际数据库交互                                            |
| [DatabasePermittedPrivilegesProviderAlgorithm](https://github.com/apache/shardingsphere/blob/dec5581af372e1e7daa487800867265ef99bb07c/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/database/DatabasePermittedPrivilegesProviderAlgorithm.java)          | DATABASE_PERMITTED| 通过属性 user-database-mappings 配置的权限                                               |
