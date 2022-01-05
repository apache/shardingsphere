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

## JobRateLimitAlgorithm

| *SPI 名称*                                   | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| JobRateLimitAlgorithm                       | 任务限流算法                                  |

| *已知实现类*                                  | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| SourceJobRateLimitAlgorithm                 | 源端限流算法                                  |

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

| *已知实现类*                                  | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| DataMatchDataConsistencyCheckAlgorithm      | 基于数据匹配的一致性校验算法                    |
| CRC32MatchDataConsistencyCheckAlgorithm     | 基于数据CRC32匹配的一致性校验算法               |
