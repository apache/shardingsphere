+++
pre = "<b>5.4. </b>"
title = "数据源"
weight = 4
chapter = true
+++

## DatabaseType

### 全限定类名

[`org.apache.shardingsphere.infra.database.type.DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/DatabaseType.java)

### 定义

支持的数据库类型

### 已知实现

| *配置标识* | *详细说明*                             | *全限定类名* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| SQL92                | 遵循 SQL92 标准的数据库类型                       | [`org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/SQL92DatabaseType.java) |
| MySQL                | MySQL 数据库                            | [`org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/MySQLDatabaseType.java) |
| MariaDB              | MariaDB 数据库                          | [`org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/MariaDBDatabaseType.java) |
| PostgreSQL           | PostgreSQL 数据库                       | [`org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/PostgreSQLDatabaseType.java) |
| Oracle               | Oracle 数据库                           | [`org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/OracleDatabaseType.java) |
| SQLServer            | SQLServer 数据库                        | [`org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/SQLServerDatabaseType.java) |
| H2                   | H2 数据库                               | [`org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/H2DatabaseType.java) |
| openGauss            | OpenGauss 数据库                        | [`org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/database/type/dialect/OpenGaussDatabaseType.java) |

## DialectSchemaMetaDataLoader

### 全限定类名

[`org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/spi/DialectSchemaMetaDataLoader.java)

### 定义

使用 SQL 方言快速加载元数据

### 已知实现

| *配置标识* | *详细说明*                           | *全限定类名* |
| -------------------- | --------------------------------------- | ---------------------- |
| MySQL                | 使用 MySQL 方言加载元数据      | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.MySQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/MySQLSchemaMetaDataLoader.java) |
| Oracle               | 使用 Oracle 方言加载元数据    | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.OracleSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/OracleSchemaMetaDataLoader.java) |
| PostgreSQL           | 使用 PostgreSQL 方言加载元数据 | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.PostgreSQLSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/PostgreSQLSchemaMetaDataLoader.java) |
| SQLServer            | 使用 SQLServer 方言加载元数据 | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.SQLServerSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/SQLServerSchemaMetaDataLoader.java) |
| H2                   | 使用 H2 方言加载元数据         | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.H2SchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/H2SchemaMetaDataLoader.java) |
| openGauss            | 使用 OpenGauss 方言加载元数据 | [`org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect.OpenGaussSchemaMetaDataLoader`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/metadata/database/schema/loader/dialect/OpenGaussSchemaMetaDataLoader.java) |

## DataSourcePoolMetaData

### 全限定类名

[`org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/DataSourcePoolMetaData.java)

### 定义

数据源连接池元数据

### 已知实现

| *配置标识*                                                                  | *详细说明*                    | *全限定类名* |
| ------------------------------------------------------------------------------------- | -------------------------------- | ---------------------------- |
| org.apache.commons.dbcp.BasicDataSource, org.apache.tomcat.dbcp.dbcp2.BasicDataSource | DBCP 数据库连接池元数据   | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp.DBCPDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/dbcp/DBCPDataSourcePoolMetaData.java) |
| com.zaxxer.hikari.HikariDataSource                                                    | Hikari 数据源连接池元数据 | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari.HikariDataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/hikari/HikariDataSourcePoolMetaData.java) |
| com.mchange.v2.c3p0.ComboPooledDataSource                                             | C3P0 数据源连接池元数据  | [`org.apache.shardingsphere.infra.datasource.pool.metadata.type.c3p0.C3P0DataSourcePoolMetaData`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/metadata/type/c3p0/C3P0DataSourcePoolMetaData.java)         |

## DataSourcePoolActiveDetector

### 全限定类名

[`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.DataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/DataSourcePoolActiveDetector.java)

### 定义

数据源连接池活跃探测器

### 已知实现

| *配置标识*               | *详细说明*                            | *全限定类名* |
| ---------------------------------- | ---------------------------------------- | ---------------------------- |
| Default                            | 默认数据源连接池活跃探测器 | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.DefaultDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/DefaultDataSourcePoolActiveDetector.java) |
| com.zaxxer.hikari.HikariDataSource | Hikari 数据源连接池活跃探测器  | [`org.apache.shardingsphere.infra.datasource.pool.destroyer.detector.type.HikariDataSourcePoolActiveDetector`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/datasource/pool/destroyer/detector/type/HikariDataSourcePoolActiveDetector.java) |
