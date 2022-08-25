+++
title = "使用 Helm"
weight = 3
+++

## 背景信息

使用 [Helm](https://helm.sh/) 在 Kubernetes 集群中引导 ShardingSphere-Proxy 实例进行安装。

## 前提条件

- kubernetes 1.18+
- kubectl
- helm 3.3.0+
- 可以动态申请 PV(Persistent Volumes) 的 StorageClass 用于持久化数据。（可选）

## 操作步骤

### 在线安装

1. 将 ShardingSphere-Proxy 添加到 Helm 本地仓库：

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

1. 以 ShardingSphere-Proxy 命名安装 charts：

```shell
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

### 源码安装

1. 执行下述命令以执行默认配置进行安装。

```shell
cd shardingsphere-proxy/charts/governance
 helm dependency build 
 cd ../..
 helm dependency build 
 cd ..
 helm install shardingsphere-proxy shardingsphere-proxy 
```

1. 其他的配置详见下方的配置列表。
1. 执行 helm list 获取所有安装的 release。

### 卸载

1. 默认删除所有发布记录，增加 `--keep-history` 参数保留发布记录。

```shell
helm uninstall shardingsphere-proxy
```

## 参数解释

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
| `compute.serverConfig`              | ShardingSphere-Proxy 模式配置文件        |  `""`                         |

## 配置示例

```PlainText
#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

## @section Governance-Node parameters
## @param governance.enabled Switch to enable or disable the governance helm chart
##
governance:
  enabled: true
  ## @section Governance-Node ZooKeeper parameters
  zookeeper:
    ## @param governance.zookeeper.enabled Switch to enable or disable the ZooKeeper helm chart
    ##
    enabled: true
    ## @param governance.zookeeper.replicaCount Number of ZooKeeper nodes
    ##
    replicaCount: 1
    ## ZooKeeper Persistence parameters
    ## ref: https://kubernetes.io/docs/user-guide/persistent-volumes/
    ## @param governance.zookeeper.persistence.enabled Enable persistence on ZooKeeper using PVC(s)
    ## @param governance.zookeeper.persistence.storageClass Persistent Volume storage class
    ## @param governance.zookeeper.persistence.accessModes Persistent Volume access modes
    ## @param governance.zookeeper.persistence.size Persistent Volume size
    ##
    persistence:
      enabled: false
      storageClass: ""
      accessModes:
        - ReadWriteOnce
      size: 8Gi
    ## ZooKeeper's resource requests and limits
    ## ref: https://kubernetes.io/docs/user-guide/compute-resources/
    ## @param governance.zookeeper.resources.limits The resources limits for the ZooKeeper containers
    ## @param governance.zookeeper.resources.requests.memory The requested memory for the ZooKeeper containers
    ## @param governance.zookeeper.resources.requests.cpu The requested cpu for the ZooKeeper containers
    ##
    resources:
      limits: {}
      requests:
        memory: 256Mi
        cpu: 250m

## @section Compute-Node parameters
## 
compute:
  ## @section Compute-Node ShardingSphere-Proxy parameters
  ## ref: https://kubernetes.io/docs/concepts/containers/images/
  ## @param compute.image.repository Image name of ShardingSphere-Proxy.
  ## @param compute.image.pullPolicy The policy for pulling ShardingSphere-Proxy image
  ## @param compute.image.tag ShardingSphere-Proxy image tag
  ##
  image:
    repository: "apache/shardingsphere-proxy"
    pullPolicy: IfNotPresent
    ## Overrides the image tag whose default is the chart appVersion.
    ##
    tag: "5.1.2"
  ## @param compute.imagePullSecrets Specify docker-registry secret names as an array
  ## e.g：
  ## imagePullSecrets:
  ##   - name: myRegistryKeySecretName
  ##
  imagePullSecrets: []
  ## ShardingSphere-Proxy resource requests and limits
  ## ref: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/
  ## @param compute.resources.limits The resources limits for the ShardingSphere-Proxy containers
  ## @param compute.resources.requests.memory The requested memory for the ShardingSphere-Proxy containers
  ## @param compute.resources.requests.cpu The requested cpu for the ShardingSphere-Proxy containers
  ##
  resources:
    limits: {}
    requests:
      memory: 2Gi
      cpu: 200m
  ## ShardingSphere-Proxy Deployment Configuration
  ## ref: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
  ## ref: https://kubernetes.io/docs/concepts/services-networking/service/
  ## @param compute.replicas Number of cluster replicas
  ##
  replicas: 3
  ## @param compute.service.type ShardingSphere-Proxy network mode
  ## @param compute.service.port ShardingSphere-Proxy expose port
  ##
  service:
    type: ClusterIP
    port: 3307
  ## MySQL connector Configuration
  ## ref: https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/
  ## @param compute.mysqlConnector.version MySQL connector version
  ##
  mysqlConnector:
    version: "5.1.49"
  ## @param compute.startPort ShardingSphere-Proxy start port
  ## ShardingSphere-Proxy start port
  ## ref: https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/startup/docker/
  ##
  startPort: 3307
  ## @section Compute-Node ShardingSphere-Proxy ServerConfiguration parameters
  ## NOTE: If you use the sub-charts to deploy Zookeeper, the server-lists field must be "{{ printf \"%s-zookeeper.%s:2181\" .Release.Name .Release.Namespace }}",
  ## otherwise please fill in the correct zookeeper address
  ## The server.yaml is auto-generated based on this parameter.
  ## If it is empty, the server.yaml is also empty.
  ## ref: https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/mode/
  ## ref: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/metadata-repository/
  ##
  serverConfig:
    ## @section Compute-Node ShardingSphere-Proxy ServerConfiguration authority parameters
    ## NOTE: It is used to set up initial user to login compute node, and authority data of storage node.
    ## ref: https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/yaml-config/authentication/
    ## @param compute.serverConfig.authority.privilege.type authority provider for storage node, the default value is ALL_PERMITTED
    ## @param compute.serverConfig.authority.users[0].password Password for compute node.
    ## @param compute.serverConfig.authority.users[0].user Username,authorized host for compute node. Format: <username>@<hostname> hostname is % or empty string means do not care about authorized host
    ##
    authority:
      privilege:
        type: ALL_PRIVILEGES_PERMITTED
      users:
      - password: root
        user: root@%
    ## @section Compute-Node ShardingSphere-Proxy ServerConfiguration mode Configuration parameters
    ## @param compute.serverConfig.mode.type Type of mode configuration. Now only support Cluster mode
    ## @param compute.serverConfig.mode.repository.props.namespace Namespace of registry center
    ## @param compute.serverConfig.mode.repository.props.server-lists Server lists of registry center
    ## @param compute.serverConfig.mode.repository.props.maxRetries Max retries of client connection
    ## @param compute.serverConfig.mode.repository.props.operationTimeoutMilliseconds Milliseconds of operation timeout
    ## @param compute.serverConfig.mode.repository.props.retryIntervalMilliseconds Milliseconds of retry interval
    ## @param compute.serverConfig.mode.repository.props.timeToLiveSeconds Seconds of ephemeral data live
    ## @param compute.serverConfig.mode.repository.type Type of persist repository. Now only support ZooKeeper
    ## @param compute.serverConfig.mode.overwrite Whether overwrite persistent configuration with local configuration
    ##
    mode:
      type: Cluster
      repository:
        type: ZooKeeper
        props:
          maxRetries: 3
          namespace: governance_ds
          operationTimeoutMilliseconds: 5000
          retryIntervalMilliseconds: 500
          server-lists: "{{ printf \"%s-zookeeper.%s:2181\" .Release.Name .Release.Namespace }}"
          timeToLiveSeconds: 60
      overwrite: true
```