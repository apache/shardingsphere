+++
pre = "<b>6.7. </b>"
title = "Data Sharding"
weight = 7
chapter = true
+++

## ShardingAlgorithm

| *SPI Name*                          | *Description*                                  |
| ----------------------------------- | ---------------------------------------------- |
| ShardingAlgorithm                   | Sharding algorithm                             |

| *Implementation Class*              | *Description*                                  |
| ----------------------------------- | ---------------------------------------------- |
| BoundaryBasedRangeShardingAlgorithm | Boundary based range sharding algorithm        |
| VolumeBasedRangeShardingAlgorithm   | Volume based range sharding algorithm          |
| ComplexInlineShardingAlgorithm      | Complex inline sharding algorithm              |
| AutoIntervalShardingAlgorithm       | Mutable interval sharding algorithm            |
| ClassBasedShardingAlgorithm         | Class based sharding algorithm                 |
| HintInlineShardingAlgorithm         | Hint inline sharding algorithm                 |
| IntervalShardingAlgorithm           | Fixed interval sharding algorithm              |
| HashModShardingAlgorithm            | Hash modulo sharding algorithm                 |
| InlineShardingAlgorithm             | Inline sharding algorithm                      |
| ModShardingAlgorithm                | Modulo sharding algorithm                      |

## KeyGenerateAlgorithm

| *SPI Name*                    | *Description*                    |
| ----------------------------- | -------------------------------- |
| KeyGenerateAlgorithm          | Key generate algorithm           |

| *Implementation Class*        | *Description*                    |
| ----------------------------- | -------------------------------- |
| SnowflakeKeyGenerateAlgorithm | Snowflake key generate algorithm |
| UUIDKeyGenerateAlgorithm      | UUID key generate algorithm      |

## DatetimeService

| *SPI Name*                      | *Description*                                                |
| ------------------------------- | ------------------------------------------------------------ |
| DatetimeService                 | Use current time for routing                                 |

| *Implementation Class*          | *Description*                                                |
| ------------------------------- | ------------------------------------------------------------ |
| DatabaseDatetimeServiceDelegate | Get the current time from the database for routing           |
| SystemDatetimeService           | Get the current time from the application system for routing |

## DatabaseSQLEntry

| *SPI Name*                 | *Description*                           |
| -------------------------- | --------------------------------------- |
| DatabaseSQLEntry           | Database dialect for get current time   |

| *Implementation Class*     | *Description*                           |
| -------------------------- | --------------------------------------- |
| MySQLDatabaseSQLEntry      | MySQL dialect for get current time      |
| PostgreSQLDatabaseSQLEntry | PostgreSQL dialect for get current time |
| OracleDatabaseSQLEntry     | Oracle dialect for get current time     |
| SQLServerDatabaseSQLEntry  | SQLServer dialect for get current time  |
