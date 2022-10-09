+++
pre = "<b>5.6. </b>"
title = "代理端"
weight = 6
chapter = true
+++

## DatabaseProtocolFrontendEngine

### 全限定类名

[`org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-spi/src/main/java/org/apache/shardingsphere/proxy/frontend/spi/DatabaseProtocolFrontendEngine.java)

### 定义

用于 ShardingSphere-Proxy 解析与适配访问数据库的协议

### 已知实现

| *配置标识*   | *详细说明*          | *全限定类名*                                                                                                                                                                                                                                                                                                                            |
| ---------- | ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| MySQL      | MySQL 协议实现      | [`org.apache.shardingsphere.proxy.frontend.mysql.MySQLFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-mysql/src/main/java/org/apache/shardingsphere/proxy/frontend/mysql/MySQLFrontendEngine.java)                          |
| PostgreSQL | PostgreSQL 协议实现 | [`org.apache.shardingsphere.proxy.frontend.postgresql.PostgreSQLFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-postgresql/src/main/java/org/apache/shardingsphere/proxy/frontend/postgresql/PostgreSQLFrontendEngine.java) |
| openGauss  | openGauss 协议实现  | [`org.apache.shardingsphere.proxy.frontend.opengauss.OpenGaussFrontendEngine`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-opengauss/src/main/java/org/apache/shardingsphere/proxy/frontend/opengauss/OpenGaussFrontendEngine.java)      |

## AuthorityProvideAlgorithm

### 全限定类名

[`org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-api/src/main/java/org/apache/shardingsphere/authority/spi/AuthorityProviderAlgorithm.java)

### 定义

用户权限加载逻辑

### 已知实现

| *配置标识*          | *详细说明*                                | *全限定类名*                                                                                                                                                                                                                                                                                                                                                         |
| ------------------ | --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ALL_PERMITTED      | 默认授予所有权限（不鉴权）                   | [`org.apache.shardingsphere.authority.provider.simple.AllPermittedPrivilegesProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/simple/AllPermittedPrivilegesProviderAlgorithm.java)               |
| DATABASE_PERMITTED | 通过属性 user-database-mappings 配置的权限 | [`org.apache.shardingsphere.authority.provider.database.DatabasePermittedPrivilegesProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/provider/database/DatabasePermittedPrivilegesProviderAlgorithm.java) |
