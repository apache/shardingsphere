+++
title = "使用 Helm"
weight = 3
+++
# ShardingSphere-Proxy Helm Chart
## **太长不看版**

```
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

## shardingsphere-proxy 介绍

这个charts 使用helm工具在一个kubernetes集群中引导 一个shardingsphere-proxy 实例进行安装



## 必要条件

kubernetes 1.18+

kubectl

helm 3.2.0+

如需要持久化数据：

可以动态申请pv 的strogeclass支持

## 安装shardingsphere-proxy chart

将 shardingsphere-proxy 添加到helm 本地仓库

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

以shardingsphere-proxy 命名安装charts

```shell
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

执行上述命令后会以默认的配置进行安装。其他的配置详见下方的配置列表。

如果需要获取所有安装的release ，可以执行```helm list```

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
| `global.resources.limits`   | The resources limits for the shardingsphere-proxy,mysql,zookeeper containers    | `{}`  |
| `global.resources.requests` | The requested resources for the shardingsphere-proxy,mysql,zookeeper containers | `{}`  |


#### mysql parameters

| Name                   | Description               | Value  |
| ---------------------- | ------------------------- | ------ |
| `mysql.enabled`        | 开启mysql子charts依赖          | `true` |
| `mysql.storageclass`   | mysql持久化存储需要的storageclass | `nil`  |
| `mysql.storagerequest` | mysql持久化存储需要的空间           | `nil`  |


#### zookeeper parameters

| Name                       | Description                   | Value  |
| -------------------------- | ----------------------------- | ------ |
| `zookeeper.enabled`        | 开启zookeeper子charts依赖          | `true` |
| `zookeeper.storageclass`   | zookeeper持久化存储需要的storageclass | `nil`  |
| `zookeeper.storagerequest` | zookeeper持久化存储需要的空间           | `nil`  |


#### shardingsphere-proxy parameters

| Name                     | Description                                | Value                         |
| ------------------------ | ------------------------------------------ | ----------------------------- |
| `image.repository`       | shardingsphere-proxy镜像名，默认是从apache官方镜像仓库拉取 | `apache/shardingsphere-proxy` |
| `image.pullPolicy`       | 镜像拉取策略                                     | `IfNotPresent`                |
| `image.tag`              | 镜像tag                                      | `5.1.0`                       |
| `replicas`               | shardingsphere-proxy集群模式副本数                | `3`                           |
| `service.type`           | shardingsphere-proxy网络模式                   | `NodePort`                    |
| `mysqlconnector.enabled` | mysql驱动开启开关                                | `true`                        |
| `mysqlconnector.version` | mysql驱动版本                                  | `5.1.49`                      |
| `proxyport`              | 启动端口                                       | `3307`                        |


### 相关配置详见 shardingsphere 文档

[YAML 配置 :: ShardingSphere (apache.org)](https://shardingsphere.apache.org/document/5.1.0/cn/user-manual/shardingsphere-jdbc/yaml-config/)



