+++
pre = "<b>5.3. </b>"
title = "内核"
weight = 3
chapter = true
+++

## DatabaseType

| *SPI 名称*             | *详细说明*                |
| ---------------------- | ------------------------ |
| DatabaseType           | 支持的数据库类型           |

| *已知实现类*            | *详细说明*                |
| ---------------------- | ------------------------ |
| SQL92DatabaseType      | 遵循 SQL92 标准的数据库类型 |
| MySQLDatabaseType      | MySQL 数据库              |
| MariaDBDatabaseType    | MariaDB 数据库            |
| PostgreSQLDatabaseType | PostgreSQL 数据库         |
| OracleDatabaseType     | Oracle 数据库             |
| SQLServerDatabaseType  | SQLServer 数据库          |
| H2DatabaseType         | H2 数据库                 |

## RuleMetaDataLoader

| *SPI 名称*             | *详细说明*         |
| ---------------------- | ----------------- |
| RuleMetaDataLoader     | 用于元数据初始化    |

| *已知实现类*            | *详细说明*         |
| ---------------------- | ----------------- |
| ShardingMetaDataLoader | 用于分片元数据初始化 |
| EncryptMetaDataLoader  | 用于加密元数据初始化 |

## RuleMetaDataDecorator

| *SPI 名称*                | *详细说明*        |
| ------------------------ | ---------------- |
| RuleMetaDataDecorator    | 用于元数据更新     |

| *已知实现类*               | *详细说明*        |
| ------------------------- | ---------------- |
| ShardingMetaDataDecorator | 用于分片元数据更新 |
| EncryptMetaDataDecorator  | 用于加密元数据更新 |

## SQLRouter

| *SPI 名称*                | *详细说明*              |
| ------------------------- | --------------------- |
| SQLRouter                 | 用于处理路由结果        |

| *已知实现类*                        | *详细说明*                |
| ---------------------------------- | ------------------------ |
| ShardingSQLRouter                  | 用于处理分片路由结果       |
| PrimaryReplicaReplicationSQLRouter | 用于处理读写分离路由结果    |
| ConsensusReplicationSQLRouter      | 用于处理强一致多副本路由结果 |
| ShadowSQLRouter                    | 用于处理影子库路由结果      |

## SQLRewriteContextDecorator

| *SPI 名称*                         | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| SQLRewriteContextDecorator         | 用于处理 SQL 改写结果       |

| *已知实现类*                        | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| ShardingSQLRewriteContextDecorator | 用于处理分片 SQL 改写结果   |
| EncryptSQLRewriteContextDecorator  | 用于处理加密 SQL 改写结果   |
| ShadowSQLRewriteContextDecorator   | 用于处理影子库 SQL 改写结果 |

## ExecuteGroupDecorator

| *SPI 名称*                   | *详细说明*             |
| ---------------------------- | --------------------- |
| ExecuteGroupDecorator        | 用于修改数据节点分组结果 |

| *已知实现类*                               | *详细说明*                |
| ----------------------------------------- | ------------------------ |
| ConsensusReplicationExecuteGroupDecorator | 用于强一致多副本数据节点分组 |

## SQLExecutionHook

| *SPI 名称*                     | *详细说明*                        |
| ----------------------------- | --------------------------------- |
| SQLExecutionHook              | SQL执行过程监听器 |

| *已知实现类*                   | *详细说明*                         |
| ----------------------------- | --------------------------------- |
| TransactionalSQLExecutionHook | 基于事务的SQL执行过程监听器          |
| OpenTracingSQLExecutionHook   | 基于 OpenTracing 的SQL执行过程监听器 |

## ResultProcessEngine

| *SPI 名称*                   | *详细说明*           |
| ---------------------------- | ------------------- |
| ResultProcessEngine          | 用于处理结果集        |

| *已知实现类*                  | *详细说明*           |
| ---------------------------- | ------------------- |
| ShardingResultMergerEngine   | 用于处理分片结果集归并 |
| EncryptResultDecoratorEngine | 用于处理加密结果集改写 |
