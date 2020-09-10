+++
title = "Governance"
weight = 2
+++

ShardingSphere-Proxy use SPI to support [Governance](/en/features/governance/management/), realize the unified management of configurations and metadata, as well as instance disabling and slave disabling.

## Zookeeper

ShardingSphere-Proxy has provided the solution of Zookeeper in default, which implements the functions of config center, registry center.
[Configuration Rules](/en/user-manual/shardingsphere-jdbc/configuration/yaml/governance/) consistent with ShardingSphere-JDBC YAML.

## Other Third Party Components
Refer to [Supported Third Party Components](/en/features/governance/management/dependency/) for details.

1. Use SPI methods in logic coding and put the generated jar package to the lib folder of ShardingSphere-Proxy.
1. Follow [Configuration Rules](/en/user-manual/shardingsphere-jdbc/configuration/yaml/governance/) to configure and use it.
