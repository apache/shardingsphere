+++
title = "Use Helm"
weight = 3
+++
# ShardingSphere-Proxy Helm Chart
## TL;DR 
```Dockerfiile
helm repo add shardingsphere https://shardingsphere.apache.org/charts
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

## Introduction to ShardingSphere-Proxy
These charts use the Helm tool to provide guidance for the installation of a ShardingSphere-Proxy instance in a Kubernetes cluster.

## Requirements
Kubernetes 1.18+
kubectl
Helm 3.2.0+
If you need persistent data:
Please configure StorageClass that allows dynamic provisioning of Persistent Volumes (PV)

## Install ShardingSphere-Proxy chart
Add ShardingSphere-Proxy to the local helm repo
```
helm repo add shardingsphere https://shardingsphere.apache.org/charts
```

Install ShardingSphere-Proxy charts
```
helm install shardingsphere-proxy shardingsphere/shardingsphere-proxy
```

Once execution of the above commands is completed, charts will be installed with default configuration. You can refer to the configuration list below for other configurations.

If you need to acquire all the installed releases, execute `helm list`

## Uninstall
To uninstall the releases, please execute
```
helm uninstall shardingsphere-proxy
```

helm uninstall will delete all release records by default. If you need to keep them, please add `--keep-history`

## Configuration Items' Description
### Parameters
#### Global parameters
| Name                        | Description                                                                       | Value |
| --------------------------- | --------------------------------------------------------------------------------- | ----- |
| `global.resources.limits`   | The resources limits for the ShardingSphere-Proxy, MySQL, ZooKeeper. containers    | `{}`  |
| `global.resources.requests` | The requested resources for the ShardingSphere-Proxy, MySQL, ZooKeeper containers. | `{}`  |

#### MySQL parameters
| Name                   | Description                                      | Value |
| ---------------------- | ------------------------------------------------ | ----- |
| `mysql.enabled`        | Enable MySQL sub-charts dependency.               | `TRUE`  |
| `mysql.storageclass`   | Storage class needed by MySQL persistent storage. | `nil`   |
| `mysql.storagerequest` | Space for MySQL persistent storage.               | `nil`   |

#### ZooKeeper parameters
| Name                       | Description                                           | Value  |
| -------------------------- |-------------------------------------------------------| ------ |
| `zookeeper.enabled`        | Enable ZooKeeper sub-charts dependency.               | `TRUE` |
| `zookeeper.storageclass`   | Storage class needed by ZooKeeper persistent storage. | `nil`  |
| `zookeeper.storagerequest` | Space for ZooKeeper persistent storage.               | `nil`  |

#### ShardingSphere-Proxy parameters
| Name                       | Description                                                                                                      | Value  |
| -------------------------- |------------------------------------------------------------------------------------------------------------------| ------ |
|`image.repository`       | ShardingSphere-Proxy's image name. The default setting is to pull it from the Apache official-images repository. | `apache/shardingsphere-proxy` |
| `image.pullPolicy` | The policy for pulling an image.                                                                                 | `IfNotPresent` |
| `image.tag`| Image tag.                                                                                                       |`5.1.0`  |
| `replicas`| Number of cluster-mode replicas in ShardingSphere-Proxy.                                                         |`3`  |
|`service.type`| ShardingSphere-Proxy network mode.                                                                               |`NodePort`|
| `mysqlconnector.enabled`| MySQL connector enabled.                                                                                         |`TRUE`  |
| `mysqlconnector.version` | MySQL connector Version                                                                                          | `5.1.49`                      |
| `proxyport`| start port                                                                                                       |`3307` |

### ShardingSphere-Proxy config.yaml && server.yaml configuration
For more configuration information, please refer to the following link: [YAML Configuration :: ShardingSphere (apache.org)](https://shardingsphere.apache.org/document/5.1.0/en/user-manual/shardingsphere-jdbc/yaml-config/)

