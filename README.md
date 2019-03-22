# ShardingSphere-example

ShardingSphere example.

Example for 1.x please see tags in `https://github.com/apache/incubator-shardingsphere/tree/${tag}/sharding-jdbc-example`

Example for 2.x or 3.x please see tags in `https://github.com/apache/incubator-shardingsphere-example/tree/${tag}`

Please do not use `dev` branch to run your example, example of `dev` branch is not released yet. 

The manual schema initial script is in `https://github.com/apache/incubator-shardingsphere-example/blob/dev/src/resources/manual_schema.sql`, 
please execute it before you first run the example.

Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.

## Using docker-compose to config startup environment
before we use docker compose, please install docker first : https://docs.docker.com/compose/install/

#### sharding-jdbc
1. access the docker folder (cd docker/sharding-jdbc/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access mysql / etcd / zookeeper as you want
4. if there is conflict on port, just modify the mapper port in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

#### sharding-proxy
1. access the docker folder (cd docker/sharding-proxy/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access proxy by `mysql -h127.0.0.1 -P13308 -proot -uroot`
4. if there is conflict on port, just modify the mapper port in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

to clean the docker container , you could use docker rm `docker ps -a -q` (be careful)

## sharding-sphere-example module design
```
sharding-sphere-example
  ├── example-common
  │   ├── config-utility
  │   ├── repository-api
  │   ├── repository-jdbc
  │   ├── repository-jpa
  │   └── repository-mybatis
  ├── sharding-jdbc-example
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


