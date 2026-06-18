+++
title = "ShardingSphere Operator Practical Guide"
weight = 91
chapter = true 
+++

# Apache ShardingSphere-on-Cloud sub-project

ShardingSphere-on-Cloud is a sub-project of Apache ShardingSphere for cloud solutions, covering verything from automated deployment scripts to virtual machines in AWS, GCP, AliCloud, Huawei Cloud cloud environments such as CloudFormation Stack templates, Terraform and more.

The project also covers tools such as Helm Charts, Operator, and automatic horizontal scaling in Kubernetes environments, and will gradually cover ShardingSphere high-availability, observability, and security compliance in cloud environments.

- If you want to quickly understand, validate or use the features of ShardingSphere-Proxy and don't have a Kubernetes environment, you can use AWS CloudFormation or Terraform for on-demand deployments, as described in the previous article "[Use AWS CloudFormation to create ShardingSphere HA clusters](https://shardingsphere.apache.org/blog/en/material/2022_12_13_use_aws_cloudformation_to_create_shardingsphere_ha_clusters/)".
- If you'd like to deploy in a Kubernetes environment, you can leverage the Operator feature or install ShardingSphere-Proxy directly without using Operator but with Helm Charts.

For more information, please refer to the [official documentation](https://shardingsphere.apache.org/oncloud/).

## Why Do You Need ShardingSphere Operator

Kubernetes provides a way to implement a "platform on top of a platform", the Operator pattern, which leverages a custom `CustomResourceDefinition` with the Kubernetes exposed Reconcile model framework.

The Reconcile model framework exposed by Kubernetes enables developers to quickly implement a declarative custom Ops tool for a given application, such as Prometheus-Operator, Nats-Operator, etc.

As a transparent database enhancement engine, Apache ShardingSphere supports access to any client using MySQL, PostgreSQL, openGauss protocols, with ShardingSphere-Proxy being more suitable to heterogeneous languages and high-availability scenarios.

Using Operator makes the deployment and maintenance of ShardingSphere-Proxy on Kubernetes much easier and more efficient.

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide1.png)

# Using ShardingSphere Operator: An Example

Before using ShardingSphere, you will need to prepare one or more compatible databases as storage nodes.

Detailed instructions can be found in the [official documentation](https://shardingsphere.apache.org/oncloud/current/en/operation-guide/operator/) for the ShardingSphere-on-Cloud sub-project.

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide2.png)

## Install ShardingSphere-Operator

Find the required configuration content and configuration [file directory for installation here](https://github.com/apache/shardingsphere-on-cloud/tree/main/charts/apache-shardingsphere-operator-charts).

To facilitate the installation of Operator, our community provides an online installation in addition to the source code installation. The two methods are as follows:

## Online Installation

```bash
kubectl create ns shardingsphere-operator
	helm repo add shardingsphere https://apache.github.io/shardingsphere-on-cloud
	helm repo update
	helm install demo-release shardingsphere/apache-shardingsphere-operator-charts -n shardingsphere-operator
```

Example of operation result:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide3.png)

**Note:** please refer to the official documentation for source code installation。

At this point, you can see that the operator has injected the `crd `that Operator will be working on into the Kubernetes cluster by using `kubectl get crd`:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide4.png)

As well as viewing the deployed ShardingSphere-Operator:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide5.png)

## Deploy ShardingSphere-Proxy Clusters

For a list of all the parameters that can be configured options, refer to the [documentation here](https://github.com/apache/shardingsphere-on-cloud/tree/main/charts/apache-shardingsphere-operator-charts#parameters).

**Tip:** you need to provide an accessible ZooKeeper cluster before you can run ShardingSphere-Proxy.

To run a ShardingSphere-Proxy you need to write two `CustomResourceDefinition` files: `shardingsphereproxy.yaml` and `shardingsphereproxyserverconfigs.yaml`, examples of which are as follows:

```yaml
# shardingsphereproxy.yaml
apiVersion: shardingsphere.apache.org/v1alpha1
kind: ShardingSphereProxy
metadata:
  labels:
    app: shardingsphere-proxy
  name: shardingsphere-proxy
  namespace: shardingsphere-demo
spec:
  mySQLDriver:
    version: 5.1.47
  port: 3307
  proxyConfigName: shardingsphere-proxy-configuration
  replicas: 3
  serviceType:
    type: ClusterIP
  version: 5.3.0
---
# shardingsphereproxyserverconfigs.yaml
apiVersion: shardingsphere.apache.org/v1alpha1
kind: ShardingSphereProxyServerConfig
metadata:
  labels:
    app: shardingsphere-proxy
  name: shardingsphere-proxy-configuration
  namespace: shardingsphere-demo
spec:
  authority:
    privilege:
      type: ALL_PERMITTED
    users:
    - password: root
      user: root@%
  mode:
    repository:
      props:
        maxRetries: 3
        namespace: governance_ds
        operationTimeoutMilliseconds: 5000
        retryIntervalMilliseconds: 500
        server-lists: shardingsphere-proxy-zookeeper.shardingsphere:2181 # This is an example and it should be modified to the real scenario.
        timeToLiveSeconds: 600
      type: ZooKeeper
    type: Cluster
  props:
    proxy-frontend-database-protocol-type: MySQL
```

Operation examples:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide6.png)

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide7.png)

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide8.png)

Then you can check the status of the Pod running under the `shardingsphere-demo` namespace:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide9.png)

Related resources created by the Operator can also be found at:

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide10.png)

# Configure ShardingSphere-Proxy for Data Encryption Capabilities

ShardingSphere supports DistSQL to help DBAs quickly set up and run storage node registration, rule configuration and more. You can find out more [here](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/).

As enterprises continue to transform digitally, increasing amounts of user data are being communicated between all kinds of businesses. Various countries and regions have introduced regulations and frameworks to ensure data security, such as the EU's GDPR and others.

Apache ShardingSphere offers multiple functions in data security such as data encryption and decryption.

Followings are examples of data encryption:

1. Register storage nodes

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide11.png)

2. Create encryption rules

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide12.png)

3. Create logical tables

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide13.png)

4. View physical table properties

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide14.png)

5. Logical table insertion and query

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide15.png)

6. Data source physical table query

You can find further details of the encryption features [here](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/usage/encrypt-rule/).

# Good news! Our new ShardingSphere-on-Cloud is now live!

With the migration of the ShardingSphere-on-Cloud sub-project to [ShardingSphere-on-Cloud Github](https://github.com/apache/shardingsphere-on-cloud), we designed and launched a new website.

As the project grows, this will facilitate interested users or contributors better understand the project and participate in the community, including documentation for each version, an introduction to the community, information about the Apache Foundation, community updates, information about the Apache Foundation, and etc.

![img](https://shardingsphere.apache.org/blog/img/2023_03_14_ShardingSphere_Operator_Practical_Guide16.png)

# Relevant Links:

🔗 [ShardingSphere-on-Cloud Github](https://github.com/apache/shardingsphere-on-cloud)

🔗 [ShardingSphere-on-Cloud Official Website](https://shardingsphere.apache.org/oncloud/)

🔗 [Apache ShardingSphere GitHub](https://github.com/apache/shardingsphere)

🔗 [Apache ShardingSphere Official Website](https://shardingsphere.apache.org/)

🔗 [Apache ShardingSphere Slack Channel](https://apacheshardingsphere.slack.com/)