+++
pre = "<b>6.10. </b>"
title = "Scaling"
weight = 10
chapter = true
+++

## ScalingEntry

| *SPI Name*             | *Description*               |
| ---------------------- | --------------------------- |
| ScalingEntry           | Entry of scaling            |

| *Implementation Class* | *Description*               |
| ---------------------- | --------------------------- |
| MySQLScalingEntry      | MySQL entry of scaling      |
| PostgreSQLScalingEntry | PostgreSQL entry of scaling |

## ScalingClusterAutoSwitchAlgorithm

| *SPI Name*                                  | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| ScalingClusterAutoSwitchAlgorithm           | Scaling job completion check algorithm      |

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| ScalingIdleClusterAutoSwitchAlgorithm       | Incremental task idle time based algorithm  |

## ScalingDataConsistencyCheckAlgorithm

| *SPI Name*                                  | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| ScalingDataConsistencyCheckAlgorithm        | Data consistency check algorithm on source and target database cluster |

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| ScalingDefaultDataConsistencyCheckAlgorithm | Default implementation with CRC32 of all records. |
