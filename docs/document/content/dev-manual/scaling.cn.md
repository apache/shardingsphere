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

| *SPI 名称*                                   | *详细说明*                   |
| ------------------------------------------- | --------------------------- |
| JobCompletionDetectAlgorithm                | 作业是否接近完成检测算法         |

| *已知实现类*                                  | *详细说明*                    |
| ------------------------------------------- | ---------------------------- |
| IdleRuleAlteredJobCompletionDetectAlgorithm | 基于增量迁移任务空闲时长的检测算法 |

## DataConsistencyCalculateAlgorithm

| *SPI 名称*                                   | *详细说明*                    |
| ------------------------------------------- | ---------------------------- |
| DataConsistencyCalculateAlgorithm           | 校验数据一致性使用的算法         |

| *已知实现类*                                  | *详细说明*                    |
| ------------------------------------------- | ---------------------------- |
| DataMatchDataConsistencyCalculateAlgorithm  | 根据数据逐条校验数据一致性的算法   |
| CRC32MatchDataConsistencyCalculateAlgorithm | 使用 CRC32 校验数据一致性的算法  |
