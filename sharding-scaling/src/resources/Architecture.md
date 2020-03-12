# Architecture

## Architecture
![Architecture](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/ControllerProcess.png)

Description：
1. Job is submitted by user. In sharding-scaling, a Job is divided into multiple tasks, and the tasks are responsible for the real data migration. `ScalingJobController` is responsible for managing the jobs lifecycle, and `SyncTaskController` is responsible for managing the tasks lifecycle.
2. `Reader` is responsible for reading data from the source, `Channel` is responsible for data transmission, and `Writer` is responsible for reading data from `Channel` and writing them to destination.

## Design
History data migration：Traversing data based on the JDBC interface, It has the advantages of simple implementation and good compatibility.

Real-time data migration：

- MySQL：Masquerading as a slave, read and parse the binlog from source for real-time data migration.
- PostgreSQL：Using [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) for real-time data migration.

## Roadmap
![Roadmap](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/roadmap.png)

The above is our planning roadmap, the sharding-scaling will be more convenient for user to scale up the data in the future. These plans include auto-switch configuration, breakpoint continuation, and data correctness comparison. And all of these operations can be done through the UI interface：

![Workflow](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/workflow.png)
