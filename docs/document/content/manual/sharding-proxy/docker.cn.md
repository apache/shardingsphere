+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker镜像"
weight = 3
+++

## 拉取官方Docker镜像

``` 
docker pull apache/sharding-proxy
```

## 手动构建Docker镜像（可选）

``` 
git clone https://github.com/apache/incubator-shardingsphere
mvn clean install
cd sharding-sphere/sharding-distribution/sharding-proxy-distribution
mvn clean package docker:build
```

## 配置Sharding-Proxy

创建/${your_work_dir}/conf/config.yaml文件，进行分片规则配置。配置方式请参考[配置手册](/cn/manual/sharding-proxy/configuration/)。

## 运行Docker

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```

可以自定义端口`3308`和`13308`。`3308`表示docker容器端口, `13308`表示宿主机端口。

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```

可以自定义JVM相关参数到环境变量`JVM_OPTS`中。

## 访问Sharding-Proxy

与连接PostgreSQL的方式相同。

```
psql -U ${your_user_name} -h ${your_host} -p 13308
```

## FAQ

问题1：I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？

回答：在构建镜像前，请确保docker daemon进程已经运行。

问题2：启动时报无法连接到数据库错误？

回答：请确保/${your_work_dir}/conf/sharding-config.yaml配置文件中指定的PostgreSQL数据库的IP可以被Docker容器内部访问到。
