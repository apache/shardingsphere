+++
pre = "<b>4.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
chapter = true
+++

Configuration is the only module in ShardingSphere-Proxy that interacts with application developers,
through which developer can quickly and clearly understand the functions provided by ShardingSphere-Proxy.

This chapter is a configuration manual for ShardingSphere-Proxy, which can also be referred to as a dictionary if necessary.

ShardingSphere-Proxy provided YAML configuration, and used DistSQL to communicate.
By configuration, application developers can flexibly use data sharding, readwrite-splitting, data encryption, shadow database or the combination of them.

Rule configuration keeps consist with YAML configuration of ShardingSphere-JDBC.
DistSQL and YAML can be replaced each other.

Please refer to [Example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-proxy-example) for more details.
