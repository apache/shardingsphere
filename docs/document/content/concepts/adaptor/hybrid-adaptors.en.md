+++
pre = "<b>3.1.3. </b>"
title = "Hybrid Adaptors"
weight = 3
+++

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java.
ShardingSphere-Proxy provides static entry and all languages support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosystem consisting of multiple endpoints together.
Through a mixed use of ShardingSphere-JDBC and ShardingSphere-Proxy and a unified sharding strategy by the same registry center, ShardingSphere can build an application system that is applicable to all kinds of scenarios.
Architects can adjust the system architecture to the most applicable one to their needs to conduct business more freely.

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)
