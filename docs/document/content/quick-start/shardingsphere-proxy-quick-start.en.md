+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## Rule Configuration

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/config-xxx.yaml`.

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml`.

> %SHARDINGSPHERE_PROXY_HOME% is the shardingsphere proxy extract path. for example: /opt/shardingsphere-proxy-bin/

Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for more details.

## Import Dependencies

If the backend database is PostgreSQL, there's no need for additional dependencies.

If the backend database is MySQL, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) or [mysql-connector-java-8.0.11.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar) and put it into `%SHARDINGSPHERE_PROXY_HOME%/ext-lib` directory.

## Start Server

* Use default configuration to start

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

Default port is `3307`, default profile directory is `%SHARDINGSPHERE_PROXY_HOME%/conf/` .

* Customize port and profile directory

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh ${port} ${proxy_conf_directory}
```

## Use ShardingSphere-Proxy

Use MySQL or PostgreSQL client to connect ShardingSphere-Proxy. For example with MySQL:

```bash
mysql -u${proxy_username} -p${proxy_password} -h${proxy_host} -P${proxy_port}
```
