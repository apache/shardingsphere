+++
pre = "<b>4.4. </b>"
title = "错误码"
weight = 4
chapter = true
+++

本章列举所有的 Apache ShardingSphere 错误码，错误码以标准的 SQL State，Vendor Code 和详细错误信息提供。

| SQL State | Vendor Code | 错误信息 |
| --------- | ----------- | ------ |
| 01000     | 10000       | Circuit break open, the request has been ignored |
| 08000     | 10001       | The URL \`%s\` is not recognized, please refer to the pattern \`%s\` |
| 42000     | 11000       | You have an error in your SQL syntax: %s |
| 42000     | 11001       | configuration error |
| 42000     | 11002       | Resource does not exist |
| 42000     | 11003       | Rule does not exist |
| 42000     | 12000       | Unsupported command: %s |
| 44000     | 13000       | SQL check failed, error message: %s |
| HY000     | 14000       | The table \`%s\` of schema \`%s\` is locked |
| HY000     | 14001       | The table \`%s\` of schema \`%s\` lock wait timeout of %s ms exceeded |
| HY000     | 15000       | Can not find pipeline job \`%s\` |
| HY004     | 25000       | Shadow column \`%s\` of table \`%s\` does not support \`%s\` type |
| 42000     | 30000       | Unknown exception: %s |
