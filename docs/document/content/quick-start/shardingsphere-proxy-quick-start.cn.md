+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## 1. 规则配置

编辑`%SHARDING_PROXY_HOME%\conf\config-xxx.yaml`。详情请参见[配置手册](/cn/manual/shardingsphere-proxy/configuration/)。

编辑`%SHARDING_PROXY_HOME%\conf\server.yaml`。详情请参见[配置手册](/cn/manual/shardingsphere-proxy/configuration/)。

## 2. 引入依赖

如果后端连接PostgreSQL数据库，不需要引入额外依赖。

如果后端连接MySQL数据库，需要下载[MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz)，
解压缩后，将mysql-connector-java-5.1.47.jar拷贝到${shardingsphere-proxy}\lib目录。

## 3. 启动服务

* 使用默认配置项

```sh
${shardingsphere-proxy}\bin\start.sh
```

* 配置端口

```sh
${shardingsphere-proxy}\bin\start.sh ${port}
```
