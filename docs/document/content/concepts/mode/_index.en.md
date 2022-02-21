+++
pre = "<b>3.2. </b>"
title = "Mode"
weight = 2
chapter = true
+++

## Background

In order to meet the different needs of users for quick test startup, stand-alone running and cluster running, 
Apache shardingsphere provides various mode such as memory, stand-alone and cluster.

## Memory mode

Suitable for fast integration testing, which is convenient for testing, such as for developers looking to perform fast integration function testing. 
This is the default mode of Apache ShardingSphere.

## Standalone mode

Suitable in a standalone environment, through which data sources, rules, and metadata can be persisted. 
Will create a `.shardingsphere` file in the root directory to store configuration data by default.

## Cluster mode

Suitable for use in distributed scenarios which provides metadata sharing and state coordination among multiple computing nodes.
It is necessary to provide registry center for distributed coordination, such as ZooKeeper or Etcd.
