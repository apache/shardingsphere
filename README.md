# ShardingSphere-example

ShardingSphere example.

Example for 1.x please see tags in `https://github.com/apache/incubator-shardingsphere/tree/${tag}/sharding-jdbc-example`

Example for 2.x or 3.x please see tags in `https://github.com/apache/incubator-shardingsphere-example/tree/${tag}`

Please do not use `dev` branch to run your example, example of `dev` branch is not released yet. 

The manual schema initial script is in `https://github.com/apache/incubator-shardingsphere-example/blob/dev/src/resources/manual_schema.sql`, 
please execute it before you first run the example.

Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.

## sharding-sphere-example module design

### project module
```
sharding-sphere-example
  ├── example-common
  │   ├── config-utility
  │   ├── repository-api
  │   ├── repository-jdbc
  │   ├── repository-jpa
  │   └── repository-mybatis
  ├── sharding-jdbc-example
  │   ├── hint-example
  │   │   └── hint-raw-jdbc-example
  │   ├── orchestration-example
  │   │   ├── orchestration-raw-jdbc-example
  │   │   ├── orchestration-spring-boot-example
  │   │   └── orchestration-spring-namespace-example
  │   ├── sharding-example
  │   │   ├── sharding-raw-jdbc-example
  │   │   ├── sharding-spring-boot-jpa-example
  │   │   ├── sharding-spring-boot-mybatis-example
  │   │   ├── sharding-spring-namespace-jpa-example
  │   │   └── sharding-spring-namespace-mybatis-example
  │   └── transaction-example
  │       ├── transaction-2pc-xa-example
  │       └── transaction-base-saga-example
  ├── sharding-proxy-example
  │   └── sharding-proxy-boot-mybatis-example
  └── src/resources
        └── manual_schema.sql
```

### Best practice for sharding data
* sharding databases
* sharding tables
* sharding databases and tables
* master-slave
* sharding & master-slave

you can get more detail from **[sharding-example](./sharding-jdbc-example/sharding-example)**

### Best practice for sharding + orchestration
* local zookeeper/etcd & sharding

    local sharding configuration can override the configuration of zookeeper/etcd.

* cloud zookeeper/etcd & sharding

    shardingsphere will load the sharding configuration form zookeeper/etcd directly.

you can get more detail from **[orchestration-example](./sharding-jdbc-example/orchestration-example)**

### Best Practice for sharding + distribution-transaction
* 2pc-xa transaction
* base-saga transaction

you can get more detail from **[transaction-example](./sharding-jdbc-example/transaction-example)**

### how to use hint routing
you can get more detail from **[hint-example](./sharding-jdbc-example/hint-example)**

### how to config none-sharding tables
we will add none-sharding example recently.

### how to config broadcast-table
we will add broadcast-table example recently.

### how to use APM with shardingsphere
we will add APM example recently.

### how to encrypt & decrypt data in shardingsphere
we prefer to add encrypt & decrypt example recently.

### how to use sharding-proxy with jdbc.
we prefer to add a docker base example recently.

### [how to use docker to config sharding-jdbc & sharding-proxy](./docker/docker-compose.md) (Optional)
