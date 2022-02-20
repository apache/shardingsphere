+++
pre = "<b>6.11. </b>"
title = "Scaling"
weight = 11
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
| OpenGaussScalingEntry  | openGauss entry of scaling |

## JobCompletionDetectAlgorithm

| *SPI Name*                                  | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| JobCompletionDetectAlgorithm                | Job completion check algorithm              |

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| IdleRuleAlteredJobCompletionDetectAlgorithm | Incremental task idle time based algorithm  |

## DataConsistencyCheckAlgorithm

| *SPI Name*                                  | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| DataConsistencyCheckAlgorithm               | Data consistency check algorithm on source and target database cluster |

| *Implementation Class*                      | *Description*                                                          |
| ------------------------------------------- | ---------------------------------------------------------------------- |
| DataMatchDataConsistencyCheckAlgorithm      | Records content match implementation. Type name: DATA_MATCH.           |
| CRC32MatchDataConsistencyCheckAlgorithm     | Records CRC32 match implementation. Type name: CRC32_MATCH.            |

## SingleTableDataCalculator

| *SPI Name*                                  | *Description*                                                          |
| ------------------------------------------- | ---------------------------------------------------------------------- |
| SingleTableDataCalculator                   | Single table data calculator for data consistency check                |

| *Implementation Class*                      | *Description*                                                          |
| ------------------------------------------- | ---------------------------------------------------------------------- |
| DataMatchSingleTableDataCalculator          | Single table data calculator for DATA_MATCH data consistency check     |
| CRC32MatchMySQLSingleTableDataCalculator    | Single table data calculator for CRC32_MATCH data consistency check    |
