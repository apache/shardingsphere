+++
pre = "<b>5.3. </b>"
title = "Kernel"
weight = 3
chapter = true
+++

## DatabaseType

| *SPI Name*             | *Description*           |
| ---------------------- | ----------------------- |
| DatabaseType           | Supported database type |

| *Implementation Class* | *Description*           |
| ---------------------- | ----------------------- |
| SQL92DatabaseType      | SQL92 database type     |
| MySQLDatabaseType      | MySQL database          |
| MariaDBDatabaseType    | MariaDB database        |
| PostgreSQLDatabaseType | PostgreSQL database     |
| OracleDatabaseType     | Oracle database         |
| SQLServerDatabaseType  | SQLServer database      |
| H2DatabaseType         | H2 database             |

## RuleMetaDataLoader

| *SPI Name*             | *Description*                           |
| ---------------------- | --------------------------------------- |
| RuleMetaDataLoader     | Used to initialize meta data            |

| *Implementation Class* | *Description*                           |
| ---------------------- | --------------------------------------- |
| ShardingMetaDataLoader | Used to initialize sharding meta data   |
| EncryptMetaDataLoader  | Used to initialize encryption meta data |

## RuleMetaDataDecorator

| *SPI Name*                | *Description*                        |
| ------------------------- | ------------------------------------ |
| RuleMetaDataDecorator     | Used to update meta data             |

| *Implementation Class*    | *Description*                        |
| ------------------------- | ------------------------------------ |
| ShardingMetaDataDecorator | Used to update sharding meta data    |
| EncryptMetaDataDecorator  | Used to update encryption meta data  |

## RouteDecorator

| *SPI Name*                | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| RouteDecorator            | Used to process routing results                 |

| *Implementation Class*    | *Description*                                   |
| ------------------------- | ----------------------------------------------- |
| ShardingRouteDecorator    | Used to process sharding routing results        |
| MasterSlaveRouteDecorator | Used to process master-slave routing results    |
| ReplicaRouteDecorator     | Used to process multi replica routing results   |
| ShadowRouteDecorator      | Used to process shadow database routing results |

## SQLRewriteContextDecorator

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| SQLRewriteContextDecorator         | Used to process SQL rewrite results            |

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| ShardingSQLRewriteContextDecorator | Used to process sharding SQL rewrite results   |
| EncryptSQLRewriteContextDecorator  | Used to process encryption SQL rewrite results |
| ShadowSQLRewriteContextDecorator   | Used to process shadow SQL rewrite results     |

## ExecuteGroupDecorator

| *SPI Name*                   | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ExecuteGroupDecorator        | Used by update data nodes group result |

| *Implementation Class*       | *Description*                          |
| ---------------------------- | -------------------------------------- |
| ReplicaExecuteGroupDecorator | Used by multi replica data nodes group |

## SQLExecutionHook

| *SPI Name*                    | *Description*                      |
| ----------------------------- | ---------------------------------- |
| SQLExecutionHook              | Hook of SQL execution              |

| *Implementation Class*        | *Description*                      |
| ----------------------------- | ---------------------------------- |
| TransactionalSQLExecutionHook | Transaction hook of SQL execution  |
| OpenTracingSQLExecutionHook   | Open tracing hook of SQL execution |

## ResultProcessEngine

| *SPI Name*                   | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ResultProcessEngine          | Used by merge engine to process result set            |

| *Implementation Class*       | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ShardingResultMergerEngine   | Used by merge engine to process sharding result set   |
| EncryptResultDecoratorEngine | Used by merge engine to process encryption result set |
