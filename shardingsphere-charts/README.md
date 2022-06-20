# Use Helm

Use [Helm](https://helm.sh/) to provide guidance for the installation of ShardingSphere-Proxy instance in Kubernetes cluster.

## Quick Start

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
```

## Step By Step

### Requirements

Kubernetes 1.18+

kubectl

Helm 3.2.0+

Use StorageClass to allow dynamic provisioning of Persistent Volumes (PV) for data persistent (optional).

### Install

#### Online installation     
Add ShardingSphere-Proxy to the local helm repo:

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

Install ShardingSphere-Proxy charts:

```shell
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy
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
| ----------------------------------- | ------------------------------------------------------------ | ----------------------------- |
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
| `compute.serverConfig`              | ServerConfiguration file for ShardingSphere-Proxy            | `""`                          |


