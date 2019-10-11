# ShardingScaling - ShardingSphere Scaling Out Component

## Requirement

MySQL: 5.1.15 ~ 5.7.x

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