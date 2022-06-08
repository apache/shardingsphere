+++
pre = "<b>6.9. </b>"
title = "高可用"
weight = 9
chapter = true
+++

## DatabaseDiscoveryProviderAlgorithm

| *SPI 名称*                                                    | *详细说明*                        |
| ------------------------------------------------------------ | -------------------------------- |
| DatabaseDiscoveryProviderAlgorithm                           | 数据库发现算法                      |

| *已知实现类*                                                   | *详细说明*                         |
| ------------------------------------------------------------ | --------------------------------- |
| MGRDatabaseDiscoveryProviderAlgorithm                        | 基于 MySQL MGR 的数据库发现算法       |
| MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm     | 基于 MySQL 主从同步的数据库发现算法     |
| OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm | 基于 openGauss 主从同步的数据库发现算法 |
