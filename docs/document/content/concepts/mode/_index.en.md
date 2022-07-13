+++
pre = "<b>3.2. </b>"
title = "Mode"
weight = 2
chapter = true
+++

## Background

In order to meet the different needs of users for quick test startup, standalone running and cluster running, 
Apache shardingsphere provides various mode such as standalone and cluster.

## Standalone mode

Suitable in a standalone environment, through which data sources, rules, and metadata can be persisted. 
Will use H2 database to store configuration data by default.

## Cluster mode

Suitable for use in distributed scenarios which provides metadata sharing and state coordination among multiple computing nodes.
It is necessary to provide registry center for distributed coordination, such as ZooKeeper or Etcd.

**Source Codes: https://github.com/apache/shardingsphere/tree/master/shardingsphere-mode**
