# ShardingScaling - ShardingSphere Scaling Out Component

## Overview

The following figure may clearly express this component's role:

![scale out](https://user-images.githubusercontent.com/14773179/75600294-8516d500-5ae8-11ea-9635-5656b72242e3.png)

Supplementary instruction about the figure:

1. Support to migrate tables in sharding rule configuration only, can't support to migrate specified tables.

2. The process of migration splits into two steps, inventory data migration and incremental data synchronization.

  - During inventory data migration, ShardingSphere-Scaling use `select *` statement to acquire the data, and use `insert` statement to migrate the data to the target;
   
  - During incremental data migration, ShardingSphere-Scaling use binlog to migrate the data, and mark the binlog position before migration.

3. If the table in the source schema has primary key, ShardingSphere-Scaling can migrate it concurrently using `where predication`.

## Requirement

MySQL: 5.1.15 ~ 5.7.x

ShardingSphere-Proxy: 3.x ~ 5.x

## How to Run

Refer to the [Quick Start](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-scaling-quick-start/)

## For more documents

[Overview](https://shardingsphere.apache.org/document/current/en/features/scaling/)

[Principle](https://shardingsphere.apache.org/document/current/en/features/scaling/principle/)
