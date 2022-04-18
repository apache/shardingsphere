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

| *SPI 名称*                                   | *详细说明*                      |
| ------------------------------------------- | ------------------------------ |
| JobCompletionDetectAlgorithm                | 作业是否接近完成检测算法            |

| *已知实现类*                                  | *详细说明*                       |
| ------------------------------------------- | ------------------------------- |
| IdleRuleAlteredJobCompletionDetectAlgorithm | 基于增量迁移任务空闲时长的检测算法    |

## SingleTableDataCalculator

| *SPI 名称*                                   | *详细说明*                       |
| ------------------------------------------- | ------------------------------- |
| SingleTableDataCalculator                   | 给数据一致性校验使用的单表数据计算算法 |

| *已知实现类*                                  | *详细说明*                       |
| ------------------------------------------- | ------------------------------- |
| DataMatchSingleTableDataCalculator          | 根据数据逐条校验数据一致性的算法      |
| CRC32MatchSingleTableDataCalculator         | 使用 CRC32 校验数据一致性的算法     |
