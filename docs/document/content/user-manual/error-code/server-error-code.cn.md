+++
title = "服务器错误码"
weight = 2
chapter = true
+++

服务器发生错误时所提供的唯一错误码，打印在 Proxy 后端或 JDBC 启动日志中。

| 错误码                 | 错误信息 |
| --------------------- | ------ |
| SPI-00001             | No implementation class load from SPI \`%s\` with type \`%s\`. |
| DATA-SOURCE-00001     | Data source unavailable. |
| PROPS-00001           | Value \`%s\` of \`%s\` cannot convert to type \`%s\`. |
| PROXY-00001           | Load database server info failed. |
| SPRING-00001          | Can not find JNDI data source. |
| SPRING-SHARDING-00001 | Can not support type \`%s\`. |
