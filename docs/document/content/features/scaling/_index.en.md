+++
pre = "<b>4.7. </b>"
title = "Scaling"
weight = 7
chapter = true
+++

## Background

There is a problem which how to migrate data from stand-alone database to sharding data nodes safely and simply;
For applications which have used Apache ShardingSphere, scale out elastically is a mandatory requirement.

## Challenges

Apache ShardingSphere provides great flexibility in sharding algorithms, but it gives a great challenge to scaling out.
So it's the first challenge that how to find a way can support kinds of sharding algorithms and scale data nodes efficiently.

What's more, During the scaling process, it should not affect the running applications. 
So It is another big challenge for scaling to reduce the time window of data unavailability during the scaling as much as possible, or even completely unaware.

Finally, scaling should not affect the existing data. How to ensure the availability and correctness of data is the third challenge of scaling.

ShardingSphere-Scaling is a common solution for migrating or scaling data.

![Overview](https://shardingsphere.apache.org/document/current/img/scaling/overview.en.png)

## Goal

**The main design goal of ShardingSphere-Scaling is providing common solution which can support kinds of sharding algorithm and reduce the impact as much as possible during scaling.**

## Status

ShardingSphere-Scaling since version **4.1.0**.
Current status is in **alpha** development.
