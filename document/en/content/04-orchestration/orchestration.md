+++
toc = true
title = "Orchestration"
weight = 2
+++

## Motivation

Use registry center to provide circuit breaker and disable slave databases. Data orchestration still have lots of feature need to do.

## Data structure

Registry center is defined in the namespace under `state` node, and user can create the object running node to access the database, by which you can distinguish different accessing instances. There are instances and datasources nodes.

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....
datasources
    ├──ds_0
    ├──ds_1
    ├──....
```

### state/instances

It includes the running-instance information of database-accessing object, and its child node is the identity of the current running instance. This identify is composed of IP and PID in the running server and always a temporary node. It is registered when the instance is online, and automatically cleaned when the instance is offline. The registry manages the access to the database by monitoring changes in these nodes.

### state/datasource

It is used to manage Read-write splitting and dynamically add, remove or disable data sources.
