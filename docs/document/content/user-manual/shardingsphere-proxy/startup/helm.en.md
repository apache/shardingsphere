+++
title = "Use Helm"
weight = 3
+++

Use the Helm tool to provide guidance for the installation of ShardingSphere-Proxy instance in Kubernetes cluster.

## Quick Start

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

## Step By Step

### Requirements

Kubernetes 1.18+

kubectl

Helm 3.2.0+

Use StorageClass to allow dynamic provisioning of Persistent Volumes (PV) for data persistent.

### Install

Add ShardingSphere-Proxy to the local helm repo:

```shell
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

Install ShardingSphere-Proxy charts:

```shell
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

Charts will be installed with default configuration if above commands executed.
Please refer configuration items description below to get more details.
Execute `helm list` to acquire all installed releases.

### Uninstall

```shell
helm uninstall shardingsphere-proxy
```

Delete all release records by default, add `--keep-history` to keep them.

## Configuration Items Description

### Global Configuration

| Name                      | Description                                | Default Value |
| ------------------------- | ------------------------------------------ | ------------- |
| global.resources.limits   | The resources limits for all containers    | {}            |
| global.resources.requests | The requested resources for all containers | {}            |

### ShardingSphere-Proxy Configuration

| Name                   | Description                                                                         | Default Value               |
| ---------------------- |------------------------------------------------------------------------------------ | --------------------------- |
| image.repository       | Image name of ShardingSphere-Proxy. Pull from apache official repository by default | apache/shardingsphere-proxy |
| image.pullPolicy       | The policy for pulling an image                                                     | IfNotPresent                |
| image.tag              | Image tag                                                                           | 5.1.0                       |
| service.type           | Network mode                                                                        | NodePort                    |
| replicas               | Number of cluster replicas                                                          | 3                           |
| proxyport              | start port                                                                          | 3307                        |
| mysqlconnector.enabled | MySQL connector enabled                                                             | TRUE                        |
| mysqlconnector.version | MySQL connector version                                                             | 5.1.49                      |

### MySQL Configuration

| Name                 | Description                                      | Default Value |
| -------------------- | ------------------------------------------------ | ------------- |
| mysql.enabled        | Enable MySQL sub-charts dependency               | TRUE          |
| mysql.storageclass   | Storage class needed by MySQL persistent storage | nil           |
| mysql.storagerequest | Space for MySQL persistent storage               | nil           |

### ZooKeeper Configuration

| Name                     | Description                                           | Default Value  |
| ------------------------ | ----------------------------------------------------- | -------------- |
| zookeeper.enabled        | Enable ZooKeeper sub-charts dependency                | TRUE           |
| zookeeper.storageclass   | Storage class needed by ZooKeeper persistent storage  | nil            |
| zookeeper.storagerequest | Space for ZooKeeper persistent storage                | nil            |
