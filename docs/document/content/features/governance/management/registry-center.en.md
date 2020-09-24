+++
title = "Registry Center"
weight = 2
+++

## Motivation

- As config center manage configuration data, registry center hold all ephemeral status data dynamically generated in runtime(such as available proxy instances, disabled datasource instances etc).

- Registry center can disable the access to slave database and the access of application. Governance still has many functions(such as flow control) to be developed.

## Data Structure in Registry Center

The registry center can create running node of database access object under `states` in defined namespace, to distinguish different database access instances, including `proxynodes` and `datanodes` nodes.

```
namespace
   ├──states
   ├    ├──proxynodes
   ├    ├     ├──${your_instance_ip_a}@${your_instance_pid_x}@${UUID}
   ├    ├     ├──${your_instance_ip_b}@${your_instance_pid_y}@${UUID}
   ├    ├     ├──....
   ├    ├──datanodes
   ├    ├     ├──${schema_1}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──${schema_2}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──....
```

### /states/proxynodes

It includes running instance information of database access object, with sub-nodes as the identifiers of currently running instance, which consist of IP and PID. Those identifiers are temporary nodes, which are registered when instances are on-line and cleared when instances are off-line. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### /states/datanodes

It is able to orchestrate read-write split slave database, delete or disable data dynamically.

## Operation Guide

### Circuit Breaker

Write `DISABLED` (case insensitive) to `IP@PID@UUID` to disable that instance; delete `DISABLED` to enable the instance.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/states/proxynodes/${your_instance_ip_a}@${your_instance_pid_x}@${UUID} DISABLED
```

### Disable Slave Database

Under read-write split scenarios, users can write `DISABLED` (case insensitive) to sub-nodes of data source name to disable slave database sources. Delete `DISABLED` or the node to enable it.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/states/datanodes/${your_schema_name}/${your_replica_datasource_name} DISABLED
```
