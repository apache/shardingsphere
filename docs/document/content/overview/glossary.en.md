+++
pre = "<b>1.2 </b>"
title = "Core Concepts"
weight = 2
chapter = true
+++

## Database Plus

Database Plus is the concept driving Apache ShardingSphere's project positioning, and it is designed to build a standard layer and ecosystem above heterogeneous databases. This concept focuses on how to maximize the original database computing and storage capabilities rather than creating a new database. Placed above databases, ShardingSphere focuses on enhancing databases' inter-compatibility and collaboration.

Connect, Enhance, and Pluggable are the core concepts of Database Plus:

### Connect

Through flexible adaptation to the database protocol, SQL dialect, and database storage, ShardingSphere can quickly connect applications and multi-model heterogeneous databases.

### Enhance

ShardingSphere can obtain databases' access traffic and provide transparent enhancement features such as traffic redirection (sharding, read/write splitting, and shadow DB), transformation (data encryption and masking), authentication (security, audit, and permission), governance (circuit breaker and traffic limit) and analysis (QoS and observability).

### Pluggable

The project adopts the micro-kernel and three-layer pluggable model, which enables the kernel, features, and database ecosystems to be flexibly expanded. Developers can customize their ShardingSphere just like building with LEGO blocks.

## Multi Operation Mode

Apache ShardingSphere is a complete set of products applicable to a wide range of usage scenarios. In addition to the cluster deployment of the production environment, it also provides corresponding operation modes for engineers in the development process and automated testing scenarios. Apache ShardingSphere provides three operation modes: standalone mode, and cluster mode.

Source code: https://github.com/apache/shardingsphere/tree/master/shardingsphere-mode

### Standalone mode

Initial configuration or metadata changes caused by SQL execution take effect only in the current process. It is ideal for engineers to build a ShardingSphere environment locally without cleaning the running traces. This is the default mode of Apache ShardingSphere.

### Cluster mode

It provides metadata sharing between multiple Apache ShardingSphere instances and the capability to coordinate states in distributed scenarios. In an actual production environment for deployment and release, you must use the cluster mode. 

It provides the capabilities necessary for distributed systems, such as horizontal scaling of computing capability and high availability. Clustered environments need to store metadata and coordinate nodes' status through a separately deployed registry center.
