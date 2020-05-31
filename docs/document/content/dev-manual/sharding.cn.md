+++
pre = "<b>5.4. </b>"
title = "数据分片"
weight = 4
chapter = true
+++

## ShardingAlgorithm

| *SPI 名称*                       | *详细说明*                   |
| ------------------------------- | ---------------------------- |
| ShardingAlgorithm               | 分片算法                      |

| *已知实现类*                     | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| InlineShardingAlgorithm         | 基于行表达式的分片算法          |
| ModuloShardingAlgorithm         | 基于取模的分片算法             |
| HashShardingAlgorithm           | 基于哈希取模的分片算法          |
| DatetimeShardingAlgorithm       | 基于时间的分片算法             |
| CustomDateTimeShardingAlgorithm | 基于用户自定义时间格式的分片算法 |
| StandardRangeShardingAlgorithm  | 基于范围的分片算法             |
| CustomRangeShardingAlgorithm    | 基于用户自定义范围的分片算法     |

## KeyGenerateAlgorithm

| *SPI 名称*                    | *详细说明*                    |
| ----------------------------- | ---------------------------- |
| KeyGenerateAlgorithm          | 分布式主键生成算法             |

| *已知实现类*                   | *详细说明*                    |
| ----------------------------- | ---------------------------- |
| SnowflakeKeyGenerateAlgorithm | 基于雪花算法的分布式主键生成算法 |
| UUIDKeyGenerateAlgorithm      | 基于UUID的分布式主键生成算法    |

## TimeService

| *SPI 名称*                  | *详细说明*                   |
| --------------------------- | --------------------------- |
| TimeService                 | 获取当前时间进行路由           |

| *已知实现类*                 | *详细说明*                       |
| --------------------------- | ------------------------------- |
| DefaultTimeService          | 从应用系统时间中获取当前时间进行路由 |
| DatabaseTimeServiceDelegate | 从数据库中获取当前时间进行路由      |

## DatabaseSQLEntry

| *SPI 名称*                 | *详细说明*                          |
| -------------------------- | ---------------------------------- |
| DatabaseSQLEntry           | 获取当前时间的数据库方言              |

| *已知实现类*                | *详细说明*                          |
| -------------------------- | ---------------------------------- |
| MySQLDatabaseSQLEntry      | 从 MySQL 获取当前时间的数据库方言     |
| PostgreSQLDatabaseSQLEntry | 从 PostgreSQL 获取当前时间的数据库方言|
| OracleDatabaseSQLEntry     | 从 Oracle 获取当前时间的数据库方言    |
| SQLServerDatabaseSQLEntry  | 从 SQLServer 获取当前时间的数据库方言 |
