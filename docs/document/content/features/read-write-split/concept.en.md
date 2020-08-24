+++
pre = "<b>3.3.1. </b>"
title = "Core Concept"
weight = 1
+++

## Master Database

It refers to the database used in data insertion, update and deletion. It only supports single master database for now.

## Slave Database

It refers to the database used in data query. It supports multiple slave databases.

## Master-Slave Replication

It refers to the operation to asynchronously replicate data from the master database to the slave database. Because of master-slave asynchronization, there may be short-time data inconsistency between them.  

## Load Balance Strategy

Through this strategy, queries are separated to different slave databases.