+++
pre = "<b>6.3. </b>"
title = "内核"
weight = 3
chapter = true
+++

## SQLRouter

| *SPI 名称*                           | *详细说明*                 |
| ----------------------------------- | ------------------------- |
| SQLRouter                           | 用于处理路由结果             |

| *已知实现类*                          | *详细说明*                 |
| ----------------------------------- | ------------------------- |
| ReadwriteSplittingSQLRouter         | 用于处理读写分离路由结果       |
| DatabaseDiscoverySQLRouter          | 用于处理数据库发现路由结果      |
| SingleTableSQLRouter                | 用于处理单表路由结果           |
| ShardingSQLRouter                   | 用于处理分片路由结果           |
| ShadowSQLRouter                     | 用于处理影子库路由结果         |

## SQLRewriteContextDecorator

| *SPI 名称*                          | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| SQLRewriteContextDecorator         | 用于处理 SQL 改写结果        |

| *已知实现类*                         | *详细说明*                 |
| ---------------------------------- | ------------------------ |
| ShardingSQLRewriteContextDecorator | 用于处理分片 SQL 改写结果   |
| EncryptSQLRewriteContextDecorator  | 用于处理加密 SQL 改写结果   |
| ShadowSQLRewriteContextDecorator   | 用于处理影子库 SQL 改写结果 |

## SQLExecutionHook

| *SPI 名称*                     | *详细说明*              |
| ----------------------------- | ---------------------- |
| SQLExecutionHook              | SQL执行过程监听器         |

| *已知实现类*                    | *详细说明*               |
| ----------------------------- | ----------------------- |
| TransactionalSQLExecutionHook | 基于事务的SQL执行过程监听器 |

## ResultProcessEngine

| *SPI 名称*                    | *详细说明*           |
| ---------------------------- | ------------------- |
| ResultProcessEngine          | 用于处理结果集        |

| *已知实现类*                   | *详细说明*           |
| ---------------------------- | ------------------- |
| ShardingResultMergerEngine   | 用于处理分片结果集归并 |
| EncryptResultDecoratorEngine | 用于处理加密结果集改写 |

## StoragePrivilegeHandler

| *SPI 名称*                  | *详细说明*                      |
| -------------------------- | ------------------------------ |
| StoragePrivilegeHandler    | 使用数据库方言处理权限信息          |

| *已知实现类*                 | *详细说明*                      |
| -------------------------- | ------------------------------ |
| PostgreSQLPrivilegeHandler | 使用 PostgreSQL 方言处理权限信息   |
| SQLServerPrivilegeHandler  | 使用 SQLServer 方言处理权限信息    |
| OraclePrivilegeHandler     | 使用 Oracle 方言处理权限信息       |
| MySQLPrivilegeHandler      | 使用 MySQL 方言处理权限信息        |
