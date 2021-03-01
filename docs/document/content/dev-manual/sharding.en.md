+++
pre = "<b>5.4. </b>"
title = "Data Sharding"
weight = 4
chapter = true
+++

## ShardingAlgorithm

| *SPI Name*                      | *Description*                          |
| ------------------------------- | -------------------------------------- |
| ShardingAlgorithm               | Sharding algorithm                     |

| *Implementation Class*              | *Description*                           |
| ----------------------------------- | --------------------------------------- |
| InlineShardingAlgorithm             | Inline sharding algorithm               |
| ModShardingAlgorithm                | Modulo sharding algorithm               |
| HashModShardingAlgorithm            | Hash modulo sharding algorithm          |
| FixedIntervalShardingAlgorithm      | Fixed interval sharding algorithm       |
| MutableIntervalShardingAlgorithm    | Mutable interval sharding algorithm     |
| VolumeBasedRangeShardingAlgorithm   | Volume based range sharding algorithm   |
| BoundaryBasedRangeShardingAlgorithm | Boundary based range sharding algorithm |
| ClassBasedShardingAlgorithm         | Class based sharding algorithm          |

## KeyGenerateAlgorithm

| *SPI Name*                    | *Description*                    |
| ----------------------------- | -------------------------------- |
| KeyGenerateAlgorithm          | Key generate algorithm           |

| *Implementation Class*        | *Description*                    |
| ----------------------------- | -------------------------------- |
| SnowflakeKeyGenerateAlgorithm | Snowflake key generate algorithm |
| UUIDKeyGenerateAlgorithm      | UUID key generate algorithm      |

## TimeService

| *SPI Name*                  | *Description*                                                |
| --------------------------- | ------------------------------------------------------------ |
| TimeService                 | Use current time for routing                                 |

| *Implementation Class*      | *Description*                                                |
| --------------------------- | ------------------------------------------------------------ |
| DefaultTimeService          | Get the current time from the application system for routing |
| DatabaseTimeServiceDelegate | Get the current time from the database for routing           |

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
