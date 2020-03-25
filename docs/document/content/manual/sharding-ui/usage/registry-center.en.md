+++
toc = true
title = "Registry Center"
weight = 2
+++

## Registry Center Configuration

The registry center needs to be added and activated first. Multiple registries can be added, but only one is active, and the following configuration management and orchestration operate on the currently active registry.
Zookeeper support is provided now, and the support for other registries will be added later.

![Registry Center](https://shardingsphere.apache.org/document/current/img/sharding-ui/registry-center.png)

## Configuration Management

The configuration of all data sources in the current active registry can be obtained, including data sharding, read-write split, properties, and so on.

![Configuration Management](https://shardingsphere.apache.org/document/current/img/sharding-ui/config-management.png)

The configuration can be modified by the YAML format.

![Configuration ditor](https://shardingsphere.apache.org/document/current/img/sharding-ui/config-edit.png)

## Orchestration

Registry center can disable the access to slave database and the access of application.

### Circuit Breaker

Users can disable or enable the instance.

![Circuit Breaker](https://shardingsphere.apache.org/document/current/img/sharding-ui/circuit-breaker.png)

### Disable Slave Database

Users can disable or enable the access to slave database.

![Disable Slave Database](https://shardingsphere.apache.org/document/current/img/sharding-ui/disable-slave-database.png)