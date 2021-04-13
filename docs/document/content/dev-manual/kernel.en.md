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

## DialectTableMetaDataLoader

| *SPI Name*                   | *Description*                             |
| ---------------------------- | ----------------------------------------- |
| DialectTableMetaDataLoader   | Use SQL dialect to load meta data rapidly |

| *Implementation Class*       | *Description*                             |
| ---------------------------- | ----------------------------------------- |
| MySQLTableMetaDataLoader     | Use MySQL dialect to load meta data       |
| OracleTableMetaDataLoader    | Use Oracle dialect to load meta data      |
| PostgreSQLTableMetaDataLoader| Use PostgreSQL dialect to load meta data  |
| SQLServerTableMetaDataLoader | Use SQLServer dialect to load meta data   |
| H2TableMetaDataLoader        | Use H2 dialect to load meta data          |

## SQLRouter

| *SPI Name*                    | *Description*                                         |
| ----------------------------- | ----------------------------------------------------- |
| SQLRouter                     | Used to process routing results                       |

| *Implementation Class*        | *Description*                                         |
| ----------------------------- | ----------------------------------------------------- |
| ShardingSQLRouter             | Used to process sharding routing results              |
| ReplicaQuerySQLRouter         | Used to process replica query routing results         |
| ShadowRouteSQLRouter          | Used to process shadow database routing results       |

## SQLRewriteContextDecorator

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| SQLRewriteContextDecorator         | Used to process SQL rewrite results            |

| *SPI Name*                         | *Description*                                  |
| ---------------------------------- | ---------------------------------------------- |
| ShardingSQLRewriteContextDecorator | Used to process sharding SQL rewrite results   |
| EncryptSQLRewriteContextDecorator  | Used to process encryption SQL rewrite results |
| ShadowSQLRewriteContextDecorator   | Used to process shadow SQL rewrite results     |

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

## StoragePrivilegeLoader

| *SPI Name*                | *Description*                                       |
| ------------------------- | ---------------------------------------------------- |
| StoragePrivilegeLoader    | Use SQL dialect to load privilege metadata           |

| *Implementation Class*    | *Description*                                        |
| ------------------------- | ---------------------------------------------------- |
| MySQLPrivilegeLoader      | Use MySQL dialect to load privilege metadata         |
| OraclePrivilegeLoader     | Use Oracle dialect to load privilege metadata        |
| PostgreSQLPrivilegeLoader | Use PostgreSQL dialect to load privilege metadata    |