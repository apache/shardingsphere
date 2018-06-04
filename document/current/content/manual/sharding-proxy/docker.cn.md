+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker镜像"
weight = 3
+++

## 拉取官方Docker镜像

``` 
docker pull shardingsphere/sharding-proxy
```

## 手动构建Docker镜像（可选）

``` 
git clone https://github.com/sharding-sphere/sharding-sphere
mvn clean install
cd sharing-sphere/sharding-proxy
mvn clean package docker:build
```

## 配置Sharing-Proxy

创建/${your_work_dir}/conf/config.yaml文件，进行分片规则配置。配置方式请参考[配置手册](/cn/manual/sharding-proxy/configuration/)。

## 运行Docker

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 shardingsphere/sharding-proxy:latest
```

可以自定义端口`3308`和`13308`。`3308`表示docker容器端口, `13308`表示宿主机端口。

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 shardingsphere/sharding-proxy:latest
```

可以自定义JVM相关参数到环境变量`JVM_OPTS`中。

## 访问Sharing-Proxy

与连接MySQL的方式相同。

```
mysql -u${your_user_name} -p${your_password} -h${your_host} -P13308
```

## FAQ

问题：I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？

回答：在构建镜像前，请确保docker daemon进程已经运行。
