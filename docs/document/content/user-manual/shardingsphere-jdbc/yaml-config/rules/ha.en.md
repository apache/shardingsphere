+++
title = "HA"
weight = 4
+++

## Background

Through `YAML` format, ShardingSphere will automatically create the `ShardingSphereDataSource` object according to the YAML configuration, reducing unnecessary coding work for users.

## Parameters

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    replica_ds:
      dynamicStrategy:
        autoAwareDataSourceName: # High availability rule logical data source name

- !DB_DISCOVERY
  dataSources:
    <data-source-name> (+): # Logic data source name
      dataSourceNames: # Data source names
        - <data-source>
        - <data-source>
      discoveryHeartbeatName: # Detect heartbeat name
      discoveryTypeName: # Database discovery type name
  
  # Heartbeat Configuration
  discoveryHeartbeats:
    <discovery-heartbeat-name> (+): # heartbeat name
      props:
        keep-alive-cron: # This is cron expression, such as：'0/5 * * * * ?'
  
  # Database Discovery Configuration
  discoveryTypes:
    <discovery-type-name> (+): # Database discovery type name
      type: # Database discovery type, such as: MySQL.MGR
      props (?):
        group-name: 92504d5b-6dec-11e8-91ea-246e9612aaf1 # Required parameters for database discovery types, such as MGR's group-name
```

## Sample

```yaml
databaseName: database_discovery_db

dataSources:
  ds_0:
    url: jdbc:mysql://127.0.0.1:33306/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_1:
    url: jdbc:mysql://127.0.0.1:33307/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_2:
    url: jdbc:mysql://127.0.0.1:33308/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1

rules:
  - !READWRITE_SPLITTING
    dataSources:
      replica_ds:
        dynamicStrategy:
          autoAwareDataSourceName: readwrite_ds
  - !DB_DISCOVERY
    dataSources:
      readwrite_ds:
        dataSourceNames:
          - ds_0
          - ds_1
          - ds_2
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
    discoveryHeartbeats:
      mgr-heartbeat:
        props:
          keep-alive-cron: '0/5 * * * * ?'
    discoveryTypes:
      mgr:
        type: MySQL.MGR
        props:
          group-name: 558edd3c-02ec-11ea-9bb3-080027e39bd2
```
## Related References

- [Feature Description of HA](/en/features/ha/)
- [JAVA API: HA](/en/user-manual/shardingsphere-jdbc/java-api/rules/ha/)
- [Spring Boot Starter: HA](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)
- [Spring Namespace: HA](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha/)
