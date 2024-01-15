+++
title = "Known Implementation"
weight = 4
chapter = true
+++

## Background Information

For the driver class of `org.apache.shardingsphere.driver.ShardingSphereDriver`,
by implementing the SPI of `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider`,
allows YAML configuration files to be fetched from multiple sources and File Systems and parsed into ShardingSphere.

After parsing and loading the YAML file into ShardingSphere's metadata,
The next behavior will be determined again through the relevant configuration of [Mode Configuration](../../../java-api/mode.cn.md). Discuss two situations:
1. ShardingSphere’s metadata does not exist in the metadata persistence warehouse, and local metadata will be stored in the metadata persistence warehouse.
2. Metadata of ShardingSphere that is different from local metadata already exists in the metadata persistence warehouse, and the local metadata will be overwritten by the metadata of the metadata persistence warehouse.

For the configuration of the metadata persistence warehouse, please refer to [Metadata Persistence Warehouse](../../../../common-config/builtin-algorithm/metadata-repository.cn.md).

## URL configuration

### Load configuration files from classpath
Load the JDBC URL of the config.yaml configuration file in classpath, identified by the `jdbc:shardingsphere:classpath:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is consistent with [YAML configuration](../../../yaml-config).

Example:
- `jdbc:shardingsphere:classpath:config.yaml`

### Load configuration file from absolute path
JDBC URL to load the config.yaml configuration file in an absolute path, identified by the `jdbc:shardingsphere:absolutepath:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is consistent with [YAML configuration](../../../yaml-config).

Example:
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`

### Other implementations
For details, please refer to https://github.com/apache/shardingsphere-plugin .
