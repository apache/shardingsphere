+++
title = "Config Center"
weight = 1
+++

## Motivation

- Centralized configuration: more and more running examples have made it hard to manage separate configurations and asynchronized configurations can cause serious problems. Concentrating them in the configuration center can make the management more effective.

- Dynamic configuration: distribution after configuration modification is another important capability of configuration center. It can support dynamic switch between data sources and rule configurations.

## Structure in Configuration Center

Under defined namespace `config` node, configuration center stores data sources, rule configurations, authentication configuration, and properties in YAML. Modifying nodes can dynamically refresh configurations.

```
config
    ├──authentication                            # Authentication configuration
    ├──props                                     # Properties configuration
    ├──schema                                    # Schema configuration
    ├      ├──schema_1                           # Schema name 1
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Rule configuration
    ├      ├──schema_2                           # Schema name 2
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Rule configuration
```

### config/authentication

Authentication configuration. Can configure username and password for ShardingSphere-Proxy.

```yaml
username: root
password: root
```

### config/props

Properties configuration. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/configuration/) for more details.

```yaml
executor.size: 20
sql.show: true
```

### config/schema/schemeName/datasource

A collection of multiple database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by users themselves.

```yaml
ds_0: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
ds_1: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
```

### config/schema/sharding_db/rule

Rule configurations, including sharding, read-write split, data encryption, shadow DB, multi replica configurations.

```yaml
rules:
- !SHARDING
  xxx
  
- !MASTERSLAVE
  xxx
  
- !ENCRYPT
  xxx
```

## Dynamic Effectiveness

Modification, deletion and insertion of relevant configurations in the registry center will immediately take effect in the producing environment.
