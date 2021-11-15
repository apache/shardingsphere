+++
title = "模式配置"
weight = 1
chapter = true
+++

## 配置入口

类名称：org.apache.shardingsphere.infra.config.mode.ModeConfiguration

可配置属性：

| *名称*      | *数据类型*                      | *说明*                                                                                                                                                                      | *默认值* |
| ---------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| type       | String                         | 运行模式类型<br />可选配置：Memory、Standalone、Cluster                                                                                                                         | Memory  |
| repository | PersistRepositoryConfiguration | 持久化仓库配置<br />Memory 类型无需持久化，可以为 null<br />Standalone 类型使用 StandalonePersistRepositoryConfiguration<br />Cluster 类型使用 ClusterPersistRepositoryConfiguration |         |
| overwrite  | boolean                        | 是否使用本地配置覆盖持久化配置                                                                                                                                                   | false   |

## Standalone 持久化配置

类名称：org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

可配置属性：

| *名称* | *数据类型*  | *说明*           |
| ----- | ---------- | --------------- |
| type  | String     | 持久化仓库类型    |
| props | Properties | 持久化仓库所需属性 |

## Cluster 持久化配置

类名称：org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

可配置属性：

| *名称*       | *数据类型*  | *说明*           |
| ----------- | ---------- | --------------- |
| type        | String     | 持久化仓库类型     |
| namespace   | String     | 注册中心命名空间   |
| serverLists | String     | 注册中心连接地址   |
| props       | Properties | 持久化仓库所需属性 |

持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/)。
