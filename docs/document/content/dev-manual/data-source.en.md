+++
pre = "<b>6.4. </b>"
title = "DataSource"
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

## DataSourcePoolCreator

| *SPI Name*                   | *Description*                    |
| ---------------------------- | -------------------------------- |
| DataSourcePoolCreator        | Data source pool creator         |

| *Implementation Class*       | *Description*                    |
| ---------------------------- | -------------------------------- |
| DefaultDataSourcePoolCreator | Default data source pool creator |
| HikariDataSourcePoolCreator  | Hikari data source pool creator  |

## DataSourcePoolDestroyer

| *SPI Name*                     | *Description*                      |
| ------------------------------ | ---------------------------------- |
| DataSourcePoolDestroyer        | Data source pool destroyer         |

| *Implementation Class*         | *Description*                      |
| ------------------------------ | ---------------------------------- |
| DefaultDataSourcePoolDestroyer | Default data source pool destroyer |
| HikariDataSourcePoolDestroyer  | Hikari data source pool destroyer  |
