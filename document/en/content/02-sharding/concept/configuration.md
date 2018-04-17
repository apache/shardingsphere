+++
toc = true
title = "Configuration"
weight = 3
+++

## Sharding Rule

Main entrance of sharding configuration. Includes data sources configurations, tables configurations, binding tables configurations and master-slave configurations.

## Data Sources Configurations

Physical data sources list.

## Tables Configurations

Configure logic table, actual data nodes and sharding strategies.

## Sharding Strategies Configurations

Include database sharding strategy and table sharding strategy. API of them are same.

## Key Generator Strategy Configurations

Generate distribute unique primary keys by this.

## Config Map

ConfigMap allows you to configure metadata information for data source of Sharding or Read-write splitting. The information of shardingConfig and masterSlaveConfig in ConfigMap can be obtained by calling ConfigMapContext.getInstance (). e.g. Differet weight for machines, different traffic on machines. The metadata for machines' weight can be configured through the ConfigMap.
