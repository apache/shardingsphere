+++
pre = "<b>3.4. </b>"
title = "Governance"
weight = 4
chapter = true
+++

## Background

As the scale of data continues to expand, a distributed approach using multi-node clusters has gradually become a trend. In this case, how to efficiently and automatically manage cluster nodes, realize the collaborative work of different nodes, configuration consistency, state consistency, high availability, observability, etc., has become a challenge.

This section includes three modules: governance, cluster management and observability.

## Challenges

The challenges of distributed governance mainly lie in the complexity of cluster management and how to connect various third-party integrated components in a unified and standard manner.

The complexity of integrated management is reflected in that on the one hand, we need to manage the status of all nodes in a unified manner and can detect the latest changes in real time, whether it is the underlying database node, middleware or business system node, to further provide the basis for the control and scheduling of the cluster. In this regard, we use the cluster topology state diagram to manage the cluster state and the heartbeat detection mechanism to achieve state detection and update.

On the other hand, the unified coordination and the synchronization of policies and rules between different nodes also require us to design a set of global event notification mechanisms and distributed coordination lock mechanisms for exclusive operations in distributed situations. In this regard, we use Zookeeper/Etcd to achieve configuration synchronization, notification of state changes and distributed locks to control exclusive operations.

At the same time, since the governance function itself can use appropriate third-party components as basic services, we need to abstract a unified interface, unify the standard calling APIs of various components and dock to the governance function module.

Finally, for the requirements of manageability and observability, we need to improve the functions of querying, operating and controlling the system through the UI, further improving the support for tracing and APM.

## Goal

For the governance function, the goals are as follows:

- Realize the configuration center: support Zookeeper/etcd/Apollo/Nacos, manage the configuration of data sources, rules and policies.
- Realize the registration center: support Zookeeper/etcd, manage the status of each Proxy example.
- Implement metadata center: support Zookeeper/etcd, manage metadata of LogicSchema.

For observability, the goals are as follows:

- Support OpenTracing/Skywalking integration and realize call chain tracking.