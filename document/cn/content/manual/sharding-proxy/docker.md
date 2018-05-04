+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker镜像"
weight = 3
+++

## 构建Docker镜像

``` 
git clone https://github.com/shardingjdbc/sharding-jdbc
mvn clean install
cd sharing-jdbc/sharding-proxy
mvn clean package docker:build
```

## 在Docker中配置Sharding-Proxy

创建/${your_work_dir}/conf/sharding-config.yaml文件，进行分片规则配置。配置方式请参考[配置手册](/manual/sharding-proxy/configuration/)。

## 在Docker中运行Sharding-Proxy

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```

可以自定义端口`3308`和`13308`。`3308`表示docker容器端口, `13308`表示宿主机端口。

```
docker run -d -v /${your_work_dir}/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```

可以自定义JVM相关参数到环境变量`JVM_OPTS`中。

## 在Docker中访问Sharing-Proxy

与连接MySQL的方式相同。

```
mysql -u${your_user_name} -p${your_password} -h${your_host} -P13308
```

## FAQ

1. I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused？

答: 在构建镜像前，请确保docker daemon进程已经运行。
