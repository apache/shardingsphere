+++
pre = "<b>4.2.3. </b>"
title = "Docker 镜像"
weight = 3
+++

## 拉取官方 Docker 镜像

```bash
docker pull apache/shardingsphere-proxy
```

## 手动构建 Docker 镜像（可选）

```bash
git clone https://github.com/apache/shardingsphere
mvn clean install
cd shardingsphere-distribution/shardingsphere-proxy-distribution
mvn clean package -Prelease,docker
```

## 配置 ShardingSphere-Proxy

在 `/${your_work_dir}/conf/` 创建 `server.yaml` 和 `config-xxx.yaml` 文件，进行服务器和分片规则配置。
配置规则，请参考[配置手册](/cn/user-manual/shardingsphere-proxy/configuration/)。
配置模板，请参考[配置模板](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy/shardingsphere-proxy-bootstrap/src/main/resources/conf)

## 运行 Docker

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

**说明**

* 可以自定义端口 `3308` 和 `13308`。`3308` 表示 docker 容器端口, `13308` 表示宿主机端口。
* 必须挂载配置路径到 /opt/shardingsphere-proxy/conf。

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -e JVM_OPTS="-Djava.awt.headless=true" -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

**说明**

* 可以自定义JVM相关参数到环境变量 `JVM_OPTS` 中。

```bash
docker run -d -v /${your_work_dir}/conf:/opt/shardingsphere-proxy/conf -v /${your_work_dir}/ext-lib:/opt/shardingsphere-proxy/ext-lib -p13308:3308 apache/shardingsphere-proxy:latest
```

**说明**

* 如需使用外部 jar 包，可将其所在目录挂载到 /opt/shardingsphere-proxy/ext-lib。

## 访问 ShardingSphere-Proxy

与连接 PostgreSQL 的方式相同。

```bash
psql -U ${your_user_name} -h ${your_host} -p 13308
```

## FAQ

问题1：I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？

回答：在构建镜像前，请确保 docker daemon 进程已经运行。

问题2：启动时报无法连接到数据库错误？

回答：请确保 /${your_work_dir}/conf/config-xxx.yaml 配置文件中指定的 PostgreSQL 数据库的 IP 可以被 Docker 容器内部访问到。

问题3：如何使用后端数据库为 MySQL 的 ShardingSphere-Proxy？

回答：将 `mysql-connector.jar` 所在目录挂载到 /opt/shardingsphere-proxy/ext-lib。

问题4：如何使用自定义分片算法？

回答：实现对应的分片算法接口，将编译出的分片算法 jar 所在目录挂载到 /opt/shardingsphere-proxy/ext-lib。
