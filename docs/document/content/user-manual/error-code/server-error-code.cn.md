+++
title = "服务器错误码"
weight = 2
chapter = true
+++

服务器发生错误时所提供的唯一错误码，打印在 Proxy 后端或 JDBC 启动日志中。

| 错误码               | 错误信息                                                       |
|-------------------|------------------------------------------------------------|
| SPI-00001         | No implementation class load from SPI '%s' with type '%s'. |
| DATA-SOURCE-00001 | Data source '%s' is unavailable.                           |
| PROPS-00001       | Properties convert failed, details are: %s.                |
| PROXY-00001       | Load database server info failed.                          |
