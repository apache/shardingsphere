+++
title = "Core Concept"
weight = 1
+++

## high Availability Type

Apache ShardingSphere does not provide high availability solution of database, it reuses 3rd party high availability solution and auto-detect switch of primary and replica databases.
Specifically, the ability of Apache ShardingSphere provided is database discovery, detect the primary and replica databases automatically, and updates the connection of compute nodes to the databases.

## Dynamic Readwrite-Splitting

When high availability and readwrite-splitting are used together, there is unnecessary to configure specific primary and replica databases for readwrite-splitting.
Highly available data sources will update the primary and replica databases of readwrite-splitting dynamically, and route the query and update SQL correctly.
