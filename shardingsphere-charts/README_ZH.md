# 使用 Helm

使用 Helm 工具在 Kubernetes 集群中引导 ShardingSphere-Proxy 实例进行安装。

## 快速入门

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install ShardingSphere-Proxy shardingsphere/ShardingSphere-Proxy
```

## 操作步骤

### 必要条件

kubernetes 1.18+

kubectl

helm 3.2.0+

可以动态申请 PV(Persistent Volumes) 的 StorageClass 已持久化数据。

### 安装

将 ShardingSphere-Proxy 添加到 Helm 本地仓库：

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

以 ShardingSphere-Proxy 命名安装 charts：

```shell
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

执行上述命令以执行默认配置进行安装。
其他的配置详见下方的配置列表。
执行 `helm list` 获取所有安装的 release。

### 卸载

```shell
helm uninstall shardingsphere-proxy
```

默认删除所有发布记录，增加 `--keep-history` 参数保留发布记录。

## 配置项说明

### 全局配置

| 名称                       | 描述           | 默认值  |
| ------------------------- | -------------- | ----- |
| global.resources.limits   | 容器的全局资源限制 | {}    |
| global.resources.requests | 容器的全局资源申请 | {}    |

### ShardingSphere-Proxy 配置

| 名称                    | 描述                                                    | 默认值                        |
| ---------------------- | ------------------------------------------------------- | --------------------------- |
| image.repository       | ShardingSphere-Proxy 镜像名，默认是从 Apache 官方镜像仓库拉取 | apache/ShardingSphere-Proxy |
| image.pullPolicy       | 镜像拉取策略                                              | IfNotPresent                |
| image.tag              | 镜像 tag                                                 | 5.1.0                       |
| service.type           | 网络模式                                                  | NodePort                    |
| replicas               | 集群副本数                                                | 3                           |
| proxyport              | 启动端口                                                  | 3307                        |
| mysqlconnector.enabled | MySQL 驱动开启开关                                         | true                        |
| mysqlconnector.version | MySQL 驱动版本                                            | 5.1.49                      |

### MySQL 配置

| 名称                  | 描述                              | 默认值  |
|--------------------- | -------------------------------- | ------ |
| mysql.enabled        | 开启 MySQL 子 charts 依赖          | true   |
| mysql.storageclass   | MySQL 持久化存储需要的 StorageClass | nil    |
| mysql.storagerequest | MySQL 持久化存储需要的空间           | nil    |

### ZooKeeper 配置

| 名称                      | 描述                                  | 默认值  |
|------------------------- | ------------------------------------ | ------ |
| zookeeper.enabled        | 开启 ZooKeeper 子 charts 依赖          | true   |
| zookeeper.storageclass   | ZooKeeper 持久化存储需要的 StorageClass | nil    |
| zookeeper.storagerequest | ZooKeeper 持久化存储需要的空间           | nil    |
