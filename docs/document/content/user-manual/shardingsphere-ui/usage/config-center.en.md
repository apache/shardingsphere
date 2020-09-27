+++
title = "Config Center"
weight = 2
+++

## Config Center Configuration

The config center needs to be added and activated first. Multiple centers can be added, but only one is active, and the following rule config operate on the currently active config center.
Zookeeper support is provided now, and the support for other config centers will be added later.

## Rule Config

+ After added and activated a config center, the configuration of all data sources in the current active config center can be obtained, including data sharding, primary-replica replication, properties, and so on.

+ The configuration can be modified by the YAML format.

+ Click the + button to add a new data source and sharding rule.
