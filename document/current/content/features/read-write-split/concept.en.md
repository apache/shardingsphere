+++
pre = "<b>3.2.1. </b>"
toc = true
title = "Core Concept"
weight = 1
+++

## Master

Database that deal with insert, update and delete operations. support single master only.

## Slave

Database that deal with query operations. support multiple slaves.

## Master-slave replication

Replicate data from master to slaves asynchronously. Because of asynchronized, data of master and slaves may inconsistent on short time.  

## Load balance strategy

Redirect query operations to different slaves via load balance strategy.

## Config Map

ConfigMap allows user to configure metadata information for data source of read-write splitting. The information of masterSlaveConfig in ConfigMap can be obtained by calling ConfigMapContext.getInstance (). e.g. Different weight for machines, different traffic on machines. The metadata for machines' weight can be configured through the ConfigMap.
