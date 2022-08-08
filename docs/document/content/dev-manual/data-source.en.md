+++
pre = "<b>5.4. </b>"
title = "DataSource"
weight = 4
chapter = true
+++

## DatabaseType

### Fully-qualified class name

[`org.apache.shardingsphere.infra.database.type.DatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/DatabaseType.java)

### Definition

Supported database types definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| SQL92                | SQL92 database type                       | [`org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/SQL92DatabaseType.java) |
| MySQL                | MySQL database                            | [`org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/MySQLDatabaseType.java) |
| MariaDB              | MariaDB database                          | [`org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/MariaDBDatabaseType.java) |
| PostgreSQL           | PostgreSQL database                       | [`org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/PostgreSQLDatabaseType.java) |
| Oracle               | Oracle database                           | [`org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/OracleDatabaseType.java) |
| SQLServer            | SQLServer database                        | [`org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/SQLServerDatabaseType.java) |
| H2                   | H2 database                               | [`org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/H2DatabaseType.java) |
| openGauss            | OpenGauss database                        | [`org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/OpenGaussDatabaseType.java) |

## DialectSchemaMetaDataLoader

### Fully-qualified class name

[`org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/spi/DialectSchemaMetaDataLoader.java)

### Definition

Use SQL dialect to load meta data rapidly

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| MySQL                | Use MySQL dialect to load meta data       | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.MySQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/MySQLSchemaMetaDataLoader.java) |
| Oracle               | Use Oracle dialect to load meta data      | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.OracleSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/OracleSchemaMetaDataLoader.java) |
| PostgreSQL           | Use PostgreSQL dialect to load meta data  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.PostgreSQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/PostgreSQLSchemaMetaDataLoader.java) |
| SQLServer            | Use SQLServer dialect to load meta data   | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.SQLServerSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/SQLServerSchemaMetaDataLoader.java) |
| H2                   | Use H2 dialect to load meta data          | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.H2SchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/H2SchemaMetaDataLoader.java) |
| openGauss            | Use OpenGauss dialect to load meta data   | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.OpenGaussSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/OpenGaussSchemaMetaDataLoader.java) |

## DataSourcePoolMetaData 

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/DataSourcePoolMetaData.java)

### Definition

Data source connection pool metadata

### Implementation classes

| *Configuration Type*                                                                  | *Description*                     | *Fully-qualified class name* |
| ------------------------------------------------------------------------------------- | --------------------------------- | ---------------------------- |
| org.apache.commons.dbcp.BasicDataSource, org.apache.tomcat.dbcp.dbcp2.BasicDataSource | DBCP data source pool meta data   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp.DBCPDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/dbcp/DBCPDataSourcePoolMetaData.java) |
| com.zaxxer.hikari.HikariDataSource                                                    | Hikari data source pool meta data | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari.HikariDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/hikari/HikariDataSourcePoolMetaData.java) |
| com.mchange.v2.c3p0.ComboPooledDataSource                                             | C3P0 data source pool meta data   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.c3p0.C3P0DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/c3p0/C3P0DataSourcePoolMetaData.java)         |

## DataSourcePoolActiveDetector

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/DataSourcePoolActiveDetector.java)

### Definition

Data source connection pool active detector

### Implementation classes

| *Configuration Type*               | *Description*                            | *Fully-qualified class name* |
| ---------------------------------- | ---------------------------------------- | ---------------------------- |
| Default                            | Default data source pool active detector | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.DefaultDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/DefaultDataSourcePoolActiveDetector.java) |
| com.zaxxer.hikari.HikariDataSource | Hikari data source pool active detector  | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.HikariDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/HikariDataSourcePoolActiveDetector.java) |
