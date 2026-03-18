+++
title = "使用 Docker"
weight = 2
+++

## 背景信息

本节主要介绍如何通过 Docker 启动 ShardingSphere-Proxy。

## 注意事项

使用 Docker 启动 ShardingSphere-Proxy 无须额外依赖。

## 操作步骤

1. 获取 Docker 镜像

* 方式一（推荐）：从 DockerHub 获取
```bash
docker pull apache/shardingsphere-proxy
```

* 方式二：获取 master 分支最新镜像：<https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy>

* 方式三：自行构建镜像
```bash
git clone https://github.com/apache/shardingsphere
./mvnw clean install
cd shardingsphere-distribution/shardingsphere-proxy-distribution
./mvnw clean package -P-dev,release,all,docker
```

如果遇到以下问题，请确保 Docker daemon 进程已经运行。
```
I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？
```

2. 配置 `conf/global.yaml` 和 `conf/database-*.yaml`

可以从 Docker 容器中获取配置文件模板，拷贝到宿主机任意目录中：
```bash
docker run -d --name tmp --entrypoint=bash apache/shardingsphere-proxy
docker cp tmp:/opt/shardingsphere-proxy/conf /host/path/to/conf
docker rm tmp
```

由于容器内的网络环境可能与宿主机的网络环境有差异，如果启动时报无法连接到数据库错误等错误，请确保 `conf/database-*.yaml` 配置文件中指定的数据库的 IP 可以被 Docker 容器内部访问到。

具体配置请参考 [ShardingSphere-Proxy 启动手册 - 使用二进制发布包](/cn/user-manual/shardingsphere-proxy/startup/bin/)。

3. （可选）引入第三方依赖或自定义算法

如果存在以下任意需求：
* ShardingSphere-Proxy 后端使用 MySQL 数据库；
* 使用自定义算法；
* 使用 Etcd 作为集群模式的注册中心。

请在宿主机中任意位置创建 `ext-lib` 目录，并参考 [ShardingSphere-Proxy 启动手册 - 使用二进制发布包](/cn/user-manual/shardingsphere-proxy/startup/bin/)中的对应步骤。

4. 启动 ShardingSphere-Proxy 容器

将宿主机中的 `conf` 与 `ext-lib` 目录挂载到容器中，启动容器：

```bash
docker run -d \
    -v /host/path/to/conf:/opt/shardingsphere-proxy/conf \
    -v /host/path/to/ext-lib:/opt/shardingsphere-proxy/ext-lib \
    -e PORT=3308 -p13308:3308 apache/shardingsphere-proxy:latest
```

其中，`ext-lib` 非必需，用户可按需挂载。
ShardingSphere-Proxy 默认端口 `3307`，可以通过环境变量 `-e PORT` 指定。
自定义 JVM 相关参数可通过环境变量 `JVM_OPTS` 设置。

说明：

支持设置 CGROUP_MEM_OPTS 环境变量: 用于在容器环境中设置相关内存参数，脚本中的默认值为：

```sql
-XX:InitialRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0 -XX:MinRAMPercentage=80.0
```

5. 使用客户端连接 ShardingSphere-Proxy

请参考 [ShardingSphere-Proxy 启动手册 - 使用二进制发布包](/cn/user-manual/shardingsphere-proxy/startup/bin/)。
