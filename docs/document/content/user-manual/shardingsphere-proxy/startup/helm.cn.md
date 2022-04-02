+++
title = "使用 Helm"
weight = 3
+++
# ShardingSphere-Proxy Helm Chart
## **TL;DR**

```
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

## ShardingSphere-Proxy 介绍

这个charts 使用 helm 工具在一个 kubernetes 集群中引导 一个 ShardingSphere-Proxy 实例进行安装



## 必要条件

kubernetes 1.18+

kubectl

helm 3.2.0+

如需要持久化数据：

可以动态申请 PV(Persistent Volumes) 的 storageclass 支持

## 安装 ShardingSphere-Proxy chart

将 ShardingSphere-Proxy 添加到 helm 本地仓库

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

以 ShardingSphere-Proxy 命名安装 charts

```shell
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

执行上述命令后会以默认的配置进行安装。其他的配置详见下方的配置列表。

如果需要获取所有安装的 release ，可以执行```helm list```

## 卸载

如果要卸载上述安装的release ，请执行

```shell
helm uninstall shardingsphere-proxy
```

helm uninstall 会默认删除所有发布记录，如果需要留下发布记录，请加上 ```--keep-history```

## 配置项说明
### Parameters

#### Global parameters

| Name                        | Description                                                                     | Value |
| --------------------------- | ------------------------------------------------------------------------------- | ----- |
| `global.resources.limits`   | The resources limits for the ShardingSphere-Proxy,MySQL,ZooKeeper containers    | `{}`  |
| `global.resources.requests` | The requested resources for the ShardingSphere-Proxy,MySQL,ZooKeeper containers | `{}`  |


#### MySQL parameters

| Name                   | Description                | Value  |
| ---------------------- |----------------------------| ------ |
| `mysql.enabled`        | 开启 MySQL 子 charts 依赖       | `true` |
| `mysql.storageclass`   | MySQL持久化存储需要的 storageclass | `nil`  |
| `mysql.storagerequest` | MySQL持久化存储需要的空间            | `nil`  |


#### ZooKeeper parameters

| Name                       | Description                    | Value  |
| -------------------------- |--------------------------------| ------ |
| `zookeeper.enabled`        | 开启ZooKeeper子 charts 依赖         | `true` |
| `zookeeper.storageclass`   | ZooKeeper持久化存储需要的 storageclass | `nil`  |
| `zookeeper.storagerequest` | ZooKeeper持久化存储需要的空间            | `nil`  |


#### ShardingSphere-Proxy parameters

| Name                     | Description                                   | Value                         |
| ------------------------ |-----------------------------------------------| ----------------------------- |
| `image.repository`       | ShardingSphere-Proxy 镜像名，默认是从 apache 官方镜像仓库拉取 | `apache/shardingsphere-proxy` |
| `image.pullPolicy`       | 镜像拉取策略                                        | `IfNotPresent`                |
| `image.tag`              | 镜像 tag                                        | `5.1.0`                       |
| `replicas`               | ShardingSphere-Proxy 集群模式副本数                  | `3`                           |
| `service.type`           | ShardingSphere-Proxy 网络模式                     | `NodePort`                    |
| `mysqlconnector.enabled` | MySQL 驱动开启开关                                  | `true`                        |
| `mysqlconnector.version` | MySQL 驱动版本                                    | `5.1.49`                      |
| `proxyport`              | 启动端口                                          | `3307`                        |


### ShardingSphere-Proxy config.yaml && server.yaml 相关配置详见 ShardingSphere 文档

[YAML 配置 :: ShardingSphere (apache.org)](https://shardingsphere.apache.org/document/5.1.0/cn/user-manual/shardingsphere-jdbc/yaml-config/)



