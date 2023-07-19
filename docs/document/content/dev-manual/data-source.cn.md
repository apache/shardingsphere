+++
pre = "<b>5.4. </b>"
title = "数据源"
weight = 4
chapter = true
+++

## DatabaseType

### 全限定类名

[`org.apache.shardingsphere.infra.database.spi.DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/spi/src/main/java/org/apache/shardingsphere/infra/database/spi/DatabaseType.java)

### 定义

支持的数据库类型

### 已知实现

| *配置标识*   | *详细说明*               | *全限定类名*                                                                                                                                                                                                                                                        |
|------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SQL92      | 遵循 SQL92 标准的数据库类型 | [`org.apache.shardingsphere.infra.database.sql92.SQL92DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/sql92/src/main/java/org/apache/shardingsphere/infra/database/sql92/SQL92DatabaseType.java)                          |
| MySQL      | MySQL 数据库             | [`org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/mysql/src/main/java/org/apache/shardingsphere/infra/database/mysql/MySQLDatabaseType.java)                          |
| MariaDB    | MariaDB 数据库           | [`org.apache.shardingsphere.infra.database.mariadb.MariaDBDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/mariadb/src/main/java/org/apache/shardingsphere/infra/database/mariadb/MariaDBDatabaseType.java)                |
| PostgreSQL | PostgreSQL 数据库        | [`org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/postgresql/src/main/java/org/apache/shardingsphere/infra/database/postgresql/PostgreSQLDatabaseType.java) |
| Oracle     | Oracle 数据库            | [`org.apache.shardingsphere.infra.database.oracle.OracleDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/oracle/src/main/java/org/apache/shardingsphere/infra/database/oracle/OracleDatabaseType.java)                     |
| SQLServer  | SQLServer 数据库         | [`org.apache.shardingsphere.infra.database.sqlserver.SQLServerDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/sqlserver/src/main/java/org/apache/shardingsphere/infra/database/sqlserver/SQLServerDatabaseType.java)      |
| H2         | H2 数据库                | [`org.apache.shardingsphere.infra.database.h2.H2DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/h2/src/main/java/org/apache/shardingsphere/infra/database/h2/H2DatabaseType.java)                                         |
| openGauss  | OpenGauss 数据库         | [`org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/database/type/opengauss/src/main/java/org/apache/shardingsphere/infra/database/opengauss/OpenGaussDatabaseType.java)      |

## DialectSchemaMetaDataLoader

### 全限定类名

[`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.DialectSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/DialectSchemaMetaDataLoader.java)

### 定义

使用 SQL 方言快速加载元数据

### 已知实现

| *配置标识*     | *详细说明*                | *全限定类名*                                                                                                                                                                                                                                                                                                 |
|------------|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL      | 使用 MySQL 方言加载元数据      | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.MySQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/MySQLSchemaMetaDataLoader.java)           |
| Oracle     | 使用 Oracle 方言加载元数据     | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.OracleSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/OracleSchemaMetaDataLoader.java)         |
| PostgreSQL | 使用 PostgreSQL 方言加载元数据 | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.PostgreSQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/PostgreSQLSchemaMetaDataLoader.java) |
| SQLServer  | 使用 SQLServer 方言加载元数据  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.SQLServerSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/SQLServerSchemaMetaDataLoader.java)   |
| H2         | 使用 H2 方言加载元数据         | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.H2SchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/H2SchemaMetaDataLoader.java)                 |
| openGauss  | 使用 OpenGauss 方言加载元数据  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.dialect.OpenGaussSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/OpenGaussSchemaMetaDataLoader.java)   |

## DialectDataTypeLoader

### 全限定类名

[`org.apache.shardingsphere.infra.metadata.database.schema.loader.datatype.DialectDataTypeLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/datatype/DialectDataTypeLoader.java)

### 定义

数据类型加载器

### 已知实现

| *配置标识* | *详细说明*         | *全限定类名*                                                                                                                                                                                                                                                                                                     |
|--------|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL  | MySQL 数据类型加载器  | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.datatype.dialect.MySQLDataTypeLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/MySQLSchemaMetaDataLoader.java)   |
| Oracle | Oracle 数据类型加载器 | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.datatype.dialect.OracleDataTypeLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/metadata/dialect/OracleSchemaMetaDataLoader.java) |

## DataSourcePoolMetaData

### 全限定类名

[`org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/DataSourcePoolMetaData.java)

### 定义

数据源连接池元数据

### 已知实现

| *配置标识*                                                                                | *详细说明*           | *全限定类名*                                                                                                                                                                                                                                                                                       |
|---------------------------------------------------------------------------------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| org.apache.commons.dbcp.BasicDataSource, org.apache.tomcat.dbcp.dbcp2.BasicDataSource | DBCP 数据库连接池元数据   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp.DBCPDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/dbcp/DBCPDataSourcePoolMetaData.java)         |
| com.zaxxer.hikari.HikariDataSource                                                    | Hikari 数据源连接池元数据 | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari.HikariDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/hikari/HikariDataSourcePoolMetaData.java) |
| com.mchange.v2.c3p0.ComboPooledDataSource                                             | C3P0 数据源连接池元数据   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.c3p0.C3P0DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/c3p0/C3P0DataSourcePoolMetaData.java)         |

## DataSourcePoolActiveDetector

### 全限定类名

[`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/DataSourcePoolActiveDetector.java)

### 定义

数据源连接池活跃探测器

### 已知实现

| *配置标识*                             | *详细说明*             | *全限定类名*                                                                                                                                                                                                                                                                                                           |
|------------------------------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Default                            | 默认数据源连接池活跃探测器      | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.DefaultDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/DefaultDataSourcePoolActiveDetector.java) |
| com.zaxxer.hikari.HikariDataSource | Hikari 数据源连接池活跃探测器 | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.HikariDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/HikariDataSourcePoolActiveDetector.java)   |

## ShardingSphereDriverURLProvider

### 全限定类名

[`org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/ShardingSphereDriverURLProvider.java)

### 定义

ShardingSphere 驱动 URL 提供器

### 已知实现

| *配置标识*                                  | *详细说明*         | *全限定类名*                                                                                                                                                                                                                                                        |
|-----------------------------------------|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| jdbc:shardingsphere:classpath:<path>    | 驱动的类路径加载器      | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.ClasspathDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/ClasspathDriverURLProvider.java)       |
| jdbc:shardingsphere:absolutepath:<path> | 驱动的绝对路径加载器     | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.AbsolutePathDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/AbsolutePathDriverURLProvider.java) |
| jdbc:shardingsphere:apollo:<namespace>  | 驱动的 Apollo 加载器 | [`org.apache.shardingsphere.driver.jdbc.core.driver.spi.ApolloDriverURLProvider`](https://github.com/apache/shardingsphere/blob/master/jdbc/core/src/main/java/org/apache/shardingsphere/driver/jdbc/core/driver/spi/ApolloDriverURLProvider.java)             |

### 注意

当您使用 Apollo 加载器时，需要添加对应的 apollo 的 pom 依赖，目前适配的版本为 `1.9.0` , 如下：

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>${apollo.version}</version>
</dependency>
```