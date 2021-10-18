+++
pre = "<b>4.9. </b>"
title = "mode"
weight = 9
chapter = true
+++

## Background

To meet different user needs, such as quick test, stand-alone operation and distributed operation mode provides three modes, 
which are: Memory mode, Standalone mode, and Cluster mode.

## Memory mode

The memory mode is suitable for fast integration testing, which is convenient for testing, such as for developers looking to perform fast integration function testing. 
This mode is also Apache ShardingSphere’s default mode.

## Standalone mode

The standalone mode is suitable in a standalone environment, through which data sources, rules, and metadata can be persisted. 
Among them, the File type in Standalone mode will write the configuration information to the Path you specify. If the Path attribute is not set, 
then Apache ShardingSphere will create a .shardingsphere file in the root directory to store configuration information.

## Cluster mode

The cluster mode is suitable for use in distributed scenarios. Cluster mode provides metadata sharing and state coordination among multiple computing nodes.
