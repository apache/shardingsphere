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

## DataConsistencyCalculateAlgorithm

| *SPI Name*                                  | *Description*                                        |
| ------------------------------------------- | ---------------------------------------------------- |
| DataConsistencyCalculateAlgorithm           | Check data consistency algorithm                     |

| *Implementation Class*                      | *Description*                                        |
| ------------------------------------------- | ---------------------------------------------------- |
| DataMatchDataConsistencyCalculateAlgorithm  | Check data consistency with every recodes one by one |
| CRC32MatchDataConsistencyCalculateAlgorithm | Use CRC32 to check data consistency                  |
