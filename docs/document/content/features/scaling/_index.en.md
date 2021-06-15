+++
pre = "<b>3.5. </b>"
title = "Scaling"
weight = 5
chapter = true
+++

## Background
 
Apache ShardingSphere provides data sharding capability, which can split data to different databases.

For applications that have been running with stand-alone database, there is a problem how to migrate data to sharding data nodes safely and simply;
For some applications which have used Apache ShardingSphere, the rapid growth of data may also cause a single data node or even the entire data nodes to reach a bottleneck.
How to expand their data nodes for Apache ShardingSphere cluster also became a problem.

## Introduction

ShardingSphere-Scaling is a common solution for migrating or scaling data in Apache ShardingSphere since **4.1.0**.

![Scaling Overview](https://shardingsphere.apache.org/document/current/img/scaling/scaling-overview.en.png)

## Challenges

Apache ShardingSphere provides users with great freedom in sharding strategies and algorithms, but it gives a great challenge to scaling.
So it's the first challenge that how to find a way can support kinds of sharding strategies and algorithms and scale data nodes efficiently.

What's more, During the scaling process, it should not affect the running applications. 
So It is another big challenge for scaling to reduce the time window of data unavailability during the scaling as much as possible, or even completely unaware.

Finally, scaling should not affect the existing data. How to ensure the availability and correctness of data is the third challenge of scaling.

## Goal

The main design goal of sharding scaling is providing a common Apache ShardingSphere scaling solution which can support kinds of sharding strategies and reduce the impact as much as possible during scaling.

## Status

current is in **alpha** development.

![Roadmap](https://shardingsphere.apache.org/document/current/img/scaling/roadmap.en.png)
