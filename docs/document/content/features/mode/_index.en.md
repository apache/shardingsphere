+++
pre = "<b>3.9. </b>"
title = "mode"
weight = 9
chapter = true
+++

## Background

To meet different user needs, such as quick test, stand-alone operation and distributed operation mode provides three modes, 
which are: Memory mode, Standalone mode, and Cluster mode.

## Memory mode

The memory mode is more suitable for fast integration testing, which is convenient for testing, such as for operations and 
maintenance developers looking to perform fast integration function testing. This mode is also Apache ShardingSphere’s default mode.

## Standalone mode

The standalone mode is more suitable for use in a standalone environment, through which data sources, rules, and metadata can be persisted. 
Among them, the File property in Standalone mode will write the configuration information to the Path you specify. If the Path attribute is not set, 
then Apache ShardingSphere will create a .shardingsphere file in the root directory to store configuration information.

## Cluster mode

The cluster mode is more suitable for use in distributed scenarios. Cluster mode provides functions such as sharing metadata between multiple instances, 
node status synchronization, and dynamic adjustment rules through Dist SQL. Regarding distributed governance, 
please [click here](https://shardingsphere.apache.org/document/current/cn/features/governance/)
