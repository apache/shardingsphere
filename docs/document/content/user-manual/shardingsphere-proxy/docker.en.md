+++
pre = "<b>4.2.3. </b>"
title = "Docker Clone"
weight = 3
+++

## Pull Official Docker Clone

```bash
docker pull apache/shardingsphere-proxy
```

## Build Docker Clone Manually (Optional)

```bash
git clone https://github.com/apache/shardingsphere
mvn clean install
cd shardingsphere-distribution/shardingsphere-proxy-distribution
mvn clean package -Prelease,docker
```

## Configure ShardingSphere-Proxy

Create `server.yaml` and `config-xxx.yaml` to configure sharding rules and server rule in `/${your_work_dir}/conf/`. 
Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/).
Please refer to [Example](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf).

## Run Docker

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

**Notice**

* You can define port `3308` and `13308` by yourself. `3308` refers to docker port; `13308` refers to the host port.
* You have to volume conf dir to /opt/shardingsphere-proxy/conf.

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -e JVM_OPTS="-Djava.awt.headless=true" -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

**Notice**

* You can define JVM related parameters to environment variable `JVM_OPTS`.

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -v /${your_work_dir}/ext-lib:/opt/shardingsphere-proxy/ext-lib -p13308:3308 apache/shardingsphere-proxy:latest
```

**Notice**

* If you want to import external jar packages, whose directory is supposed to volume to /opt/shardingsphere-proxy/ext-lib.

## Access ShardingSphere-Proxy

It is in the same way as connecting to PostgreSQL.

```bash
psql -U ${your_user_name} -h ${your_host} -p 13308
```

## FAQ

Question 1: there is I/O exception (`java.io.IOException`) when process request to `{}->unix://localhost:80: Connection` is refused.

Answer: before building clone, please make sure docker daemon thread is running.

Question 2: there is error report of being unable to connect to the database.

Answer: please make sure designated PostgreSQL  IP in `/${your_work_dir}/conf/config-xxx.yaml` configuration is accessible to Docker container.

Question 3：How to start ShardingProxy whose backend databases are MySQL.

Answer：Volume the directory where `mysql-connector.jar` stores to /opt/shardingsphere-proxy/ext-lib.

Question 4：How to import user-defined sharding strategy？

Answer: Volume the directory where `shardingsphere-strategy.jar` stores to /opt/shardingsphere-proxy/ext-lib.
