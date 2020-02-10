+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker Clone"
weight = 3
+++

## Pull Official Docker Clone

```
docker pull apache/sharding-proxy
```

## Build Docker Clone Manually (Optional)

```
git clone https://github.com/apache/incubator-shardingsphere
mvn clean install
cd sharding-distribution/sharding-proxy-distribution
mvn clean package docker:build
```

## Configure Sharding-Proxy

Create `/${your_work_dir}/conf/config.yaml` document to configure sharding rules. Please refer to [Configuration Manual](/en/manual/sharding-proxy/configuration/) for the configuration method.

## Run Docker

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```

You can define port `3308` and `13308` by yourself. `3308` refers to docker port; `13308` refers to the host port.

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```

You can define JVM related parameters to environment variable `JVM_OPTS`.

## Access Sharding-Proxy

It is in the same way as connecting to PostgreSQL.

```
psql -U ${your_user_name} -h ${your_host} -p 13308
```

## FAQ

Question 1: there is I/O exception (`java.io.IOException`) when process request to `{}->unix://localhost:80: Connection` is refused.

Answer: before building clone, please make sure docker daemon thread is running.

Question 2: there is error report of being unable to connect to the database.

Answer: please make sure designated PostgreSQL  IP in `/${your_work_dir}/conf/sharding-config.yaml` configuration is accessible to Docker container.
