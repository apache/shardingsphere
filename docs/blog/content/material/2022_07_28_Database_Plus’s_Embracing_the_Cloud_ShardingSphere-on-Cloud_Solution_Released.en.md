+++ 
title = "Database Plus’s Embracing the Cloud: ShardingSphere-on-Cloud Solution Released"
weight = 70
chapter = true 
+++

As a follower of the [Database Plus](https://faun.pub/whats-the-database-plus-concepand-what-challenges-can-it-solve-715920ba65aa?source=your_stories_page-------------------------------------)
development concept, [ShardingSphere](https://shardingsphere.apache.org/) has successfully passed production environment tests across multiple industries and gained popularity among community enthusiasts.

As [Kubernetes](https://kubernetes.io/) has become the de facto standard for container orchestration, the cloud-native concept has gone viral in the tech world. Apache ShardingSphere, as a database enhancement engine with an open ecosystem, has many similarities with Kubernetes in its design concept. So [SphereEx](https://www.sphere-ex.com/en/) took the lead in launching ShardingSphere-on-Cloud, its cloud solution for ShardingSphere. Database Plus’ trip to the Cloud has officially begun.

The ShardingSphere-on-Cloud repository will release its best practices including configuration templates, automation scripts, deployment tools, and Kubernetes Operator for Apache ShardingSphere on the cloud. Currently, ShardingSphere-on-Cloud has released V0.1.0, which contains the smallest available version of ShardingSphere Operator.

## ShardingSphere Operator
One of the key reasons Kubernetes has become the de facto standard for cloud-native orchestration tools is its great extensibility, enabling developers to quickly build platforms on top of other platforms.

For all kinds of software trying to run on Kubernetes, the Kubernetes Operator mode can be used to work with the `CustomResourceDefinition` framework to quickly build automatic maintenance capabilities.

Last month, Apache ShardingSphere v5.1.2 released [Helm Charts-based package management](https://faun.pub/shardingsphere-cloud-applications-an-out-of-the-box-shardingsphere-proxy-cluster-9fd7209b5512?source=your_stories_page-------------------------------------) capability. Helm Charts helps us deal with the first step, that is, how to describe and deploy ShardingSphere in Kubernetes.

The second step focuses on how to manage a stateful or complex workload in Kubernetes, for which a customized management tool is needed. In this case, SphereEx’s cloud team developed the **ShardingSphere Operator** to further enhance ShardingSphere’s deployment and maintenance capabilities on top of Kubernetes.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/0hx9p0gtqft32stajecr.png)
 

ShardingSphere made the following improvements in response to challenges faced when Apache ShardingSphere migrates to the Kubernetes environment.

- **Simplified startup configuration:** ShardingSphere has strong database enhancement capabilities, and its corresponding configuration is relatively complex. In the current version, ShardingSphere Operator automates the configuration and mount behavior of the configuration. Users only need to fill in the minimum startup dependency configuration to quickly deploy and start a ShardingSphere-Proxy cluster. The runtime configuration can be implemented through [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/).

- **Automatic deployment of governance nodes:**
 ShardingSphere relies on the governance center to achieve metadata persistence and achieve metadata broadcasting among cluster nodes during operation. To optimize the user experience and deliver an out-of-the-box experience, ShardingSphere Operator can deploy the governance node with the compute node and configure dependencies based on users’ needs.

- **High availability:** to simplify maintenance in a cloud environment, it is desirable to implement stateless deployment. As a stateless compute node, [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) can work with ShardingSphere Operator to achieve multi-dimensional health detection and failover recovery.

- **Horizontal scaling:** ShardingSphere-Proxy horizontal scaling can be achieved based on CPU and memory using [HPA in Kubernetes](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/). ShardingSphere Operator will gradually support a variety of customized specifications to achieve advanced, automatic scaling that is more intelligent and stable for ShardingSphere-Proxy.
Specifically, in version 0.1.0, ShardingSphere Operator mainly supports the following capabilities:

## Helm Deployment

> **ShardingSphere-Operator Chart**

- Support the deployment of ShardingSphere-Operator.

> **ShardingSphere-Cluster Chart**

- Support the deployment of the ShardingSphere-Proxy cluster.
- Support the deployment of the [ZooKeeper](https://zookeeper.apache.org/) cluster through [Bitnami](https://bitnami.com/).
- ShardingSphere-Proxy can be automatically configured and connected to governance nodes.

> **Use Github Pages to host Charts and add repositories using `helm repo add`**

### New Features

- `shardingsphereproxy` CRD can be used to describe ShardingSphere-Proxy cluster information.
- Native ShardingSphere-Proxy `server.yaml` configuration can be used for startup.
- Support automatic scale-out for HPA configurations based on CPU specifications.
- Support automatic downloading of [MySQL](https://www.mysql.com/) drivers.
See the project [ReadMe](https://github.com/SphereEx/shardingsphere-on-cloud/blob/main/README.md) for other configuration information.

## Quick Start
You can run the following commands:

```
kubectl create ns sharding
helm repo add shardingspherecloud https://sphereex.github.io/shardingsphere-on-cloud/ 
helm install operator shardingspherecloud/shardingsphere-operator -n sharding
helm install cluster shardingspherecloud/shardingsphere-cluster -n sharding
```
The above command will install ShardingSphere Operator, ShardingSphere-Proxy cluster, and ZooKeeper cluster in the sharding Namespace according to the default configuration.

ShardingSphere-Proxy can be accessed in a cluster through Service. Then through the MySQL client, create the logical database using DistSQL, add the data source and create the corresponding rule table. Then you can start to use ShardingSphere-Proxy running on Kubernetes.

## Future Plan
ShardingSphere Operator will gradually optimize the deployment mode of ShardingSphere-Proxy, improve its automatic maintenance capabilities, and constantly polish its performance in terms of high availability and disaster recovery scenarios, striving to give users an ultimate cloud-native experience.

The ShardingSphere-on-Cloud repository will continue to take in various cloud practices from the community, to build an open ecosystem solution that is oriented to the cloud, benefits from the cloud, and can enhance database capabilities on the cloud.

If you have anything to share with us, please feel free to contact us through GitHub Issue or Apache ShardingSphere Slack.

## Project Links:

[ShardingSphere Operator GitHub](https://github.com/SphereEx/shardingsphere-on-cloud)

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter
](https://twitter.com/ShardingSphere)
[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

[GitHub Issues](https://github.com/apache/shardingsphere/issues)

[Contributor Guide](https://shardingsphere.apache.org/community/en/involved/)

[SphereEx Official Website
](https://sphere-ex.com/)

## Author

SphereEx Cloud & ShardingSphere contributor team. Focus on the R&D of Cloud solutions of ShardingSphere, the Database Mesh open source community, and the SphereEx Cloud business.
