+++
title = "Core Concept"
weight = 1
+++

## High Availability Type

Apache ShardingSphere does not provide database high availability capability. It senses the change of databases' primary-secondary relationship through a third-party provided high availability solution.
Specifically, ShardingSphere is capable of finding databases, automatically sensing the primary/secondary database relationship, and correcting compute nodes' connections to databases.

## Dynamic Read/Write Splitting

When high availability and read/write splitting are adopted together, it is not necessary to configure specific primary and secondary databases for read/write splitting. 
Highly available data sources dynamically correct the primary/secondary relationship of read/write splitting and properly channel read/write traffic.
