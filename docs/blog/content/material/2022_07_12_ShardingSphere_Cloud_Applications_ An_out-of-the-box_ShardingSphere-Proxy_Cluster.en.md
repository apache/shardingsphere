+++ 
title = "ShardingSphere Cloud Applications: An out-of-the-box ShardingSphere-Proxy Cluster"
weight = 67
chapter = true 
+++

The [Apache ShardingSphere v5.1.2](https://shardingsphere.apache.org/document/5.1.2/en/overview/) update introduces three new features, one of which allows using the [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) chart to rapidly deploy a set of ShardingSphere-Proxy clusters in a cloud environment. This post takes a closer look at this feature.

## Background and Pain Points

In a cloud-native environment, an application can be deployed in batches in multiple different environments. It is difficult to deploy it into a new environment by reusing the original `YAML`.

When deploying [Kubernetes](https://kubernetes.io/) software, you may encounter the following problems:

How to manage, edit and update these scattered Kubernetes application configuration files?
How to manage a set of related configuration files as an application?
How to distribute and reuse a Kubernetes application configuration?
The above problems also occur when migrating Apache SharidngSphere-Proxy from [Docker](https://www.docker.com/) or virtual machine to Kubernetes.

Due to the flexibility of Apache ShardingSphere-Proxy, a cluster may require multiple Apache ShardingSphere-Proxy replicas. In the traditional deployment model, you need to configure a separate deployment file for each replica. For deployment without version control, the system may fail to roll back quickly during the upgrade, which may affect application stability.

Today, there usually is more than one cluster for enterprises. It is a challenge for the traditional deployment model without version control to reuse configuration across multiple clusters while ensuring configuration consistency when producing and testing clusters as well as guaranteeing the correctness of the test.

## Design objective

As Apache ShardingSphere-Proxy officially supports standardized deployment on the cloud for the first time, choosing the deployment mode is crucial. We need to consider the ease of use, reuse, and compatibility with subsequent versions.

After investigating several existing Kubernetes deployment modes, we finally chose to use [Helm](https://helm.sh/) to make a chart for Apache ShardingSphere-Proxy and provide it to users. We aim to manage the deployment of Apache ShardingSphere-Proxy so that it can be versioned and reusable.

## Design content

[Helm](https://helm.sh/) manages the tool of the Kubernetes package called `chart`. Helm can do the following things:

Create a new `chart`
Package `chart` as an archive (tgz) file.
Interact with the repository where `chart` is stored.
Install and uninstall `chart` in an existing Kubernetes cluster.
Manage the release cycle of `chart` installed together with Helm.
Using Helm to build an Apache ShardingSphere-Proxy cloud-deployed chart will significantly simplify the deployment process in the Kubernetes environment for users. It also enables Apache ShardingSphere-Proxy to replicate quickly between multiple environments.

Currently, the deployment of Apache ShardingSphere-Proxy depends on the registry, and the deployment of the [ZooKeeper](https://zookeeper.apache.org/) cluster is also supported in the Apache ShardingSphere-Proxy chart.

This provides users with a one-stop and out-of-the-box experience. An Apache ShardingSphere-Proxy cluster with governance nodes can be deployed in Kubernetes with only one command, and the governance node data can be persisted by relying on the functions of Kubernetes.

## Quick start guide

A [quick start manual](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/startup/helm/) is provided in the V5.1.2 documentation, detailing how to deploy an Apache ShardingSphere cluster with default configuration files.

Below we will use the source code for installation and make a detailed description of the deployment of an Apache ShardingSphere-Proxy cluster in the Kubernetes cluster.

**Set up the environment**
Before deploying, we need to set up the environment. Apache ShardingSphere-Proxy charts require the following environments:

- Kubernetes cluster 1.18+
- kubectl 1.18+
- Helm 3.8.0+
The above need to be installed and configured before getting started.

**Prepare charts source code**
Download Apache ShardingSphere-Proxy charts in the [repository](https://shardingsphere.apache.org/charts/).

```bash
helm repo add shardingsphere https://shardingsphere.apache.org/charts
 helm pull shardingsphere/apache-shardingsphere-proxy
 tar -zxvf apache-shardingsphere-proxy-1.1.0-chart.tgz
 cd apache-shardingsphere-proxy
```
Apache ShardingSphere-Proxy charts configuration
Configure `values.yaml` file.

Modify the following code：

```yaml
governance:
   ...
   zookeeper:
     replicaCount: 1
   ...
 compute:
   ...
   serverConfig: ""
```
into：

```yaml
governance:
   ...
   zookeeper:
     replicaCount: 3
   ...
 compute:
   ...
   serverConfig:
     authority:
       privilege:
         type: ALL_PRIVILEGES_PERMITTED
       users:
       - password: root
         user: root@%
     mode:
       overwrite: true
       repository:
         props:
           maxRetries: 3
           namespace: governance_ds
           operationTimeoutMilliseconds: 5000
           retryIntervalMilliseconds: 500
           server-lists: "{{ printf \"%s-zookeeper.%s:2181\" .Release.Name .Release.Namespace }}"
           timeToLiveSeconds: 600
         type: ZooKeeper
       type: Cluster
```
**⚠️ Remember to maintain the indentation**

For the resmaining configurations, see the [configuration items in the document](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/startup/helm/#%E9%85%8D%E7%BD%AE%E9%A1%B9).

## Install Apache ShardingSphere-Proxy & ZooKeeper cluster

Now, the folder level is:

```
helm
 ├── apache-shardingsphere-proxy
 ...
 |   |
 │   └── values.yaml
 └── apache-shardingsphere-proxy-1.1.0-chart.tgz
```

Return to the `helm` folder and install the Apache ShardingSphere-Proxy & ZooKeeper cluster.

```
helm install shardingsphere-proxy apache-shardingsphere-proxy
```

The ZooKeeper & Apache ShardingSphere-Proxy cluster is deployed in the default namespace of the cluster:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/qgiae1qf2ryjo6u3cezg.png)
 

**Test simple functions**
Using kubectl forward for local debugging:

```
kubectl port-forward service/shardingsphere-proxy-apache-shardingsphere-proxy 3307:3307
```

Create backend Database:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/n9h3uiw5x1u5kx17rvv0.png)
 

Use [MySQL](https://www.mysql.com/) client to connect and use [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/) to add data sources:

`mysql -h 127.0.0.1 -P 3307 -uroot -proot`
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/favmpbg6kmxmk9kyjr2m.png)
 

Create rule:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/wg1xtdrnr4qoa5micudq.png)
 

Write data and query result:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/mei7d27czrq0z3xv81sh.png)
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/14j4psiw13q39wcpd9ih.png)

## Upgrade

Apache ShardingSphere-Proxy can be quickly upgraded with Helm.

`helm upgrade shardingsphere-proxy apache-shardingsphere-proxy`
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/2zzkgi71sz5tnjqumspp.png)
 

## Rollback

If an error occurs during the upgrade, you can use the `helm rollback` command to quickly roll back the upgraded `release`.

`helm rollback shardingsphere-proxy`
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/p28l2jlbg1dt0pthbesh.png)
 

## Clean Up

After the experience, the `release` can be cleaned up quickly using the helm `uninstall` command:

`helm uninstall shardingsphere-proxy`

All resources installed for Helm will be deleted.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/vsg3omz4jb2jfkx0olsv.png)
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/xgunqw1hmaaq8n17ddix.png)

## Conclusion

Apache ShardingSphere-Proxy Charts can be used to quickly deploy a set of Apache ShardingSphere-Proxy clusters in the Kubernetes cluster.

This simplifies the configuration of `YAML` for ops & maintenance teams during the migration of Apache ShardingSphere-Proxy to the Kubernetes environment.

With version control, the Apache ShardingSphere-Proxy cluster can be easily deployed, upgraded, rolled back, and cleaned up.

In the future, our community will continue to iterate and improve the Apache ShardingSphere-Proxy chart.

## Project Links:

[ShardingSphere Github
](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)
[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)
