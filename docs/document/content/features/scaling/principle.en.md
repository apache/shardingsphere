+++
pre = "<b>3.5.2. </b>"
title = "Principle"
weight = 2
+++

## Principle Description

Consider about these challenges of ShardingSphere-Scaling, the solution is: Use two database clusters temporarily, and switch after the scaling is completed.

![Scaling Principle Overview](https://shardingsphere.apache.org/document/current/img/scaling/scaling-principle-overview.en.png)

Advantages:

1. No effect for origin data during scaling.
1. No risk for scaling failure.
1. No limited by sharding strategies.

Disadvantages：

1. Redundant servers during scaling.
1. All data needs to be moved.

ShardingSphere-Scaling will analyze the sharding rules and extract information like datasource and data nodes.
According the sharding rules, ShardingSphere-Scaling create a scaling job with 4 main phases.

1. Preparing Phase.
1. Inventory Phase.
1. Incremental Phase.
1. Switching Phase.

![Workflow](https://shardingsphere.apache.org/document/current/img/scaling/workflow.en.png)

## Phase Description

### Preparing Phase

ShardingSphere-Scaling will check the datasource connectivity and permissions, statistic the amount of inventory data, record position of log, 
shard tasks based on amount of inventory data and the parallelism set by the user.

### Inventory Phase

Executing the Inventory data migration tasks sharded in preparing phase.
ShardingSphere-Scaling uses JDBC to query inventory data directly from data nodes and write to the new cluster using new rules.

### Incremental Phase

The data in data nodes is still changing during the inventory phase, so ShardingSphere-Scaling need to synchronize these incremental data to new data nodes.
Different databases have different implementations, but generally implemented by change data capture function based on replication protocols or WAL logs.

- MySQL：subscribe and parse binlog.
- PostgreSQL：official logic replication [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html).

These captured incremental data, Apache ShardingSphere also write to the new cluster using new rules.

### Switching Phase

In this phase, there may be a temporary read only time, make the data in old data nodes static so that the incremental phase complete fully.
The read only time is range seconds to minutes, it depends on the amount of data and the checking data.
After finished, Apache ShardingSphere can switch the configuration by register-center and config-center, make application use new sharding rule and new data nodes.