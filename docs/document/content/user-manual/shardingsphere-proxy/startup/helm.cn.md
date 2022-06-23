+++
title = "使用 Helm"
weight = 3
+++

使用 [Helm](https://helm.sh/) 在 Kubernetes 集群中引导 ShardingSphere-Proxy 实例进行安装。

## 快速入门

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

## 操作步骤

### 必要条件

1. kubernetes 1.18+

2. kubectl

3. helm 3.2.0+

可以动态申请 PV(Persistent Volumes) 的 StorageClass（可选）。

### 安装

#### 在线安装

将 ShardingSphere-Proxy 添加到 Helm 本地仓库：

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

以 ShardingSphere-Proxy 命名安装 charts：

```shell
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
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


### 治理节点--ZooKeeper 配置项

| 配置项                                               | 描述                                                                        | 值                   |
|--------------------------------------------------|---------------------------------------------------------------------------|---------------------|
| `governance.zookeeper.enabled`                   | 用来切换是否使用 ZooKeeper 的 chart                                                | `true`              |
| `governance.zookeeper.replicaCount`              | ZooKeeper 节点数量                                                            | `1`                 |
| `governance.zookeeper.persistence.enabled`       | 标识  ZooKeeper 是否使用持久卷申领 (PersistentVolumeClaim) 用来申请持久卷（PersistentVolume） | `false`             |
| `governance.zookeeper.persistence.storageClass`  | 持久卷（PersistentVolume）的存储类 (StorageClass)                                  | `""`                |
| `governance.zookeeper.persistence.accessModes`   | 持久卷（PersistentVolume）的访问模式                                                | `["ReadWriteOnce"]` |
| `governance.zookeeper.persistence.size`          | 持久卷（PersistentVolume） 大小                                                  | `8Gi`               |
| `governance.zookeeper.resources.limits`          | ZooKeeper 容器的资源限制                                                         | `{}`                |
| `governance.zookeeper.resources.requests.memory` | ZooKeeper 容器申请的内存                                                         | `256Mi`             |
| `governance.zookeeper.resources.requests.cpu`    | ZooKeeper 容器申请的 cpu 核数                                                    | `250m`              |


### 计算节点--ShardingSphere-Proxy 配置项

| 配置项                                | 描述                                | 值                             |
|------------------------------------|-----------------------------------|-------------------------------|
| `compute.image.repository`         | ShardingSphere-Proxy 的镜像名         | `apache/shardingsphere-proxy` |
| `compute.image.pullPolicy`         | ShardingSphere-Proxy 镜像拉取策略       | `IfNotPresent`                |
| `compute.image.tag`                | ShardingSphere-Proxy 镜像标签         | `5.1.2`                       |
| `compute.imagePullSecrets`         | 拉取私有仓库的凭证                         | `[]`                          |
| `compute.resources.limits`         | ShardingSphere-Proxy 容器的资源限制      | `{}`                          |
| `compute.resources.requests.memory` | ShardingSphere-Proxy 容器申请的内存      | `2Gi`                         |
| `compute.resources.requests.cpu`   | ShardingSphere-Proxy 容器申请的 cpu 核数 | `200m`                        |
| `compute.replicas`                 | ShardingSphere-Proxy 节点个数         | `3`                           |
| `compute.service.type`             | ShardingSphere-Proxy 网络模式         | `ClusterIP`                   |
| `compute.service.port`             | ShardingSphere-Proxy 暴露端口         | `3307`                        |
| `compute.mysqlConnector.version`   | MySQL 驱动版本                        | `5.1.49`                      |
| `compute.startPort`                | ShardingSphere-Proxy 启动端口         | `3307`                        |
| `compute.serverConfig`             | ShardingSphere-Proxy 模式配置文件       | `""`                          |
