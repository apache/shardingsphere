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

## JobRateLimitAlgorithm

| *SPI Name*                                   | *Description*                              |
| ------------------------------------------- | ------------------------------------------- |
| JobRateLimitAlgorithm                       | job rate limit algorithm                    |

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| SourceJobRateLimitAlgorithm                 | rate limit algorithm for source side        |

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

| *Implementation Class*                      | *Description*                               |
| ------------------------------------------- | ------------------------------------------- |
| DataMatchDataConsistencyCheckAlgorithm      | Records content match implementation        |
| CRC32MatchDataConsistencyCheckAlgorithm     | Records CRC32 match implementation          |
