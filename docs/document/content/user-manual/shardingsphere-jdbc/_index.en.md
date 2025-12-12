+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

Configuration is the only module in ShardingSphere-JDBC that interacts with application developers,
through which developers can quickly and clearly understand the functions provided by ShardingSphere-JDBC.

This chapter is a configuration manual for ShardingSphere-JDBC, which can also be referred to as a dictionary if necessary.

ShardingSphere-JDBC has provided 2 kinds of configuration methods for different situations.
By configuration, application developers can flexibly use data sharding, readwrite-splitting, data encryption, shadow database or the combination of them.

Mixed rule configurations are very similar to single rule configuration, except for the differences from single rule to multiple rules.

It should be noted that the superposition between rules are data source and table name related.
If the previous rule is data source oriented aggregation, the next rule needs to use the aggregated logical data source name configured by the previous rule when configuring the data source;
Similarly, if the previous rule is table oriented aggregation, the next rule needs to use the aggregated logical table name configured by the previous rule when configuring the table.

Please refer to [Example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example-generator) for more details.

> **Note**: When using ShardingSphere-JDBC adapter, pay attention to your application's memory configuration. Antlr uses an internal cache to improve performance during SQL parsing. If your application has too many SQL templates, the cache will continue to grow, occupying a large amount of heap memory.
According to feedback from the ANTLR official [issue#4232](https://github.com/antlr/antlr4/issues/4232), this issue has not yet been optimized. When connecting your application to ShardingSphere-JDBC, it is recommended to set a reasonable heap memory size using the `-Xmx` parameter to avoid OOM errors caused by insufficient memory.
