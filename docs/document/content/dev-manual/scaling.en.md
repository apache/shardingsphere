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
| OpenGaussScalingEntry  | openGauss entry of scaling  |

## JobCompletionDetectAlgorithm

| *SPI Name*                                  | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| JobCompletionDetectAlgorithm                | Job completion check algorithm              |

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| IdleRuleAlteredJobCompletionDetectAlgorithm | Incremental task idle time based algorithm  |

## SingleTableDataCalculator

| *SPI Name*                                  | *Description*                                           |
| ------------------------------------------- | ------------------------------------------------------- |
| SingleTableDataCalculator                   | Single table data calculator for data consistency check |

| *Implementation Class*                      | *Description*                                        |
| ------------------------------------------- | ---------------------------------------------------- |
| DataMatchSingleTableDataCalculator          | Check data consistency with every recodes one by one |
| CRC32MatchMySQLSingleTableDataCalculator    | Use CRC32 to check data consistency                  |
