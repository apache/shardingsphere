+++
pre = "<b>6.6. </b>"
title = "代理端"
weight = 6
chapter = true
+++

## DatabaseProtocolFrontendEngine

| *SPI 名称*                       | *详细说明*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | 用于 ShardingSphere-Proxy 解析与适配访问数据库的协议 |

| *已知实现类*               | *详细说明*                      |
| ------------------------ | ---------------------------- |
| MySQLFrontendEngine      | 基于 MySQL 的数据库协议实现      |
| PostgreSQLFrontendEngine | 基于 PostgreSQL 的数据库协议实现 |
| OpenGaussFrontendEngine  | 基于 openGauss 的数据库协议实现   |

## AuthorityProviderAlgorithm

| *SPI 名称*                       | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| AuthorityProviderAlgorithm      | 用户权限加载逻辑                |

| *已知实现类*                                          | *Type*            | *详细说明*                                                                          |
|-----------------------------------------------------| ----------------  |----------------------------------------------------------------------------------- |
| NativeAuthorityProviderAlgorithm（已弃用）            | NATIVE            | 基于后端数据库存取 server.yaml 中配置的权限信息。如果用户不存在，则自动创建用户并默认赋予最高权限  |
| AllPermittedPrivilegesProviderAlgorithm             | ALL_PERMITTED     | 默认授予所有权限（不鉴权），不会与实际数据库交互                                            |
| SchemaPermittedPrivilegesProviderAlgorithm          | DATABASE_PERMITTED| 通过属性 user-database-mappings 配置的权限                                               |
