+++
pre = "<b>4.2.3. </b>"
title = "Docker镜像"
weight = 3
+++

## 拉取官方Docker镜像

``` 
docker pull apache/sharding-proxy
```

## 手动构建Docker镜像（可选）

``` 
git clone https://github.com/apache/shardingsphere
mvn clean install
cd sharding-sphere/sharding-distribution/sharding-proxy-distribution
mvn clean package -Prelease,docker
```

## 配置Sharding-Proxy

在/${your_work_dir}/conf/创建server.yaml和config-xxx.yaml文件，进行服务器和分片规则配置。
配置规则，请参考[配置手册](/cn/manual/sharding-proxy/configuration/)。
配置模板，请参考[配置模板](https://github.com/apache/shardingsphere/tree/master/sharding-proxy/sharding-proxy-bootstrap/src/main/resources/conf)

## 运行Docker

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf -e PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```
**说明**
* 可以自定义端口`3308`和`13308`。`3308`表示docker容器端口, `13308`表示宿主机端口。
* 必须挂载配置路径到/opt/sharding-proxy/conf。

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf -e JVM_OPTS="-Djava.awt.headless=true" -e PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```
**说明**
* 可以自定义JVM相关参数到环境变量`JVM_OPTS`中。

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf -v /${your_work_dir}/ext-lib:/opt/sharding-proxy/ext-lib -p13308:3308 apache/sharding-proxy:latest
```
**说明**
* 如需使用外部jar包，可将其所在目录挂载到/opt/sharding-proxy/ext-lib。

## 访问Sharding-Proxy

与连接PostgreSQL的方式相同。

```
psql -U ${your_user_name} -h ${your_host} -p 13308
```

## FAQ

问题1：I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？

回答：在构建镜像前，请确保docker daemon进程已经运行。

问题2：启动时报无法连接到数据库错误？

回答：请确保/${your_work_dir}/conf/config-xxx.yaml配置文件中指定的PostgreSQL数据库的IP可以被Docker容器内部访问到。

问题3：如何使用后端数据库为MySQL的ShardingProxy？

回答：将`mysql-connector.jar`所在目录挂载到/opt/sharding-proxy/ext-lib。

问题4：如何使用自定义分片算法？

回答：实现对应的分片算法接口，将编译出的分片算法jar所在目录挂载到/opt/sharding-proxy/ext-lib。


