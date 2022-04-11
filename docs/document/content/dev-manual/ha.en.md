+++
pre = "<b>6.9. </b>"
title = "HA"
weight = 9
chapter = true
+++

## DatabaseDiscoveryType

| *SPI Name*                            | *Description*                                           |
| ------------------------------------- | ------------------------------------------------------- |
| DatabaseDiscoveryType                 | Database discovery type                                 |

| *Implementation Class*                | *Description*                                           |
| ------------------------------------- | ------------------------------------------------------- |
| MGRDatabaseDiscoveryType              | Database discovery of MySQL's MGR                       |
| ShowSlaveStatusDatabaseDiscoveryType  | Database discovery of MySQL's master-slave delay        |
| OpenGaussDatabaseDiscoveryType        | Database discovery of openGauss                         |
