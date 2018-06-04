+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker Image"
weight = 3
+++

## Build docker image

``` 
git clone https://github.com/sharding-sphere/sharding-sphere
mvn clean install
cd sharing-sphere/sharding-proxy
mvn clean package docker:build
```

## Configure Sharing-Proxy

Create /${your_work_dir}/conf/config.yaml file to configure sharding rule. More details please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/).

## Run docker

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 shardingsphere/sharding-proxy:latest
```

The port `3308` and `13308` could be customized, `3308` represents port of docker container, `13308` represents port of host machine.

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 shardingsphere/sharding-proxy:latest
```

Set JVM relative parameters into `JVM_OPTS`.

## Access Sharing-Proxy

Same as access MySQL.

```
mysql -u${your_user_name} -p${your_password} -h${your_host} -P13308
```

## FAQ

Q: I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused?

A: Please make sure docker is running before you build or run.
