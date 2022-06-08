+++
title = "Core Concept"
weight = 1
+++

## Circuit Breaker

Fuse connection between Apache ShardingSphere and the database.
When an Apache ShardingSphere node exceeds the max load, stop the node's access to the database, 
so that the database can ensure sufficient resources to provide services for other Apache ShardingSphere nodes.

## Request Limit

In the face of overload requests, open request limiting to protect some requests can still respond quickly.
