+++
pre = "<b>6.3. </b>"
title = "Kernel"
weight = 3
chapter = true
+++

## SPI Interface

| *SPI Name*                           | *Description*             |
| ----------------------------------- | --------------------- |
| SQLRouter                           | Used to process routing results         |
| SQLRewriteContextDecorator         | Used to handle SQL rewrite results |
| SQLExecutionHook              | SQL execution process listener |
| ResultProcessEngine          | Used to process result sets        |
| StoragePrivilegeHandler    | Use SQL dialect to process privilege metadata          |
| DynamicDataSourceStrategy                  | Dynamic data source fetch strategy                 |

## Sample

### SQLRouter

| *Implementation Class*                          | *Description*             |
| ----------------------------------- | --------------------- |
| ReadwriteSplittingSQLRouter         | Used to process read-write splitting routing results  |
| DatabaseDiscoverySQLRouter          | Used to process database discovery routing results |
| SingleTableSQLRouter                | Used to process single-table routing results      |
| ShardingSQLRouter                   | Used to process sharding routing results      |
| ShadowSQLRouter                     | Used to process shadow database routing results    |

### SQLRewriteContextDecorator
| *Implementation Class*                         | *Description*              |
| ---------------------------------- | --------------------- |
| ShardingSQLRewriteContextDecorator | Used to process sharding SQL rewrite results |
| EncryptSQLRewriteContextDecorator  | Used to process encryption SQL rewrite results |

### SQLExecutionHook
| *Implementation Class*                    | *Description*                 |
| ----------------------------- | ------------------------- |
| TransactionalSQLExecutionHook | Transaction hook of SQL execution |

### ResultProcessEngine

| *Implementation Class*                   | *Description*           |
| ---------------------------- | ------------------- |
| ShardingResultMergerEngine   | Used to handle sharding result set merge |
| EncryptResultDecoratorEngine | Used to handle encrypted result set overrides |

### StoragePrivilegeHandler

| *Implementation Class*                 | *Description*                      |
| -------------------------- | ------------------------------ |
| PostgreSQLPrivilegeHandler | Use PostgreSQL dialect to process privilege metadata   |
| SQLServerPrivilegeHandler  | Use SQLServer dialect to process privilege metadata    |
| OraclePrivilegeHandler     | Use Oracle dialect to process privilege metadata       |
| MySQLPrivilegeHandler      | Use MySQL dialect to process privilege metadata        |

### DynamicDataSourceStrategy

| *Implementation Class*                                 | *Description*                       |
| ------------------------------------------ | ------------------------------- |
| DatabaseDiscoveryDynamicDataSourceStrategy | Use database discovery to dynamic fetch data source |
