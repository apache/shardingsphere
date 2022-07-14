+++
title = "Use Helm"
weight = 3
+++

Use [Helm](https://helm.sh/) to provide guidance for the installation of ShardingSphere-Proxy instance in Kubernetes cluster.

## Quick Start

Attention: The following installation method will start ShardingSphere-Proxy with the default server.yaml configuration

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

## Step By Step

### Requirements

1. Kubernetes 1.18+
1. kubectl
1. Helm 3.2.0+

Use StorageClass to allow dynamic provisioning of Persistent Volumes (PV) for data persistent (optional).

### Install

#### Online installation    

1. Add ShardingSphere-Proxy to the local helm repo:

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

2. Install ShardingSphere-Proxy charts:
Attention: The following installation method will start ShardingSphere-Proxy with the default server.yaml configuration

```shell
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

To modify the configuration:

```shell
helm pull shardingsphere/apache-shardingsphere-proxy
tar -zxvf apache-shardingsphere-proxy-1.1.0-chart.tgz
# Modify the serverConfig section in apache-shardingsphere-proxy/values.yaml
helm install shardingsphere-proxy apache-shardingsphere-proxy
```

#### Source installation

```shell
cd apache-shardingsphere-proxy/charts/governance
helm dependency build 
cd ../..                               
helm dependency build                                   
cd ..                                                   
helm install shardingsphere-proxy apache-shardingsphere-proxy
```

Charts will be installed with default configuration if above commands executed.
Please refer configuration items description below to get more details.
Execute `helm list` to acquire all installed releases.

### Uninstall

```shell
helm uninstall shardingsphere-proxy
```

Delete all release records by default, add `--keep-history` to keep them. 

## Parameters

### Governance-Node parameters

| Name                 | Description                                           | Value  |
| -------------------- | ----------------------------------------------------- | ------ |
| `governance.enabled` | Switch to enable or disable the governance helm chart | `true` |

### Governance-Node ZooKeeper parameters

| Name                                             | Description                                          | Value               |
| ------------------------------------------------ | ---------------------------------------------------- | ------------------- |
| `governance.zookeeper.enabled`                   | Switch to enable or disable the ZooKeeper helm chart | `true`              |
| `governance.zookeeper.replicaCount`              | Number of ZooKeeper nodes                            | `1`                 |
| `governance.zookeeper.persistence.enabled`       | Enable persistence on ZooKeeper using PVC(s)         | `false`             |
| `governance.zookeeper.persistence.storageClass`  | Persistent Volume storage class                      | `""`                |
| `governance.zookeeper.persistence.accessModes`   | Persistent Volume access modes                       | `["ReadWriteOnce"]` |
| `governance.zookeeper.persistence.size`          | Persistent Volume size                               | `8Gi`               |
| `governance.zookeeper.resources.limits`          | The resources limits for the ZooKeeper containers    | `{}`                |
| `governance.zookeeper.resources.requests.memory` | The requested memory for the ZooKeeper containers    | `256Mi`             |
| `governance.zookeeper.resources.requests.cpu`    | The requested cpu for the ZooKeeper containers       | `250m`              |

### Compute-Node ShardingSphere-Proxy parameters

| Name                                | Description                                                  | Value                         |
| ----------------------------------- | ------------------------------------------------------------ |-------------------------------|
| `compute.image.repository`          | Image name of ShardingSphere-Proxy.                          | `apache/shardingsphere-proxy` |
| `compute.image.pullPolicy`          | The policy for pulling ShardingSphere-Proxy image            | `IfNotPresent`                |
| `compute.image.tag`                 | ShardingSphere-Proxy image tag                               | `5.1.2`                       |
| `compute.imagePullSecrets`          | Specify docker-registry secret names as an array             | `[]`                          |
| `compute.resources.limits`          | The resources limits for the ShardingSphere-Proxy containers | `{}`                          |
| `compute.resources.requests.memory` | The requested memory for the ShardingSphere-Proxy containers | `2Gi`                         |
| `compute.resources.requests.cpu`    | The requested cpu for the ShardingSphere-Proxy containers    | `200m`                        |
| `compute.replicas`                  | Number of cluster replicas                                   | `3`                           |
| `compute.service.type`              | ShardingSphere-Proxy network mode                            | `ClusterIP`                   |
| `compute.service.port`              | ShardingSphere-Proxy expose port                             | `3307`                        |
| `compute.mysqlConnector.version`    | MySQL connector version                                      | `5.1.49`                      |
| `compute.startPort`                 | ShardingSphere-Proxy start port                              | `3307`                        |

### Compute-Node ShardingSphere-Proxy ServerConfiguration authority parameters

| Name                                               | Description                                                                                                                                    | Value                      |
| -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------- |
| `compute.serverConfig.authority.privilege.type`    | authority provider for storage node, the default value is ALL_PERMITTED                                                                        | `ALL_PRIVILEGES_PERMITTED` |
| `compute.serverConfig.authority.users[0].password` | Password for compute node.                                                                                                                     | `root`                     |
| `compute.serverConfig.authority.users[0].user`     | Username,authorized host for compute node. Format: <username>@<hostname> hostname is % or empty string means do not care about authorized host | `root@%`                   |

### Compute-Node ShardingSphere-Proxy ServerConfiguration mode Configuration parameters

| Name                                                                      | Description                                                         | Value                                                                  |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| `compute.serverConfig.mode.type`                                          | Type of mode configuration. Now only support Cluster mode           | `Cluster`                                                              |
| `compute.serverConfig.mode.repository.props.namespace`                    | Namespace of registry center                                        | `governance_ds`                                                        |
| `compute.serverConfig.mode.repository.props.server-lists`                 | Server lists of registry center                                     | `{{ printf "%s-zookeeper.%s:2181" .Release.Name .Release.Namespace }}` |
| `compute.serverConfig.mode.repository.props.maxRetries`                   | Max retries of client connection                                    | `3`                                                                    |
| `compute.serverConfig.mode.repository.props.operationTimeoutMilliseconds` | Milliseconds of operation timeout                                   | `5000`                                                                 |
| `compute.serverConfig.mode.repository.props.retryIntervalMilliseconds`    | Milliseconds of retry interval                                      | `500`                                                                  |
| `compute.serverConfig.mode.repository.props.timeToLiveSeconds`            | Seconds of ephemeral data live                                      | `60`                                                                   |
| `compute.serverConfig.mode.repository.type`                               | Type of persist repository. Now only support ZooKeeper              | `ZooKeeper`                                                            |
| `compute.serverConfig.mode.overwrite`                                     | Whether overwrite persistent configuration with local configuration | `true`                                                                 |
