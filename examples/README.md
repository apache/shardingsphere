# ShardingSphere-example

Example for 1.x please see tags in `https://github.com/apache/shardingsphere/tree/${tag}/shardingsphere-jdbc-example`

Example for 2.x or 3.x or 4.x please see tags in `https://github.com/apache/shardingsphere-example/tree/${tag}`

**Notices**

- *The `shardingsphere-sample` module is a brand new sample experience module. *

- *Please execute [initial script](https://github.com/apache/shardingsphere/blob/master/examples/src/resources/manual_schema.sql) before you first run the example if using manual mode.*

- *Please make sure primary replica data replication sync on MySQL is running correctly. Otherwise, primary-replica example will query empty data from the replica.*

## Using `master` branch

Please make sure some dependencies from [Apache ShardingSphere](https://github.com/apache/shardingsphere) has been installed since examples depend on that.
if you are a newbie for Apache ShardingSphere, you could prepare the dependencies as following: 

1. download and install [Apache ShardingSphere](https://github.com/apache/shardingsphere):

```bash
## download source code
git clone https://github.com/apache/shardingsphere.git

## compile source code
cd shardingsphere
mvn clean install -Prelease
```

## Module design

### project structure

```
shardingsphere-example
  ├── example-core
  │   ├── config-utility
  │   ├── example-api
  │   ├── example-raw-jdbc
  │   ├── example-spring-jpa
  │   └── example-spring-mybatis
  ├── shardingsphere-jdbc-example-generator
  ├── shardingsphere-parser-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   ├── shardingsphere-proxy-distsql-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
```

## Available Examples

| Example                                                                      | Description                                                                    |
|------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| [ShardingSphere-JDBC Examples](shardingsphere-jdbc-example-generator/README.md)   | Generate the examples by configuration and show how to use ShardingSphere-JDBC |
| [DistSQL](shardingsphere-proxy-example/shardingsphere-proxy-distsql-example) | show how to use DistSQL in ShardingSphere-Proxy                                |
| APM(Pending)                                                                 | show how to use APM in ShardingSphere                                          |
| proxy(Pending)                                                               | show how to use ShardingSphere-Proxy                                           |
| [docker](./docker/docker-compose.md)                                         | show how to use docker to setup the environment for ShardingSphere             |
