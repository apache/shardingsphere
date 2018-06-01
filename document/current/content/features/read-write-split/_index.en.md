+++
pre = "<b>3.2. </b>"
title = "Read-write splitting"
weight = 2
chapter = true
+++

## Background

To face for more and more page views on website, databases became performance bottleneck. 
For some systems which have huge concurrent query requests and less update requests, split single database to master database and slave database, master database deal with DML operation, slave database deal with DQL operation, can avoid lock and improve system performance.
Use master database with multiple slave databases model, can share query requests on multiple data replica, improve throughput further.
Use multiple master databases with multiple slave databases model, can improve system availability. Even one of database physical destroy, system also run lossless.

Read-write splitting can improve throughput and availability for system, but data inconsistent will occur. This problem include data inconsistent between master databases each other, and between master master databases and slave databases. Same with data sharding, developers and operators also need to face complicated database environments. **The objective of read-write splitting middleware is let user to use complicated database like a single database.** 
