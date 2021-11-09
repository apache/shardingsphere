+++
pre = "<b>4.6. </b>"
title = "HA"
weight = 6
chapter = true
+++

## Background

High availability is the most basic requirement of modern systems.
As the cornerstone of the system, the database is also essential for high availability.

In the distributed database system with storage-compute splitting, the high availability solution of storage node and compute node are different.
The stateful storage nodes need to pay attention to data consistency, health detection, primary node election and so on;
The stateless compute nodes need to detect the changes of storage nodes, they also need to set up an independent load balancer and have the ability of service discovery and request distribution.

Apache ShardingSphere provides compute nodes and reuse database as storage nodes.
Therefore, the high availability solution it adopts is to use the high availability solution of the database itself as the high availability of the storage node, and detect the changes automatically.

## Challenges

Apache ShardingSphere needs to detect high availability solution of diversified storage nodes automatically,
and can also integrate the readwrite splitting dynamically, which is the main challenge of implementation.

## Goal

**The main goal of Apache ShardingSphere high availability module which is ensuring 7 * 24-hour uninterrupted database service as much as possible.**
