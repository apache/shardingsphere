+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker镜像"
weight = 3
+++

## 介绍
关于sharidng-proxy docker镜像，可以在Docker环境下使用sharding-proxy，开始前请先安装Docker。

## 构建Docker镜像
``` 
git clone https://github.com/shardingjdbc/sharding-jdbc
mvn clean install
cd sharing-jdbc/sharding-proxy
mvn clean package docker:build
```        

### 添加配置文件
首先创建/your_work_dir/conf/sharding-config.yaml文件，进行分片规则配置. 配置方式请参考[配置手册](/manual/sharding-proxy/configuration/)。

### 运行Docker
```
docker run -d -v /your_work_dir/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```
可以自定义端口`3308`和`13308`。`3308`表示docker容器端口, `13308`表示宿主机端口.

### 运行Docker
```
docker run -d -v /your_work_dir/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```
可以自定义JVM相关参数到环境变量`JVM_OPTS`中.

### 访问sharing-proxy
```
mysql -uryour_user_name -pyour_password -hyour_host -P13308
```

### FAQ
1. I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused?
答: 在构建镜像前，请确保docker deamon进程已经运行.
