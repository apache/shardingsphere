+++
pre = "<b>6.11. </b>"
title = "弹性伸缩"
weight = 11
chapter = true
+++

## ScalingEntry

| *SPI 名称*             | *详细说明*                    |
| ---------------------- | --------------------------- |
| ScalingEntry           | 弹性伸缩入口                  |

| *已知实现类*            | *详细说明*                    |
| ---------------------- | --------------------------- |
| MySQLScalingEntry      | 基于 MySQL 的弹性伸缩入口      |
| PostgreSQLScalingEntry | 基于 PostgreSQL 的弹性伸缩入口 |
| OpenGaussScalingEntry  | 基于 openGauss 的弹性伸缩入口  |

## JobCompletionDetectAlgorithm

| *SPI 名称*                                   | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| JobCompletionDetectAlgorithm                | 作业是否接近完成检测算法                        |

| *已知实现类*                                  | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| IdleRuleAlteredJobCompletionDetectAlgorithm | 基于增量迁移任务空闲时长的检测算法                |

## DataConsistencyCheckAlgorithm

| *SPI 名称*                                   | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| DataConsistencyCheckAlgorithm               | 数据一致性校验算法                             |

| *已知实现类*                                  | *详细说明*                                            |
| ------------------------------------------- | ---------------------------------------------------- |
| DataMatchDataConsistencyCheckAlgorithm      | 基于数据匹配的一致性校验算法。类型名：DATA_MATCH。          |
| CRC32MatchDataConsistencyCheckAlgorithm     | 基于数据CRC32匹配的一致性校验算法。类型名：CRC32_MATCH。    |

## SingleTableDataCalculator

| *SPI 名称*                                   | *详细说明*                                            |
| ------------------------------------------- | ---------------------------------------------------- |
| SingleTableDataCalculator                   | 给数据一致性校验使用的单表数据计算算法                     |

| *已知实现类*                                  | *详细说明*                                                              |
| ------------------------------------------- | ---------------------------------------------------------------------- |
| DataMatchSingleTableDataCalculator          | 给 DATA_MATCH 数据一致性校验算法使用的单表数据计算算法。适用于所有数据库。        |
| CRC32MatchMySQLSingleTableDataCalculator    | 给 CRC32_MATCH 数据一致性校验算法使用的单表数据计算算法。适用于MySQL。          |
