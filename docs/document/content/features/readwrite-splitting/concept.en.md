+++
title = "Core Concept"
weight = 1
+++

## Primary database
The primary database is used to add, update, and delete data operations. Currently, only single primary database is supported.

## Secondary database
The secondary database is used to query data operations and multi-secondary databases are supported.

## Primary-Secondary synchronization
It refers to the operation of asynchronously synchronizing data from a primary database to a secondary database. Due to the asynchronism of primary-secondary synchronization,
data from the primary and secondary databases may be inconsistent for a short time.

## Load balancer policy
Channel query requests to different secondary databases through load balancer policy.
