+++
pre = "<b>6.4. </b>"
title = "数据源"
weight = 4
chapter = true
+++

## DatabaseType

| *SPI 名称*              | *详细说明*               |
| ---------------------- | ----------------------- |
| DatabaseType           | 支持的数据库类型           |

| *已知实现类*             | *详细说明*               |
| ---------------------- | ----------------------- |
| SQL92DatabaseType      | 遵循 SQL92 标准的数据库类型 |
| MySQLDatabaseType      | MySQL 数据库             |
| MariaDBDatabaseType    | MariaDB 数据库           |
| PostgreSQLDatabaseType | PostgreSQL 数据库        |
| OracleDatabaseType     | Oracle 数据库            |
| SQLServerDatabaseType  | SQLServer 数据库         |
| H2DatabaseType         | H2 数据库                |
| OpenGaussDatabaseType  | OpenGauss 数据库         |

## DialectTableMetaDataLoader

| *SPI 名称*                    | *详细说明*                   |
| ---------------------------- | --------------------------- |
| DialectTableMetaDataLoader   | 用于使用数据库方言快速加载元数据  |

| *已知实现类*                    | *详细说明*                   |
| ----------------------------- | --------------------------- |
| MySQLTableMetaDataLoader      | 使用 MySQL 方言加载元数据      |
| OracleTableMetaDataLoader     | 使用 Oracle 方言加载元数据     |
| PostgreSQLTableMetaDataLoader | 使用 PostgreSQL 方言加载元数据 |
| SQLServerTableMetaDataLoader  | 使用 SQLServer 方言加载元数据  |
| H2TableMetaDataLoader         | 使用 H2 方言加载元数据         |
| OpenGaussTableMetaDataLoader  | 使用 OpenGauss 方言加载元数据  |

## DataSourcePoolMetaData

| *SPI 名称*                    | *详细说明*        |
| ---------------------------- |-----------------|
| DataSourcePoolMetaData       | 数据源连接池元数据 |

| *已知实现类*                   | *详细说明*              |
|------------------------------|-----------------------|
| DBCPDataSourcePoolMetaData   | DBCP 数据库连接池元数据   |
| HikariDataSourcePoolMetaData | Hikari 数据源连接池元数据 |

## DataSourcePoolActiveDetector

| *SPI 名称*                           | *详细说明*                 |
| ----------------------------------- | ------------------------- |
| DataSourcePoolActiveDetector        | 数据源连接池活跃探测器        |

| *已知实现类*                          | *详细说明*                 |
| ----------------------------------- | ------------------------- |
| DefaultDataSourcePoolActiveDetector | 默认数据源连接池活跃探测器     |
| HikariDataSourcePoolActiveDetector  | Hikari 数据源连接池活跃探测器 |
