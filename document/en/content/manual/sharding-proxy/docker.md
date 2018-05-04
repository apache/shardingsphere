+++
pre = "<b>4.2.3. </b>"
toc = true
title = "Docker Guide"
weight = 3
+++

## Introduction
This is sharidng-proxy docker image which can run sharding-proxy in docker env, you need to install docker first.

### Build docker image
``` 
git clone https://github.com/shardingjdbc/sharding-jdbc
mvn clean install
cd sharing-jdbc/sharding-proxy
mvn clean package docker:build
```        

### Add config file
You should make /your_work_dir/conf/sharding-config.yaml file, and configure the sharding rule.Please reference [Configuration Manual](/07-sharding-proxy/configuration/).

### Run docker 
```
docker run -d -v /your_work_dir/conf:/opt/sharding-proxy/conf --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```
The port `3308` and `13308` could be customized, `3308` represents docker container's port, `13308` represents host machine's port.

### Run docker with customized JVM parameter
```
docker run -d -v /your_work_dir/conf:/opt/sharding-proxy/conf --env JVM_OPTS="-Djava.awt.headless=true" --env PORT=3308 -p13308:3308 shardingjdbc/sharding-proxy:2.1.0-SNAPSHOT
```
User can put jvm relative parameters into `JVM_OPTS`.

### Access sharing-proxy
```
mysql -uryour_user_name -pyour_password -hyour_host -P13308
```

### FAQ
1. I/O exception (java.io.IOException) caught when processing request to {}->unix://localhost:80: Connection refused?
A: Please make sure docker is running before you build or run.
