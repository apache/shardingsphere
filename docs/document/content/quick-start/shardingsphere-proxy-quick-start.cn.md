+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## 应用场景

![shardingsphere-proxy](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

ShardingSphere-Proxy 的定位为透明化的数据库代理，理论上支持任何使用 MySQL、PostgreSQL、openGauss 协议的客户端操作数据，对异构语言、运维场景更友好。

## 使用限制

ShardingSphere-Proxy 对系统库/表（如 information_schema、pg_catalog）支持有限，通过部分图形化数据库客户端连接 Proxy 时，可能客户端或 Proxy 会有错误提示。可以使用命令行客户端（`mysql`、`psql`、`gsql` 等）连接 Proxy 验证功能。

## 前提条件

使用 Docker 启动 ShardingSphere-Proxy 无须额外依赖。
使用二进制分发包启动 Proxy，需要环境具备 Java JRE 8 或更高版本。

## 操作步骤

1. 获取 ShardingSphere-Proxy

目前 ShardingSphere-Proxy 可以通过以下方式：
- [二进制发布包](/cn/user-manual/shardingsphere-proxy/startup/bin/)
- [Docker](/cn/user-manual/shardingsphere-proxy/startup/docker/)
- [Helm](/cn/user-manual/shardingsphere-proxy/startup/helm/)

2. 规则配置

编辑 `%SHARDINGSPHERE_PROXY_HOME%/conf/global.yaml`。

编辑 `%SHARDINGSPHERE_PROXY_HOME%/conf/database-xxx.yaml`。

> %SHARDINGSPHERE_PROXY_HOME% 为 Proxy 解压后的路径，例：`/opt/shardingsphere-proxy-bin/`

详情请参见 [配置手册](/cn/user-manual/shardingsphere-proxy/yaml-config/)。

3. 引入依赖

如果后端连接 PostgreSQL 或 openGauss 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，请下载 [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) 或者 [mysql-connector-java-8.0.11.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar)，并将其放入 `%SHARDINGSPHERE_PROXY_HOME%/ext-lib` 目录。

4. 启动服务

* 使用默认配置项

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

默认启动端口为 `3307`，默认配置文件目录为：`%SHARDINGSPHERE_PROXY_HOME%/conf/`。

* 自定义端口和配置文件目录

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh ${proxy_port} ${proxy_conf_directory}
```

5. 使用 ShardingSphere-Proxy

执行 MySQL / PostgreSQL / openGauss 的客户端命令直接操作 ShardingSphere-Proxy 即可。

使用 MySQL 客户端连接 ShardingSphere-Proxy：
```bash
mysql -h${proxy_host} -P${proxy_port} -u${proxy_username} -p${proxy_password}
```

使用 PostgreSQL 客户端连接 ShardingSphere-Proxy：
```bash 
psql -h ${proxy_host} -p ${proxy_port} -U ${proxy_username}
```

使用 openGauss 客户端连接 ShardingSphere-Proxy：
```bash 
gsql -r -h ${proxy_host} -p ${proxy_port} -U ${proxy_username} -W ${proxy_password}
```
