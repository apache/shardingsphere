+++
pre = "<b>3.9. </b>"
title = "Pluggable Architecture"
weight = 9
chapter = true
+++

## Background

In order to satisfy the different needs of users, such as quick test, stand-alone operation and distributed operation.
The mode provides three modes, which are Memory mode, Standalone mode, and Cluster mode.

## Memory mode

The memory mode is more suitable for fast integration testing, which is convenient for testing, operation and maintenance 
and developers to do fast integration function testing. This mode is also the default mode of Apache ShardingSphere.

## Standalone mode

The standalone mode is more suitable for use in a standalone environment, through which data sources, rules, and metadata 
can be persisted. Among them, the File property in Standalone mode will write the configuration information to the Path you specify,
If the Path attribute is not set, then Apache ShardingSphere will create a .shardingsphere file in the root directory to store configuration information.

## Cluster mode

The cluster mode is more suitable for use in distributed scenarios. Cluster mode provides functions such as sharing metadata between multiple instances, 
synchronization of node status, and dynamic adjustment rules through Dist SQL. Regarding distributed governance, please [click here](https://shardingsphere.apache.org/document/current/cn/features/governance/)
