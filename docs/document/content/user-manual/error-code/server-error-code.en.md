+++
title = "Server Error Code"
weight = 2
chapter = true
+++

Unique codes provided when server exception occur, which printed by Proxy backend or JDBC startup logs.

| Error Code        | Reason |
| ----------------- | ------ |
| SPI-1             |  No implementation class load from SPI \`%s\` with type \`%s\` |
| PROPS-1           |  Value \`%s\` of \`%s\` cannot convert to type \`%s\` |
| PROXY-1           |  Load database server info failed |
| SPRING-1          |  Can not find JNDI data source |
| SPRING-SHARDING-1 |  Can not support type \`%s\` |
