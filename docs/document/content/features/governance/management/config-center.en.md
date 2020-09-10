+++
title = "Config Center"
weight = 1
+++

## Motivation

- Centralized configuration: more and more running examples have made it hard to manage separate configurations and asynchronized configurations can cause serious problems. Concentrating them in the configuration center can make the management more effective.

- Dynamic configuration: distribution after configuration modification is another important capability of configuration center. It can support dynamic switch between data sources and rule configurations.

## Structure in Configuration Center

Under defined namespace, configuration center stores data sources, rule configurations, authentication configuration, and properties in YAML. Modifying nodes can dynamically refresh configurations.

```
namespace
    ├──authentication                            # Authentication configuration
    ├──props                                     # Properties configuration
    ├──schemas                                   # Schema configuration
    ├      ├──${schema_1}                        # Schema name 1
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Rule configuration
    ├      ├      ├──table                       # Table configuration
    ├      ├──${schema_2}                        # Schema name 2
    ├      ├      ├──datasource                  # Datasource configuration
    ├      ├      ├──rule                        # Rule configuration
    ├      ├      ├──table                       # Table configuration
```

### /authentication

Authentication configuration. Can configure username and password for ShardingSphere-Proxy.

```yaml
username: root
password: root
```

### /props

Properties configuration. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/configuration/) for more details.

```yaml
executor.size: 20
sql.show: true
```

### /schemas/${schemeName}/datasource

A collection of multiple database connection pools, whose properties (e.g. DBCP, C3P0, Druid and HikariCP) are configured by users themselves.

```yaml
dataSources:
  ds_0: 
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
  ds_1: 
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

### /schemas/${schemeName}/rule

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

### /schemas/${schemeName}/table

Dynamic modification of metadata content is not supported currently.

```yaml
configuredSchemaMetaData:                       # Tables of configured with sharding rules
  tables:                                       # Tables
    t_order:                                    # table_name
      columns:                                  # Columns
        id:                                     # column_name
          caseSensitive: false
          dataType: 0
          generated: false
          name: id
          primaryKey: trues
        order_id:
          caseSensitive: false
          dataType: 0
          generated: false
          name: order_id
          primaryKey: false
      indexs:                                   # Indexes
        t_user_order_id_index:                  # index_name
          name: t_user_order_id_index
    t_order_item:
      columns:
        order_id:
          caseSensitive: false
          dataType: 0
          generated: false
          name: order_id
          primaryKey: false
unconfiguredSchemaMetaDataMap:                  # Tables of no sharding rules configured
  ds_0:                                         # DataSources
    tables:                                     # Tables
      t_user:                                   # table_name
        columns:                                # Columns
          user_id:                              # column_name
            caseSensitive: false
            dataType: 0
            generated: false
            name: user_id
            primaryKey: false
          id:
            caseSensitive: false
            dataType: 0
            generated: false
            name: id
            primaryKey: true
          order_id:
            caseSensitive: false
            dataType: 0
            generated: false
            name: order_id
            primaryKey: false
        indexes:                                # Indexes
          t_user_order_id_index:                # index_name
            name: t_user_order_id_index
          t_user_user_id_index:
            name: t_user_user_id_index
          primary:
            name: PRIMARY
```

## Dynamic Effectiveness

Modification, deletion and insertion of relevant configurations in the config center will immediately take effect in the producing environment.
