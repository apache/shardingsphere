+++
pre = "<b>6.4. </b>"
title = "DataSource"
weight = 4
chapter = true
+++

## SPI Interface

| SPI Name                     | Description                                 |
| ---------------------------- | ------------------------------------------- |
| DatabaseType                 | Supported database types                    |
| DialectTableMetaDataLoader   | Use SQL dialect to load meta data rapidly   |
| DataSourcePoolMetaData       | Data source connection pool metadata        |
| DataSourcePoolActiveDetector | Data source connection pool active detector |

## Sample

### DatabaseType

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

### DialectTableMetaDataLoader

| *Implementation Class*       | *Description*                             |
| ---------------------------- | ----------------------------------------- |
| MySQLTableMetaDataLoader     | Use MySQL dialect to load meta data       |
| OracleTableMetaDataLoader    | Use Oracle dialect to load meta data      |
| PostgreSQLTableMetaDataLoader| Use PostgreSQL dialect to load meta data  |
| SQLServerTableMetaDataLoader | Use SQLServer dialect to load meta data   |
| H2TableMetaDataLoader        | Use H2 dialect to load meta data          |
| OpenGaussTableMetaDataLoader | Use OpenGauss dialect to load meta data   |

### DataSourcePoolMetaData 

| *Implementation Class*       | *Description*                     |
|------------------------------|-----------------------------------|
| DBCPDataSourcePoolMetaData   | DBCP data source pool meta data   |
| HikariDataSourcePoolMetaData | Hikari data source pool meta data |

### DataSourcePoolActiveDetector

| *Implementation Class*              | *Description*                            |
| ----------------------------------- | ---------------------------------------- |
| DefaultDataSourcePoolActiveDetector | Default data source pool active detector |
| HikariDataSourcePoolActiveDetector  | Hikari data source pool active detector  |
