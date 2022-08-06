+++
pre = "<b>1.5 </b>"
title = "Mode"
weight = 5
chapter = true
+++

Apache ShardingSphere is a complete set of products applicable to a wide range of usage scenarios. In addition to the cluster deployment of the production environment, it also provides corresponding operation modes for engineers in the development process and automated testing scenarios. Apache ShardingSphere provides two operation modes: standalone mode and cluster mode.

## Standalone mode

It can achieve data persistence in terms of metadata information such as data sources and rules, but it is not able to synchronize metadata to multiple Apache ShardingSphere instances or be aware of each other in a cluster environment. Updating metadata through one instance causes inconsistencies in other instances because they cannot get the latest metadata. It is ideal for engineers to build a ShardingSphere environment locally.

## Cluster mode

It provides metadata sharing between multiple Apache ShardingSphere instances and the capability to coordinate states in distributed scenarios. In an actual production environment for deployment and release, you must use the cluster mode. 

It provides the capabilities necessary for distributed systems, such as horizontal scaling of computing capability and high availability. Clustered environments need to store metadata and coordinate nodes' status through a separately deployed registry center.
