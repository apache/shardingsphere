# ShardingScaling - ShardingSphere Scaling Out Component

## How to Build

Install `maven` and run command:

```shell
mvn clean package
```

## How to Run

Install `java` and run command:

```shell
bin/start.sh \
  scaling \
  --input-sharding-config conf/config-sharding.yaml \
  --output-jdbc-url jdbc:mysql://127.0.0.1/test2?useSSL=false \
  --output-jdbc-username root \
  --output-jdbc-password 123456
```