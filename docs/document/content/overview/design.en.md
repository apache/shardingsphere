+++
pre = "<b>1.2 </b>"
title = "Design Philosophy"
weight = 2
chapter = true
+++

ShardingSphere adopts the database plus design philosophy, which is committed to building the standards and ecology of the upper layer of the database and supplementing the missing capabilities of the database in the ecology.

![Overview](https://shardingsphere.apache.org/document/current/img/overview.en.png)

## Connect: Create database upper level standard

 Through flexible adaptation of database protocols, SQL dialects, and database storage, it can quickly build standards on top of multi-modal heterogeneous databases, while providing standardized connection mode for applications through built-in DistSQL.

## Enhance: Database computing enhancement engine

Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).

## Pluggable: Building database function ecology

The pluggable architecture of Apache ShardingSphere is composed of three layers - L1 Kernel Layer, L2 Feature Layer and L3 Ecosystem Layer.

### L1 Kernel Layer

An abstraction of databases' basic capabilities.
All the components are required and the specific implementation method can be replaced thanks to plugins.
It includes a query optimizer, distributed transaction engine, distributed execution engine, permission engine and scheduling engine.

### L2 Feature Layer

Used to provide enhancement capabilities.
All components are optional, allowing you to choose whether to include zero or multiple components.
Components are isolated from each other, and multiple components can be used together by overlaying.
It includes data sharding, read/write splitting, database high availability, data encryption and shadow database and so on.
The user-defined feature can be fully customized and extended for the top-level interface defined by Apache ShardingSphere without changing kernel codes.

### L3 Ecosystem Layer

It is used to integrate and merge the current database ecosystems.
The ecosystem layer includes database protocol, SQL parser and storage adapter, corresponding to the way in which Apache ShardingSphere provides services by database protocol, the way in which SQL dialect operates data, and the database type that interacts with storage nodes.
