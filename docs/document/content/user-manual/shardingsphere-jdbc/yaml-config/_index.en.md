+++
title = "YAML Configuration"
weight = 1
chapter = true
+++

## Overview

YAML configuration provides interaction with ShardingSphere JDBC through configuration files.
When used with the governance module together, the configuration of persistence in the configuration center is YAML format.

Note:
The YAML configuration file supports more than 3MB of configuration content.

YAML configuration is the most common configuration mode, which can omit the complexity of programming and simplify user configuration.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### YAML Format

ShardingSphere-JDBC YAML file consists of database name, mode configuration, data source map, rule configurations and properties.

Note: The example connection pool is HikariCP, which can be replaced with other connection pools according to business scenarios.

```yaml
# JDBC logic database name. Through this parameter to connect ShardingSphere-JDBC and ShardingSphere-Proxy.
# Default value: logic_db
databaseName (?):

mode:

dataSources:

rules:
- !FOO_XXX
    ...
- !BAR_XXX
    ...

props:
  key_1: value_1
  key_2: value_2
```

Please refer to [Mode Confiugration](/en/user-manual/shardingsphere-jdbc/yaml-config/mode) for more mode details.

Please refer to [Data Source Confiugration](/en/user-manual/shardingsphere-jdbc/yaml-config/data-source) for more data source details.

Please refer to [Rules Confiugration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules) for more rule details.

### Create Data Source

The ShardingSphereDataSource created by YamlShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.

```java

File yamlFile = // Indicate YAML file
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### Use Data Source

Same with Java API.

## YAML Syntax Explanation

`!!` means instantiation of that class

`!` means self-defined alias

`-` means one or multiple can be included

`[]` means array, can substitutable with `-` each other
