+++
title = "高可用"
weight = 4
+++

## 背景信息

通过 `Java API` 方式构建高可用规则配置。

## 参数解释

### 配置入口

类名称：org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration
可配置属性：

| *名称*                   | *数据类型*                                                  | *说明*           |
| ----------------------- | ---------------------------------------------------------- | --------------- |
| dataSources (+)         | Collection\<DatabaseDiscoveryDataSourceRuleConfiguration\> | 数据源配置        |
| discoveryHeartbeats (+) | Map\<String, DatabaseDiscoveryHeartBeatConfiguration\>     | 监听心跳配置      |
| discoveryTypes (+)      | Map\<String, AlgorithmConfiguration\>                      | 数据库发现类型配置 |

### 数据源配置

类名称：org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*             | *说明*                                     |
| -------------------------- | -------------------- | ----------------------------------------- |
| groupName (+)              | String               | 数据库发现组名称                             |
| dataSourceNames (+)        | Collection\<String\> | 数据源名称，多个数据源用逗号分隔 如：ds_0, ds_1  |
| discoveryHeartbeatName (+) | String               | 监听心跳名称                                |
| discoveryTypeName (+)      | String               | 数据库发现类型名称                           |

### 监听心跳配置

类名称：org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration

可配置属性：

| *名称*     | *数据类型*  | *说明*                                                                | *默认值* |
| --------- | ---------- | -------------------------------------------------------------------- | ------- |
| props (+) | Properties | 监听心跳属性配置，keep-alive-cron 属性配置 cron 表达式，如：'0/5 * * * * ?' | -       |

### 数据库发现类型配置

类名称：org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

| *名称*     | *数据类型*  | *说明*                                       |
| --------- | ---------- | ------------------------------------------- |
| type (+)  | String     | 数据库发现类型，如：MySQL.MGR                   |
| props (?) | Properties | 数据库发现类型配置，如 MGR 的 group-name 属性配置 |

## 操作步骤

1. 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
> 注意：请将 `${latest.release.version}` 更改为实际的版本号。
> 
## 配置示例

```java

// 构建数据源 ds_0，ds_1，ds_2
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
## 相关参考

- [高可用核心特性](/cn/features/ha/)
- [YAML 配置：高可用配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/ha/)
- [Spring Boot Starter：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)
- [Spring 命名空间：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha/)
