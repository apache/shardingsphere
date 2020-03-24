# ShardingScaling - ShardingSphere Scaling Out Component

## Overview

The following figure may clearly express this component's role:

![scale out](https://user-images.githubusercontent.com/14773179/75600294-8516d500-5ae8-11ea-9635-5656b72242e3.png)

Supplementary instruction about the figure:

1. Support to migrate whole tables in sharding configuration only, can't support to migrate specified tables.

2. The process of migration splits into two steps, inventory data migration and incremental data synchronization.

  - During inventory data migration, Sharding-Scaling use `select *` statement to acquire the data, and use `insert` statement to migrate the data to the target;
   
  - During incremental data migration, Sharding-Scaling use binlog to migrate the data, and mark the binlog position before migration.

3. If the table in the source schema has primary key, Sharding-Scaling can migrate it concurrently using `where predication`.

## Requirement

MySQL: 5.1.15 ~ 5.7.x

Sharding-Proxy: 3.x ~ 5.x

## How to Run

Refer to the [Quick Start](./src/resources/document/manual/quick-start.cn.md)

## For more documents

[Overview](./src/resources/document/features/_index.cn.md)

[Principle](./src/resources/document/features/principle.cn.md)
