+++
title = "Use Docker"
weight = 2
+++

## Background

This chapter is an introduction about how to start ShardingSphere-Proxy via Docker

## Notice

Using Docker to start ShardingSphere-Proxy does not require additional package supoort.

## Steps

1. Acquire Docker Image

* Method 1 (Recommended): Pull from DockerHub
```bash
docker pull apache/shardingsphere-proxy
```

* Method 2: Acquire latest master branch image master: <https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy>

* Method 3: Build your own image
```bash
git clone https://github.com/apache/shardingsphere
./mvnw clean install
cd shardingsphere-distribution/shardingsphere-proxy-distribution
./mvnw clean package -P-dev,release,all,docker
```

If the following problems emerge, please make sure Docker daemon Process is running.
```
I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？
```

2. Configure `conf/global.yaml` and `conf/database-*.yaml`

Configuration file template can be attained from the Docker container and can be copied to any directory on the host:
```bash
docker run -d --name tmp --entrypoint=bash apache/shardingsphere-proxy
docker cp tmp:/opt/shardingsphere-proxy/conf /host/path/to/conf
docker rm tmp
```

Since the network conditions inside the container may differ from those of the host, if errors such as "cannot connect to the database" occurs, please make sure that the IP of the database specified in the `conf/database-*.yaml` configuration file can be accessed from inside the Docker container.

For details, please refer to [ShardingSphere-Proxy quick start manual - binary distribution packages](/en/user-manual/shardingsphere-proxy/startup/bin/).

3. (Optional) Introduce third-party dependencies or customized algorithms

If you have any of the following requirements:
* ShardingSphere-Proxy Backend use MySQL Database;
* Implement customized algorithms;
* Use Etcd as Registry Center in cluster mode.

Please create `ext-lib` directory anywhere inside the host and refer to the steps in [ShardingSphere-Proxy quick start manual - binary distribution packages](/en/user-manual/shardingsphere-proxy/startup/bin/).

4. Start ShardingSphere-Proxy container

Mount the `conf` and `ext-lib` directories from the host to the container. Start the container:

```bash
docker run -d \
    -v /host/path/to/conf:/opt/shardingsphere-proxy/conf \
    -v /host/path/to/ext-lib:/opt/shardingsphere-proxy/ext-lib \
    -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

`ext-lib` is not necessary during the process. Users can mount it at will.
ShardingSphere-Proxy default portal `3307` can be designated according to environment variable `-e PORT`
Customized JVM related parameters can be set according to environment variable `JVM_OPTS`

Note: 

Support setting environment variable CGROUP_ MEM_ OPTS: used to set related memory parameters in the container environment. The default values in the script are:

```sql
-XX:InitialRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0 -XX:MinRAMPercentage=80.0
```

5. Use Client to connect to ShardingSphere-Proxy

Please refer to [ShardingSphere-Proxy quick start manual - binary distribution packages](/en/user-manual/shardingsphere-proxy/startup/bin/).
