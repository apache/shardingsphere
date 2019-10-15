# ShardingScaling - ShardingSphere Scaling Out Component

## Overview
The following figure may clearly express this component's role:

![scale out](https://user-images.githubusercontent.com/14773179/66809021-16811280-ef5f-11e9-90c7-cf536e5e2ebc.png)

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

1. Copy target\sharding-scaling-1.0.0-SNAPSHOT-bin.zip to work directory and unzip.

1. Download mysql jdbc jar to lib directory.

1. Run below command.

```shell
bin/start.sh \
  scaling \
  --input-sharding-config conf/config-sharding.yaml \ # old ss proxy sharding rule config file
  --output-jdbc-url jdbc:mysql://127.0.0.1/test2?useSSL=false \ # new sharding rule ss proxy jdbc url
  --output-jdbc-username root \ # new sharding rule ss proxy jdbc username
  --output-jdbc-password 123456 # new sharding rule ss proxy jdbc password
```