+++
pre = "<b>2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## Scenarios

![shardingsphere-proxy](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

ShardingSphere-Proxy is positioned as a transparent database proxy. It theoretically supports any client operation data using MySQL, PostgreSQL and openGauss protocols, and is friendly to heterogeneous languages and operation and maintenance scenarios.

## Limitations

Proxy provides limited support for system databases / tables (such as information_schema, pg_catalog). When connecting to Proxy through some graph database clients, the client or proxy may have an error prompt. You can use command-line clients (`mysql`, `psql`, `gsql`, etc.) to connect to the Proxy's authentication function.

## Requirements

Starting ShardingSphere-Proxy with Docker requires no additional dependency.
To start the Proxy using binary distribution, the environment must have Java JRE 8 or higher.

## Procedure

1. Get ShardingSphere-Proxy.

ShardingSphere-Proxy is available at:
- [Binary Distribution](/en/user-manual/shardingsphere-proxy/startup/bin/)
- [Docker](/en/user-manual/shardingsphere-proxy/startup/docker/)
- [Helm](/en/user-manual/shardingsphere-proxy/startup/helm/)

2. Rule configuration.

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/global.yaml`.

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/database-xxx.yaml`.

> %SHARDINGSPHERE_PROXY_HOME% is the proxy extract path. for example: `/opt/shardingsphere-proxy-bin/`

Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/yaml-config/) for more details.

3. Import dependencies.

If the backend database is PostgreSQL or openGauss, no additional dependencies are required.

If the backend database is MySQL, please download [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) or [mysql-connector-java-8.0.11.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar) and put it into the `%SHARDINGSPHERE_PROXY_HOME%/ext-lib` directory.

4. Start server.

* Use the default configuration to start

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

The default port is `3307`, while the default profile directory is `%SHARDINGSPHERE_PROXY_HOME%/conf/`.

* Customize port and profile directory

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh ${proxy_port} ${proxy_conf_directory}
```

5. Use ShardingSphere-Proxy.

Use MySQL or PostgreSQL or openGauss client to connect ShardingSphere-Proxy.

Use the MySQL client to connect to the ShardingSphere-Proxy:
```bash
mysql -h${proxy_host} -P${proxy_port} -u${proxy_username} -p${proxy_password}
```

Use the PostgreSQL client to connect to the ShardingSphere-Proxy:
```bash 
psql -h ${proxy_host} -p ${proxy_port} -U ${proxy_username}
```

Use the openGauss client to connect to the ShardingSphere-Proxy:
```bash 
gsql -r -h ${proxy_host} -p ${proxy_port} -U ${proxy_username} -W ${proxy_password}
```
