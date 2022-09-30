+++
title = "模式配置"
weight = 1
chapter = true
+++

## 背景信息

通过 Java API 方式构建运行模式。

## 参数解释

类名称：org.apache.shardingsphere.infra.config.mode.ModeConfiguration

可配置属性：

| *名称*      | *数据类型*                      | *说明*                                                                                                                                  | *默认值*     |
| ---------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| type       | String                         | 运行模式类型<br />可选配置：Standalone、Cluster                                                                                             | Standalone |
| repository | PersistRepositoryConfiguration | 持久化仓库配置<br />Standalone 类型使用 StandalonePersistRepositoryConfiguration<br />Cluster 类型使用 ClusterPersistRepositoryConfiguration |            | | false      |

### Standalone 持久化配置

类名称：org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

可配置属性：

| *名称* | *数据类型*  | *说明*           |
| ----- | ---------- | --------------- |
| type  | String     | 持久化仓库类型    |
| props | Properties | 持久化仓库所需属性 |

### Cluster 持久化配置

类名称：org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

可配置属性：

| *名称*       | *数据类型*  | *说明*           |
| ----------- | ---------- | --------------- |
| type        | String     | 持久化仓库类型     |
| namespace   | String     | 注册中心命名空间   |
| server-lists | String     | 注册中心连接地址   |
| props       | Properties | 持久化仓库所需属性 |

## 注意事项

1. 生产环境建议使用集群模式部署。
2. 集群模式部署推荐使用 `ZooKeeper` 注册中心。

## 操作步骤

### 引入Maven 依赖。

```xml
<dependency>
 <groupId>org.apache.shardingsphere</groupId>
 <artifactId>shardingsphere-jdbc-core</artifactId>
 <version>${latest.release.version}</version>
</dependency>
```

> 注意：请将 `${latest.release.version}` 更改为实际的版本号。

## 配置示例

### Standalone 运行模式

```java
ModeConfiguration modeConfig = createModeConfiguration();
Map<String, DataSource> dataSourceMap = ... // 构建真实数据源
Collection<RuleConfiguration> ruleConfigs = ... // 构建具体规则
Properties props = ... // 构建属性配置
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);

private ModeConfiguration createModeConfiguration() {
    return new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("JDBC", new Properties()));
}
```

### Cluster 运行模式 (推荐)

```java
ModeConfiguration modeConfig = createModeConfiguration();
Map<String, DataSource> dataSourceMap = ... // 构建真实数据源
Collection<RuleConfiguration> ruleConfigs = ... // 构建具体规则
Properties props = ... // 构建属性配置
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);

private ModeConfiguration createModeConfiguration() {
    return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "governance-sharding-db", "localhost:2181", new Properties()));
}
```

## 相关参考

- [ZooKeeper 注册中心安装与使用](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- 持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)。
