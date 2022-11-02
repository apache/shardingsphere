+++
pre = "<b>3.6. </b>"
title = "Traffic Governance"
weight = 6
chapter = true
+++

## Background

As the scale of data continues to expand, a distributed database has become a trend gradually.
The unified management ability of cluster perspective, and control ability of individual components are necessary ability in modern database system.

## Challenges

The challenge is ability which are unified management of centralized management, and  operation in case of single node in failure.

Centralized management is to uniformly manage the state of database storage nodes and middleware computing nodes, 
and can detect the latest updates in the distributed environment in real time, further provide information with control and scheduling.

In the overload traffic scenario, circuit breaker and request limiting for a node to ensure whole database cluster can run continuously is a challenge to control ability of a single node.

## Goal

The goal of Apache ShardingSphere management module is to realize the integrated management ability from database to computing node, and provide control ability for components in case of failure.

## Application Scenarios

### Overloaded compute node protection

When a compute node in a ShardingSphere cluster exceeds its load, the circuit breaker function is used to block the traffic to the compute node, to ensure that the whole cluster continues to provide stable services.

### Storage node traffic limit

In the read-write splitting scenario where a storage node responsible for the read traffic in a ShardingSphere cluster receives overloaded requests, the traffic limit function is used to block traffic from compute nodes to the storage node, to ensure normal response of the storage node cluster.
