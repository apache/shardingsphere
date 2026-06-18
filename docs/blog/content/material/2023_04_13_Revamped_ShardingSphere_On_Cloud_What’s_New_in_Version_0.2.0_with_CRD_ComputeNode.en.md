+++
title = "Revamped ShardingSphere-On-Cloud: What’s New in Version 0.2.0 with CRD ComputeNode"
weight = 96
chapter = true 
+++

Apache ShardingSphere-On-Cloud recently released version 0.2.0, which includes a new CRD ComputeNode for ShardingSphere Operator. This new feature enables users to define computing nodes fully within the ShardingSphere architecture.

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md1.jpg)

# Introduction to ComputeNode

In the classic architecture of Apache ShardingSphere, computing nodes, storage nodes, and governance nodes are the primary components.


The computing node refers to the ShardingSphere Proxy, which acts as the entry point for all data traffic and is responsible for data governance capabilities such as distribution and balancing.


The storage node is the environment that stores various ShardingSphere metadata, such as sharding rules, encryption rules, and read-write splitting rules. Governance node components include Zookeeper, Etcd, etc.

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md2.jpg)

In version 0.1.x of ShardingSphere Operator, two CRD components, Proxy and ProxyServerConfig, were introduced to describe the deployment and configuration of ShardingSphere Proxy, as shown in the figure below.

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md3.jpg)

These components enable basic maintenance and deployment capabilities for ShardingSphere Proxy, which are sufficient for Proof of Concept (PoC) environments.


However, for the Operator to be useful in production environments, it must be able to manage various scenarios and problems. These scenarios include cross-version upgrades, smooth session shutdowns, horizontal elastic scaling with multiple metrics, location-aware traffic scheduling, configuration security, cluster-level high availability, and more.


To address these management capabilities, ShardingSphere-On-Cloud has introduced ComputeNode, which can handle these functions within a specific group of objects. The first object is ComputeNode, as shown in the figure below:

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md4.jpg)


Compared with Proxy and ProxyServerConfig, ComputeNode brings changes such as cross-version upgrades, horizontal elastic scaling, and configuration security. ComputeNode is still in the v1alpha1 stage and needs to be enabled through a feature gate.

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md5.jpg)


# ComputeNode Practice

## Quick Installation of ShardingSphere Operator
To quickly set up a ShardingSphere Proxy cluster using ComputeNode, execute the following helm command:

```Bash
helm repo add shardingsphere-on-cloud https://charts.shardingsphere.io
helm install shardingsphere-on-cloud/shardingsphere-operator - version 0.2.0 - generate-name
```

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md6.jpg)

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md7.jpg)

- The deployment status of the ShardingSphere Proxy cluster can be checked using `kubectl get pod`:

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md8.jpg)

Now, a complete cluster managed by ShardingSphere Operator has been deployed.


**Checking the ShardingSphere Proxy cluster status using** `kubectl get`

ShardingSphere Proxy cluster’s status can be checked using kubectl get pod. The ComputeNode status includes READYINSTANCES, PHASE, CLUSTER-IP, SERVICEPORTS, and AGE.

READYINSTANCES represent the number of ShardingSphere Pods in the Ready state, PHASE represents the current cluster status, CLUSTER-IP represents the ClusterIP of the current cluster Service, SERVICEPORTS represents the port list of the current cluster Service, and AGE represents the creation time of the current cluster.


```
kubectl get computenode
```
![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md9.jpg)

**Quickly Scale the ShardingSphere Proxy Cluster Using** `kubectl scale`

ComputeNode supports the Scale subresource, which enables you to manually scale up using the kubectl scale command with the — replicas parameter.

If the ComputeNode installed by the operator’s default charts cannot meet your usage scenario, you can write a ComputeNode yaml file and submit it to Kubernetes for deployment.

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md10.jpg)

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md11.jpg)

**Customizing ComputeNode configuration**
If the ComputeNode installed by the operator’s default charts cannot meet the usage scenario, you need to write a ComputeNode yaml file by yourself and submit it to Kubernetes for deployment:

```
apiVersion: shardingsphere.apache.org/v1alpha1
kind: ComputeNode
metadata:
  labels:
    app: foo
  name: foo
spec:
  storageNodeConnector:
    type: mysql
    version: 5.1.47
  serverVersion: 5.3.1
  replicas: 1
  selector:
    matchLabels:
      app: foo
  portBindings:
  - name: server
    containerPort: 3307
    servicePort: 3307
    protocol: TCP
  serviceType: ClusterIP
  bootstrap:
    serverConfig:
      authority:
        privilege:
          type: ALL_PERMITTED
        users:
        - user: root%
          password: root
      mode:
        type: Cluster
        repository:
          type: ZooKeeper
          props:
            timeToLiveSeconds: "600"
            server-lists: shardingsphere-operator-zookeeper.default:2181
            retryIntervalMilliseconds: "500"
            operationTimeoutMilliseconds: "5000"
            namespace: governance_ds
            maxRetries: "3"
      props:
        proxy-frontend-database-protocol-type: MySQL
```


Save the above configuration as foo.yml and execute the following command to create it:

```
kubectl apply -f foo.yml
```

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md12.jpg)

![img](https://shardingsphere.apache.org/blog/img/2023_04_13_Revamped_ShardingSphere_On_Cloud_What’s_New_in_Version_0.2.0_with_CRD_ComputeNode.en.md13.jpg)

The above example can be directly found in our [Github repository](https://github.com/apache/shardingsphere-on-cloud/tree/main/examples/operator).

# Other Improvements

Other improvements in version 0.2.0 include support for rolling upgrade parameters in the ShardingSphereProxy CRD’s annotations, fixed issues with readyNodes and Conditions in the ShardingSphereProxy Status field in certain scenarios, and more:

- Introduced the scale subresource to ComputeNode to support kubectl scale #189
- Separated the construction and update logic of ComputeNode and ShardingSphereProxy #182
- Wrote NodePort back to ComputeNode definition #187
- Fixed NullPointerException caused by non-MySQL configurations #179
- Refactored Manager configuration logic and separated command line configuration #192
- Fixed Docker build process in CI #173

# Wrap Up

In conclusion, the new CRD ComputeNode for ShardingSphere Operator in version 0.2.0 provides various management capabilities that are essential in production environments.

With ComputeNode, users can define computing nodes fully within the ShardingSphere architecture and manage various scenarios and problems, including cross-version upgrades, smooth session shutdowns, horizontal elastic scaling, location-aware traffic scheduling, configuration security, and cluster-level high availability.

# Community Contribution

This ShardingSphere-On-Cloud 0.2.0 release is the result of 22 merged PRs, made by 2 contributors. Thank you for your love & passion for open source!

GitHub ID:

- mlycore
- xuanyuan300

# Relevant Links

- [ShardingSphere-on-Cloud](https://github.com/apache/shardingsphere-on-cloud)
- [ComputeNode Issue](https://github.com/apache/shardingsphere-on-cloud/issues/166)
- [Slack community](https://apacheshardingsphere.slack.com/?redir=%2Fssb%2Fredirect)
- [Apache ShardingSphere Useful Links](https://faun.pub/)





