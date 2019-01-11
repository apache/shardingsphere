+++
pre = "<b>3.2.1. </b>"
toc = true
title = "Core Concept"
weight = 1
+++

## Master Database

It refers to the database used in data insert, update and delete operations. Only support single master database for now.

## Slave Database

It refers to the database used in data query. Support multiple slave databases.

## Master-slave replication

It refers to the operation to asynchronously replicate the data from the master database to the slave database. 
Because of master-slave asynchronization between the master database and the slave database, there may be short-time data inconsistency between the master database and the slave database.  

## Load Balance Strategy

Through this strategy, queries are separated to different slave databases.

## Config Map

To configure read-write split meta-data, users can acquire masterSlaveConfig data in ConfigMap by calling for `ConfigMapContext.getInstance()`. 
For example, machines of different weights can have different traffic. 
ConfigMap can be used to configure machine weight meta-data.
