+++
title = "HA"
weight = 4
+++

## Background

Build high availability rule configuration through `Java API`.

## Parameters

### Root Configuration

Class name: org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration
Attributes:

| *Name*                  | *Data Type*                                                | *Description*                         |
| ----------------------- | ---------------------------------------------------------- | ------------------------------------- |
| dataSources (+)         | Collection\<DatabaseDiscoveryDataSourceRuleConfiguration\> | Data source configuration             |
| discoveryHeartbeats (+) | Map\<String, DatabaseDiscoveryHeartBeatConfiguration\>     | Detect heartbeat configuration        |
| discoveryTypes (+)      | Map\<String, AlgorithmConfiguration\>                      | Database discovery type configuration |

### Data Source Configuration

Class name: org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration

Attributes:

| *Name*                     | *Data Type*          | *Description*                                                                           |
|----------------------------|----------------------|-----------------------------------------------------------------------------------------|
| groupName (+)              | String               | Database discovery group name                                                           |
| dataSourceNames (+)        | Collection\<String\> | Data source names, multiple data source names separated with comma. Such as: ds_0, ds_1 |
| discoveryHeartbeatName (+) | String               | Detect heartbeat name                                                                   |
| discoveryTypeName (+)      | String               | Database discovery type name                                                            |

### Detect Heartbeat Configuration

Class name: org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration

Attributes:

| *Name*    | *Data Type* | *Description*                                                                                                      |
| --------- | ----------- | ------------------------------------------------------------------------------------------------------------------ |
| props (+) | Properties  | Detect heartbeat attribute configuration, keep-alive-cron configuration, cron expression. Such as: `0/5 * * * * ?` |

### Database Discovery Type Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

Attributes：

| *Name*    | *Data Type* | *Description*                                                             |
| --------- | ----------- | ------------------------------------------------------------------------- |
| type (+)  | String      | Database discovery type, such as: MySQL.MGR                               |
| props (?) | Properties  | Required parameters for high-availability types, such as MGR’s group-name |

## Procedure

1. Import Maven dependency.

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> Notice: Please change `${latest.release.version}` to the actual version.

## Sample

```java

// Build data source ds_0, ds_1, ds_2
Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
dataSourceMap.put("ds_0", createDataSource1("primary_demo_ds"));
dataSourceMap.put("ds_1", createDataSource2("primary_demo_ds"));
dataSourceMap.put("ds_2", createDataSource3("primary_demo_ds"));

DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("database_discovery_db", dataSourceMap, Arrays.asList(createDatabaseDiscoveryConfiguration(), createReadwriteSplittingConfiguration()), null);

private static DatabaseDiscoveryRuleConfiguration createDatabaseDiscoveryConfiguration() {
    DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfiguration = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Arrays.asList("ds_0, ds_1, ds_2"), "mgr-heartbeat", "mgr");
    return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfiguration), createDiscoveryHeartbeats(), createDiscoveryTypes());
}

private static ReadwriteSplittingRuleConfiguration createReadwriteSplittingConfiguration() {
    ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration1 = new ReadwriteSplittingDataSourceRuleConfiguration("replica_ds", new DynamicReadwriteSplittingStrategyConfiguration("readwrite_ds", true), "");
    return new ReadwriteSplittingRuleConfiguration(Arrays.asList(dataSourceConfiguration1), Collections.emptyMap());
}

private static Map<String, AlgorithmConfiguration> createDiscoveryTypes() {
    Map<String, AlgorithmConfiguration> discoveryTypes = new HashMap<>(1， 1);
    Properties props = new Properties();
    props.put("group-name", "558edd3c-02ec-11ea-9bb3-080027e39bd2");
    discoveryTypes.put("mgr", new AlgorithmConfiguration("MGR", props));
    return discoveryTypes;
}

private static Map<String, DatabaseDiscoveryHeartBeatConfiguration> createDiscoveryHeartbeats() {
    Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartBeatConfiguration = new HashMap<>(1， 1);
    Properties props = new Properties();
    props.put("keep-alive-cron", "0/5 * * * * ?");
    discoveryHeartBeatConfiguration.put("mgr-heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(props));
    return discoveryHeartBeatConfiguration;
}
```
## Related References

- [Feature Description of HA](/en/features/ha/)
- [YAML Configuration: HA](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/ha/)
- [Spring Boot Starter: HA](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)
- [Spring Namespace: HA](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha/)
