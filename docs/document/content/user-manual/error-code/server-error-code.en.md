+++
title = "Server Error Code"
weight = 2
chapter = true
+++

Unique codes provided when server exception occur, which printed by Proxy backend or JDBC startup logs.

| Error Code        | Reason                                                     |
|-------------------|------------------------------------------------------------|
| SPI-00001         | No implementation class load from SPI '%s' with type '%s'. |
| DATA-SOURCE-00001 | Data source '%s' is unavailable.                           |
| PROPS-00001       | Properties convert failed, details are: %s.                |
| PROXY-00001       | Load database server info failed.                          |
