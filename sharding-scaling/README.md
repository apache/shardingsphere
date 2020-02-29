# ShardingScaling - ShardingSphere Scaling Out Component

## Overview
The following figure may clearly express this component's role:

![scale out](https://user-images.githubusercontent.com/14773179/75600294-8516d500-5ae8-11ea-9635-5656b72242e3.png)

Supplementary instruction about the figure:

1. Only migrate whole database, can't support to migrate specified tables only.
2. The process of migration splits into two step, history data migration and realtime data migration.

   (1) During history data migration, we use 'select *' syntax to acquire the data, and use 'insert' syntax to migrate the data to the target;
   
   (2) During realtime data migration, we use binlog to migrate the data, and we mark the binlog position before migration.
3. If the table in the source schema has primary key, we can migrate it concurrently using 'where condition'.
                                                                            
## Requirement

MySQL: 5.1.15 ~ 5.7.x

Sharding-Proxy: 3.x ~ 4.x 

## How to Run

Refer to the [Quick Start](./src/resources/Quick%20Start_zh.md)

## For more documents

[Admin Guide](./src/resources/Admin%20Guide_zh.md)

[Architecture of ShardingScaling](./src/resources/Architecture_zh.md)
