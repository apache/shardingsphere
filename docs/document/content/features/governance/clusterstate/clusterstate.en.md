+++
title = "Cluster State Topology"
weight = 2
+++

## Background

The cluster status topology is used to display the status of all nodes in the cluster and the communication between the nodes. ShardingSphere collects and stores heartbeat data in the registration center based on heartbeat detection to generate the cluster state topology map, which is used to more intuitively display the real-time status of each node in the cluster and the connection between application nodes and database nodes.

## Goal

- Real-time display of application node and database node state changes
- Real-time display of the connection status between application nodes and database nodes
- Database nodes, such as abnormal connection with more than a certain number of application nodes, alert and remind (update node status)

## Node Status Description

- ONLINE online
- OFFLINE offline
- DISABLED disabled
- UNKNOWN unknown

## Use

The cluster state topology is integrated in the user interface. For specific use, please refer to the ```ShardingSphere-UI``` project.