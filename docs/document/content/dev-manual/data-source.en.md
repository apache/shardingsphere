+++
pre = "<b>5.4. </b>"
title = "DataSource"
weight = 4
chapter = true
+++

## DatabaseType

### Fully-qualified class name

[`org.apache.shardingsphere.infra.database.spi.DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/spi/src/main/java/org/apache/shardingsphere/infra/database/spi/DatabaseType.java)

### Definition

Supported database types definition

### Implementation classes

| *Configuration Type* | *Description*       | *Fully-qualified class name*                                                                                                                                                                                                                                      |
|----------------------|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SQL92                | SQL92 database type | [`org.apache.shardingsphere.infra.database.sql92.SQL92DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/sql92/src/main/java/org/apache/shardingsphere/infra/database/sql92/SQL92DatabaseType.java)                          |
| MySQL                | MySQL database      | [`org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/mysql/src/main/java/org/apache/shardingsphere/infra/database/mysql/MySQLDatabaseType.java)                          |
| MariaDB              | MariaDB database    | [`org.apache.shardingsphere.infra.database.mariadb.MariaDBDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/mariadb/src/main/java/org/apache/shardingsphere/infra/database/mariadb/MariaDBDatabaseType.java)                |
| PostgreSQL           | PostgreSQL database | [`org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/postgresql/src/main/java/org/apache/shardingsphere/infra/database/postgresql/PostgreSQLDatabaseType.java) |
| Oracle               | Oracle database     | [`org.apache.shardingsphere.infra.database.oracle.OracleDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/oracle/src/main/java/org/apache/shardingsphere/infra/database/oracle/OracleDatabaseType.java)                     |
| SQLServer            | SQLServer database  | [`org.apache.shardingsphere.infra.database.sqlserver.SQLServerDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/sqlserver/src/main/java/org/apache/shardingsphere/infra/database/sqlserver/SQLServerDatabaseType.java)      |
| H2                   | H2 database         | [`org.apache.shardingsphere.infra.database.h2.H2DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/h2/src/main/java/org/apache/shardingsphere/infra/database/h2/H2DatabaseType.java)                                         |
| openGauss            | OpenGauss database  | [`org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/opengauss/src/main/java/org/apache/shardingsphere/infra/database/opengauss/OpenGaussDatabaseType.java)      |

## DialectSchemaMetaDataLoader

### Fully-qualified class name

[`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.DialectSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/DialectSchemaMetaDataLoader.java)

### Definition

Use SQL dialect to load meta data rapidly

### Implementation classes

| *Configuration Type* | *Description*                            | *Fully-qualified class name*                                                                                                                                                                                                                                                                            |
|----------------------|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL                | Use MySQL dialect to load meta data      | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.MySQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/MySQLSchemaMetaDataLoader.java)           |
| Oracle               | Use Oracle dialect to load meta data     | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.OracleSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/OracleSchemaMetaDataLoader.java)         |
| PostgreSQL           | Use PostgreSQL dialect to load meta data | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.PostgreSQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/PostgreSQLSchemaMetaDataLoader.java) |
| SQLServer            | Use SQLServer dialect to load meta data  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.SQLServerSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/SQLServerSchemaMetaDataLoader.java)   |
| H2                   | Use H2 dialect to load meta data         | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.H2SchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/H2SchemaMetaDataLoader.java)                 |
| openGauss            | Use OpenGauss dialect to load meta data  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.OpenGaussSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/OpenGaussSchemaMetaDataLoader.java)   |

## DataSourcePoolMetaData 

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/DataSourcePoolMetaData.java)

### Definition

Data source connection pool metadata

### Implementation classes

| *Configuration Type*                                                                  | *Description*                     | *Fully-qualified class name*                                                                                                                                                                                                                                                                  |
|---------------------------------------------------------------------------------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| org.apache.commons.dbcp.BasicDataSource, org.apache.tomcat.dbcp.dbcp2.BasicDataSource | DBCP data source pool meta data   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp.DBCPDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/dbcp/DBCPDataSourcePoolMetaData.java)         |
| com.zaxxer.hikari.HikariDataSource                                                    | Hikari data source pool meta data | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari.HikariDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/hikari/HikariDataSourcePoolMetaData.java) |
| com.mchange.v2.c3p0.ComboPooledDataSource                                             | C3P0 data source pool meta data   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.c3p0.C3P0DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/c3p0/C3P0DataSourcePoolMetaData.java)         |

## DataSourcePoolActiveDetector

### Fully-qualified class name

[`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/DataSourcePoolActiveDetector.java)

### Definition

Data source connection pool active detector

### Implementation classes

| *Configuration Type*               | *Description*                            | *Fully-qualified class name*                                                                                                                                                                                                                                                                                      |
|------------------------------------|------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Default                            | Default data source pool active detector | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.DefaultDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/DefaultDataSourcePoolActiveDetector.java) |
| com.zaxxer.hikari.HikariDataSource | Hikari data source pool active detector  | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.HikariDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/HikariDataSourcePoolActiveDetector.java)   |

## ShardingSphereDriverURLProvider

### Fully-qualified class name

[`org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/ShardingSphereDriverURLProvider.java)

### Definition

ShardingSphere driver URL provider

### Implementation classes

| *Configuration Type*                    | *Description*                         | *Fully-qualified class name*                                                                                                                                                                                                                                   |
|-----------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| jdbc:shardingsphere:classpath:<path>    | The classpath driver URL provider     | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.ClasspathDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/ClasspathDriverURLProvider.java)       |
| jdbc:shardingsphere:absolutepath:<path> | The absolute path driver URL provider | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.AbsolutePathDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/AbsolutePathDriverURLProvider.java) |
| jdbc:shardingsphere:apollo:<namespace>  | The apollo driver URL provider        | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.ApolloDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/ApolloDriverURLProvider.java)             |

### Notice

When you use the Apollo driver url provider, you need to add the corresponding apollo pom dependency, currently available at version `1.9.0`, as follows:

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>${apollo.version}</version>
</dependency>
```