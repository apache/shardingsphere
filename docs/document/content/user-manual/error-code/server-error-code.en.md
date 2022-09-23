+++
title = "Server Error Code"
weight = 2
chapter = true
+++

Unique codes provided when server exception occur, which printed by Proxy backend or JDBC startup logs.

| Error Code            | Reason |
| --------------------- | ------ |
| SPI-00001             | No implementation class load from SPI \`%s\` with type \`%s\` |
| PROPS-00001           | Value \`%s\` of \`%s\` cannot convert to type \`%s\` |
| PROXY-00001           | Load database server info failed |
| SPRING-00001          | Can not find JNDI data source |
| SPRING-SHARDING-00001 | Can not support type \`%s\` |
