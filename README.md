# ShardingScaling - ShardingSphere Scaling Out Component

## Overview
The following figure may clearly express this component's role:

![scale out](https://user-images.githubusercontent.com/14773179/67069547-08760080-f1b0-11e9-8020-7092f37a4ff2.png)

Supplementary instruction about the figure:

1. Only migrate whole database, can't support to migrate specified tables only. And the target schema name must be same with the source schema;
2. The process of migration splits into two step, history data migration and realtime data migration.

   (1) During history data migration, we use 'select *' syntax to acquire the data, and use 'insert' syntax to migrate the data to the target schema;
   
   (2) During realtime data migration, we use binlog to migrate the data, and we mark the binlog position before migration.
3. If the table in the source schema has primary key, we can migrate it concurrently using 'where condition'.
                                                                            
## Requirement

MySQL: 5.1.15 ~ 5.7.x

Sharding-Proxy: 3.x ~ 4.x 

## How to Build

Install `maven` and run command:

```shell
mvn clean package
```

## How to Run

1. Copy sharding-scaling-bootstrap\target\sharding-scaling-bootstrap-1.0.0-SNAPSHOT-bin.zip to work directory and unzip.

2. Download mysql jdbc jar to lib directory.

3. Config the `config.json` file in conf directory.

4. Start the program.
```
bin/start.sh
```