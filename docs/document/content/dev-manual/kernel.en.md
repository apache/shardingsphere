+++
pre = "<b>6.3. </b>"
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
| OpenGaussDatabaseType  | OpenGauss database      |

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
| OpenGaussTableMetaDataLoader | Use OpenGauss dialect to load meta data   |

## DataSourceCreator

| *SPI Name*               | *Description*               |
| ------------------------ | --------------------------- |
| DataSourceCreator        | Data source creator         |

| *Implementation Class*   | *Description*               |
| ------------------------ | --------------------------- |
| DefaultDataSourceCreator | Default data source creator |
| HikariDataSourceCreator  | Hikari data source creator  |

## DataSourceKiller

| *SPI Name*              | *Description*                |
| ----------------------- | ---------------------------- |
| DataSourceKiller        | Default data source killer   |

| *Implementation Class*  | *Description*                |
| ----------------------- | ---------------------------- |
| DefaultDataSourceKiller | Default data source killer   |
| HikariDataSourceKiller  | Hikari data source killer    |

## SQLRouter

| *SPI Name*                          | *Description*                                         |
| ----------------------------------- | ----------------------------------------------------- |
| SQLRouter                           | Used to process routing results                       |

| *Implementation Class*              | *Description*                                         |
| ----------------------------------- | ----------------------------------------------------- |
| ReadwriteSplittingSQLRouter         | Used to process read-write separation routing results |
| DatabaseDiscoverySQLRouter          | Used to process database discovery routing results    |
| SingleTableSQLRouter                | Used to process single-table routing results          |
| ShardingSQLRouter                   | Used to process sharding routing results              |
| ShadowSQLRouter                     | Used to process shadow database routing results       |

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

| *SPI Name*                    | *Description*                     |
| ----------------------------- | --------------------------------- |
| SQLExecutionHook              | Hook of SQL execution             |

| *Implementation Class*        | *Description*                     |
| ----------------------------- | --------------------------------- |
| TransactionalSQLExecutionHook | Transaction hook of SQL execution |

## ResultProcessEngine

| *SPI Name*                   | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ResultProcessEngine          | Used by merge engine to process result set            |

| *Implementation Class*       | *Description*                                         |
| ---------------------------- | ----------------------------------------------------- |
| ShardingResultMergerEngine   | Used by merge engine to process sharding result set   |
| EncryptResultDecoratorEngine | Used by merge engine to process encryption result set |

## StoragePrivilegeHandler

| *SPI Name*                 | *Description*                                        |
| -------------------------- | ---------------------------------------------------- |
| StoragePrivilegeHandler    | Use SQL dialect to process privilege metadata        |

| *Implementation Class*     | *Description*                                        |
| -------------------------- | ---------------------------------------------------- |
| PostgreSQLPrivilegeHandler | Use PostgreSQL dialect to process privilege metadata |
| SQLServerPrivilegeHandler  | Use SQLServer dialect to process privilege metadata  |
| OraclePrivilegeHandler     | Use Oracle dialect to process privilege metadata     |
| MySQLPrivilegeHandler      | Use MySQL dialect to process privilege metadata      |
