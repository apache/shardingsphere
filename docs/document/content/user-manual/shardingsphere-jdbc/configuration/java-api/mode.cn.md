+++
title = "Mode"
weight = 5
+++

## 配置项说明

### Memory 模式

*配置入口*

类名称：org.apache.shardingsphere.infra.config.mode.ModeConfiguration

可配置属性：

| *名称*                           | *数据类型*                    | *说明*                                                 |
| ---------------------------     | --------------------------- | ------------------------------------------------------ |
| type (?)                        | String                      | Memory                                                 |

### Standalone 模式

*配置入口*

类名称：org.apache.shardingsphere.infra.config.mode.ModeConfiguration

| *名称*                       | *数据类型*                      | *说明*                                                       |
| --------------------------- | -------------------------------| ------------------------------------------------------------ |
| type                        | String                         | Standalone                                                   |
| repository                  | PersistRepositoryConfiguration | 配置 StandalonePersistRepositoryConfiguration                 |
| overwrite                   | boolean                        | 本地配置是否覆盖文件配置，如果可覆盖，每次启动都以本地配置为准        |

*StandalonePersistRepositoryConfiguration配置*

类名称：org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

可配置属性：

| *名称*         | *数据类型* | *说明*                                                                    |
| ------------- | ---------- | -----------------------------------------------------------------------  |
| type          | String     | Standalone 配置持久化类型。如：File                                         |
| props (?)     | Properties | Standalone 配置持久化的属性 如：path 路径                                   |

Standalone 属性配置：

| *名称*                            | *数据类型* | *说明*                | *默认值*             |
| -------------------------------- | --------- | -------------------- | -------------------- |
| path                            | String    | 配置信息存储路径       | .shardingsphere 目录  |

### Cluster 模式

*配置入口*

类名称：org.apache.shardingsphere.infra.config.mode.ModeConfiguration

可配置属性：

| *名称*                       | *数据类型*                    | *说明*                                                 |
| --------------------------- | --------------------------- | ------------------------------------------------------ |
| type                        | String                      | Cluster                                                |
| repository                  | RegistryCenterConfiguration | 配置 Cluster ClusterPersistRepositoryConfiguration       |
| overwrite                   | boolean                     | 本地配置是否覆盖配置中心配置，如果可覆盖，每次启动都以本地配置为准 |

*ClusterPersistRepositoryConfiguration配置*

类名称：org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

可配置属性：

| *名称*         | *数据类型* | *说明*                                                                                   |
| ------------- | ---------- | -------------------------------------------------------------------------------------- |
| type          | String     | Cluster mode 类型，如：Zookeeper, Etcd                                                  |
| namespace     | String     | Cluster mode 实例命名空间 如：cluster-sharding-mode                                      |
| server-lists  | String     | Zookeeper / Etcd服务列表，包括 IP 地址和端口号，多个地址用逗号分隔，如: host1:2181,host2:2181 |
| props         | Properties | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                                        |

ZooKeeper 属性配置

| *名称*                            | *数据类型* | *说明*                | *默认值* |
| -------------------------------- | --------- | -------------------- | ------- |
| digest (?)                       | String    | 连接注册中心的权限令牌   | 无需验证  |
| operationTimeoutMilliseconds (?) | int       | 操作超时的毫秒数        | 500 毫秒 |
| maxRetries (?)                   | int       | 连接失败后的最大重试次数  | 3 次    |
| retryIntervalMilliseconds (?)    | int       | 重试间隔毫秒数          | 500 毫秒 |
| timeToLiveSeconds (?)            | int       | 临时节点存活秒数        | 60 秒    |

Etcd 属性配置

| *名称*                 | *数据类型* | *说明*     | *默认值* |
| --------------------- | --------- | ---------- | ------ |
| timeToLiveSeconds (?) | long      | 数据存活秒数 | 30秒    |
