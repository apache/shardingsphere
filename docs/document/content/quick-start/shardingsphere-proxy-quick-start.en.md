+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## 1. Rule Configuration

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/config-xxx.yaml`. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for more details.

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml`. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for more details.

## 2. Import Dependencies

If the backend database is PostgreSQL, there's no need for additional dependencies.

If the backend database is MySQL, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) and put it into `%SHARDINGSPHERE_PROXY_HOME%/lib` directory.

## 3. Start Server

* Use default configuration to start

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

Default port is `3307`, default profile directory is `%SHARDINGSPHERE_PROXY_HOME%/conf/` .

* Customize port and profile directory

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh ${port} ${proxy_conf_directory}
```

## 4. Use ShardingSphere-Proxy

Use MySQL or PostgreSQL client to connect ShardingSphere-Proxy. For example with MySQL:

```bash
mysql -u${proxy_username} -p${proxy_password} -h${proxy_host} -P${proxy_port}
```
