+++
title = "元数据持久化仓库"
weight = 1
+++

## 背景信息

Apache ShardingSphere 为不同的运行模式提供了不同的元数据持久化方式，用户在配置运行模式的同时可以选择合适的方式来存储元数据。

## 参数解释

### 数据库持久化

类型：JDBC

适用模式：Standalone

可配置属性：

| *名称*                        | *数据类型* | *说明*            | *默认值*         |
| ---------------------------- | --------- | ----------------- | --------------- |
| provider                    | String      | 元数据存储类型     | H2              |
| jdbc_url                    | String      | JDBC URL         | jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL              |
| username                    | String      | 账号              | sa              |
| password                    | String      | 密码              |                 |


### ZooKeeper 持久化

类型：ZooKeeper

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*              | *默认值*       |
| ---------------------------- | --------- | ------------------ | ------------- |
| retryIntervalMilliseconds    | int       | 重试间隔毫秒数        | 500           |
| maxRetries                   | int       | 客户端连接最大重试次数  | 3             |
| timeToLiveSeconds            | int       | 临时数据失效的秒数     | 60            |
| operationTimeoutMilliseconds | int       | 客户端操作超时的毫秒数  | 500           |
| digest                       | String    | 登录认证密码          |               |

### Etcd 持久化

类型：Etcd

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*               | *默认值*         |
| ---------------------------- | --------- | ------------------- | --------------- |
| timeToLiveSeconds            | long      | 临时数据失效的秒数     | 30              |
| connectionTimeout            | long      | 连接超时秒数          | 30              |

### Nacos 持久化

类型：Nacos

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*                         | *默认值*         |
| ---------------------------- | --------- | ----------------------------- | --------------- |
| clusterIp                    | String    | 集群中的唯一标识                 | 真实主机IP       |
| retryIntervalMilliseconds    | long      | 重试间隔毫秒数                   | 500             |
| maxRetries                   | int       | 客户端检查数据可用性的最大重试次数  | 3               |
| timeToLiveSeconds            | int       | 临时实例失效的秒数               | 30              |

### Consul 持久化

类型：Consul

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*                         | *默认值*         |
| ---------------------------- | --------- | ----------------------------- | --------------- |
| timeToLiveSeconds            | String    | 临时实例失效的秒数               | 30s             |
| blockQueryTimeToSeconds      | long      | 查询请求超时秒数                 | 60              |

## 操作步骤

1. 在 server.yaml 中配置 Mode 运行模式
1. 配置元数据持久化仓库类型

## 配置示例

- 单机模式配置方式

```yaml
mode:
  type: Standalone
  repository:
    type: JDBC
    props:
      provider: H2
      jdbc_url: jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
      username: test
      password: Test@123
```

- 集群模式

```yaml
mode:
  type: Cluster
  repository:
    type: zookeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
```
