+++
title = "使用 Helm"
weight = 3
+++

使用 [Helm](https://helm.sh/) 在 Kubernetes 集群中引导 ShardingSphere-Proxy 实例进行安装。

## 快速入门

注意️：以下安装方式将使用默认的 server.yaml 配置启动 ShardingSphere-Proxy

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

## 操作步骤

### 必要条件

1. kubernetes 1.18+
1. kubectl
1. helm 3.2.0+

可以动态申请 PV(Persistent Volumes) 的 StorageClass（可选）。

### 安装

#### 在线安装

将 ShardingSphere-Proxy 添加到 Helm 本地仓库：

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

以 ShardingSphere-Proxy 命名安装 charts：
注意️：以下安装方式将使用默认的 server.yaml 配置启动 ShardingSphere-Proxy

```shell
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

如需修改配置，请执行以下操作:

```shell
helm pull shardingsphere/apache-shardingsphere-proxy
tar -zxvf apache-shardingsphere-proxy-1.1.0-chart.tgz
# 修改 apache-shardingsphere-proxy/values.yaml 中 serverConfig 部分
helm install shardingsphere-proxy apache-shardingsphere-proxy
```

#### 源码安装

```shell
cd apache-shardingsphere-proxy/charts/governance
helm dependency build 
cd ../..                               
helm dependency build                                   
cd ..                                                   
helm install shardingsphere-proxy apache-shardingsphere-proxy
```

执行上述命令以执行默认配置进行安装。
其他的配置详见下方的配置列表。
执行 `helm list` 获取所有安装的 release。

### 卸载

```shell
helm uninstall shardingsphere-proxy
```

默认删除所有发布记录，增加 `--keep-history` 参数保留发布记录。

## 配置项

### 治理节点配置项

| 配置项                  | 描述                  | 值      |
|----------------------|---------------------|--------|
| `governance.enabled` | 用来切换是否使用治理节点的 chart | `true` |

### 治理节点 ZooKeeper 配置项

| 配置项                                            | 描述                                                                                  | 值                   |
| ------------------------------------------------ | ------------------------------------------------------------------------------------- | ------------------- |
| `governance.zookeeper.enabled`                   | 用来切换是否使用 ZooKeeper 的 chart                                                      | `true`              |
| `governance.zookeeper.replicaCount`              | ZooKeeper 节点数量                                                                     | `1`                 |
| `governance.zookeeper.persistence.enabled`       | 标识  ZooKeeper 是否使用持久卷申领 (PersistentVolumeClaim) 用来申请持久卷（PersistentVolume）| `false`             |
| `governance.zookeeper.persistence.storageClass`  | 持久卷（PersistentVolume）的存储类 (StorageClass)                                        | `""`                |
| `governance.zookeeper.persistence.accessModes`   | 持久卷（PersistentVolume）的访问模式                                                     | `["ReadWriteOnce"]` |
| `governance.zookeeper.persistence.size`          | 持久卷（PersistentVolume） 大小                                                         | `8Gi`               |
| `governance.zookeeper.resources.limits`          | ZooKeeper 容器的资源限制                                                                | `{}`                |
| `governance.zookeeper.resources.requests.memory` | ZooKeeper 容器申请的内存                                                                | `256Mi`             |
| `governance.zookeeper.resources.requests.cpu`    | ZooKeeper 容器申请的 cpu 核数                                                           | `250m`              |

### 计算节点 ShardingSphere-Proxy 配置项

| 配置项                               | 描述                                   | 值                             |
| ----------------------------------- | ------------------------------------- | ------------------------------ |
| `compute.image.repository`          | ShardingSphere-Proxy 的镜像名           | `apache/shardingsphere-proxy` |
| `compute.image.pullPolicy`          | ShardingSphere-Proxy 镜像拉取策略        | `IfNotPresent`                |
| `compute.image.tag`                 | ShardingSphere-Proxy 镜像标签           | `5.1.2`                       |
| `compute.imagePullSecrets`          | 拉取私有仓库的凭证                        | `[]`                          |
| `compute.resources.limits`          | ShardingSphere-Proxy 容器的资源限制      | `{}`                          |
| `compute.resources.requests.memory` | ShardingSphere-Proxy 容器申请的内存      | `2Gi`                         |
| `compute.resources.requests.cpu`    | ShardingSphere-Proxy 容器申请的 cpu 核数 | `200m`                        |
| `compute.replicas`                  | ShardingSphere-Proxy 节点个数           | `3`                           |
| `compute.service.type`              | ShardingSphere-Proxy 网络模式           | `ClusterIP`                   |
| `compute.service.port`              | ShardingSphere-Proxy 暴露端口           | `3307`                        |
| `compute.mysqlConnector.version`    | MySQL 驱动版本                          | `5.1.49`                      |
| `compute.startPort`                 | ShardingSphere-Proxy 启动端口           | `3307`                        |

### 计算节点 ShardingSphere-Proxy Server 配置 权限配置项

| 配置项                                              | 描述                                                                                           | 值                         |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------- | -------------------------- |
| `compute.serverConfig.authority.privilege.type`    | 存储节点数据授权的权限提供者类型，缺省值为 ALL_PERMITTED                                              | `ALL_PRIVILEGES_PERMITTED` |
| `compute.serverConfig.authority.users[0].password` | 用于登录计算节点的密码                                                                            | `root`                     |
| `compute.serverConfig.authority.users[0].user`     | 用于登录计算节点的用户名，授权主机。格式: <username>@<hostname> hostname 为 % 或空字符串表示不限制授权主机 | `root@%`                   |

### 计算节点 ShardingSphere-Proxy Server 配置 模式配置项

| 配置项                                                                     | 描述                                | 值                                                                     |
| ------------------------------------------------------------------------- | ---------------------------------- | --------------------------------------------------------------------- |
| `compute.serverConfig.mode.type`                                          | 运行模式类型。 现阶段仅支持 Cluster 模式 | `Cluster`                                                             |
| `compute.serverConfig.mode.repository.props.namespace`                    | 注册中心命名空间                      | `governance_ds`                                                        |
| `compute.serverConfig.mode.repository.props.server-lists`                 | 注册中心连接地址                      | `{{ printf "%s-zookeeper.%s:2181" .Release.Name .Release.Namespace }}` |
| `compute.serverConfig.mode.repository.props.maxRetries`                   | 客户端连接最大重试次数                 | `3`                                                                    |
| `compute.serverConfig.mode.repository.props.operationTimeoutMilliseconds` | 客户端操作超时的毫秒数                 | `5000`                                                                 |
| `compute.serverConfig.mode.repository.props.retryIntervalMilliseconds`    | 重试间隔毫秒数                       | `500`                                                                  |
| `compute.serverConfig.mode.repository.props.timeToLiveSeconds`            | 临时数据失效的秒数                    | `60`                                                                   |
| `compute.serverConfig.mode.repository.type`                               | 持久化仓库类型。 现阶段仅支持 ZooKeeper | `ZooKeeper`                                                            |
| `compute.serverConfig.mode.overwrite`                                     | 是否使用本地配置覆盖持久化配置           | `true`                                                                |
