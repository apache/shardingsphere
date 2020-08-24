+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## 1. 规则配置

编辑`%SHARDINGSPHERE_PROXY_HOME%/conf/config-xxx.yaml`。详情请参见[配置手册](/cn/user-manual/shardingsphere-proxy/configuration/)。

编辑`%SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml`。详情请参见[配置手册](/cn/user-manual/shardingsphere-proxy/configuration/)。

## 2. 引入依赖

如果后端连接 PostgreSQL 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，需要下载 [MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz)，
解压缩后，将 `mysql-connector-java-5.1.47.jar` 拷贝到 `%SHARDINGSPHERE_PROXY_HOME%/lib` 目录。

## 3. 启动服务

* 使用默认配置项

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

默认启动端口为 `3307`，默认配置文件目录为：`%SHARDINGSPHERE_PROXY_HOME%/conf/`。

* 自定义端口和配置文件目录

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh ${proxy_port} ${proxy_conf_directory}
```

## 4. 使用ShardingSphere-Proxy

执行 MySQL 或 PostgreSQL的客户端命令直接操作 ShardingSphere-Proxy 即可。以 MySQL 举例：

```bash
mysql -u${proxy_username} -p${proxy_password} -h${proxy_host} -P${proxy_port}
```
