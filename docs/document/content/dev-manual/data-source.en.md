+++
pre = "<b>6.4. </b>"
title = "Kernel"
weight = 4
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
