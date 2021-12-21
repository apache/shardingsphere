+++
pre = "<b>6.11. </b>"
title = "弹性伸缩"
weight = 11
chapter = true
+++

## ScalingEntry

| *SPI 名称*             | *详细说明*                    |
| ---------------------- | ---------------------------- |
| ScalingEntry           | 弹性伸缩入口                  |

| *已知实现类*            | *详细说明*                    |
| ---------------------- | ---------------------------- |
| MySQLScalingEntry      | 基于 MySQL 的弹性伸缩入口      |
| PostgreSQLScalingEntry | 基于 PostgreSQL 的弹性伸缩入口 |

## ScalingClusterAutoSwitchAlgorithm

| *SPI 名称*                                   | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| ScalingClusterAutoSwitchAlgorithm           | 迁移任务完成度自动检测算法                      |

| *已知实现类*                                  | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| ScalingIdleClusterAutoSwitchAlgorithm       | 基于增量迁移任务空闲时长的检测算法                |

## ScalingDataConsistencyCheckAlgorithm

| *SPI 名称*                                   | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| ScalingDataConsistencyCheckAlgorithm        | 数据一致性校验算法                             |

| *已知实现类*                                  | *详细说明*                                   |
| ------------------------------------------- | ------------------------------------------- |
| ScalingDefaultDataConsistencyCheckAlgorithm | 默认数据一致性校验算法。对全量数据做CRC32计算。    |
