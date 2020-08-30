+++
pre = "<b>4.3. </b>"
title = "ShardingSphere-Sidecar"
weight = 3
chapter = true
+++

## Introduction

ShardingSphere-Sidecar (TODO) defines itself as a cloud native database agent of the Kubernetes environment, in charge of all the access to the database in the form of sidecar.

It provides a mesh layer interacting with the database, we call this as `Database Mesh`.

![ShardingSphere-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-sidecar-brief.png)


## Comparison

|                         | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*     | *ShardingSphere-Sidecar* |
| :---------------------- | :-------------------- | :------------------------- | :----------------------- |
| Database                | Any                   | MySQL/PostgreSQL           | `MySQL`                  |
| Connections Count Cost  | High                  | Low                        | `High`                   |
| Supported Languages     | Java Only             | Any                        | `Any`                    |
| Performance             | Low loss              | Relatively High loss       | `Low loss`               |
| Decentralization        | Yes                   | No                         | `Yes`                    |
| Static Entry            | No                    | Yes                        | `No`                     |

The advantage of ShardingSphere-Sidecar lies in its cloud native support for Kubernetes and Mesos.