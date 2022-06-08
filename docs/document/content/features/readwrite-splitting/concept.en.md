+++
title = "Core Concept"
weight = 1
+++

## Primary Database

It refers to the database used in data insertion, update and deletion. It only supports single primary database for now.

## Replica Database

It refers to the database used in data query. It supports multiple replica databases.

## Primary Replica Replication

It refers to the operation to asynchronously replicate data from the primary database to the replica database. 
Because of the asynchrony of primary-replica synchronization, there may be short-time data inconsistency between them.  

## Load Balance Strategy

Through this strategy, queries separated to different replica databases.
