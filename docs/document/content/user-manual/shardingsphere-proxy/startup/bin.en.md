+++
title = "Use Binary Tar"
weight = 1
+++

## Background

This section describes how to start ShardingSphere-Proxy by binary release packages

## Premise

Start the Proxy with a binary package requires an environment with Java JRE 8 or later.

## Steps

1. Obtain the binary release package of ShardingSphere-Proxy

Obtain the binary release package of ShardingSphere-Proxy on the [download page](https://shardingsphere.apache.org/document/current/en/downloads/).

2. Configure `conf/server.yaml`

ShardingSphere-Proxy's operational mode is configured on `server.yaml`, and its configuration mode is the same with that of ShardingSphere-JDBC. Refer to [mode of configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/mode/).

Please refer to the following links for other configuration items:
* [Permission configuration](/en/user-manual/shardingsphere-proxy/yaml-config/authentication/)
* [Property configuration](/en/user-manual/shardingsphere-proxy/yaml-config/props/)

3. Configure `conf/config-*.yaml`

Modify files named with the prefix `config-` in the `conf` directory, such as `conf/config-sharding.yaml` file and configure sharding rules and read/write splitting rules. See [Confuguration Mannual](/en/user-manual/shardingsphere-proxy/yaml-config/) for configuration methods. The `*` part of the `config-*.yaml` file can be named whatever you want.

ShardingSphere-Proxy supports multiple logical data sources. Each YAML configuration file named with the prefix `config-` is a logical data source.

4. Introduce database driver (Optional)

If the backend is connected to a PostgreSQL or openGauss database, no additional dependencies need to be introduced.

If the backend is connected to a MySQL database, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) or [mysql-connector-java-8.0.11.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar), and put it into the `ext-lib` directory.

5. Introduce dependencies required by the cluster mode (Optional)

ShardingSphere-Proxy integrates the ZooKeeper Curator client by default. ZooKeeper is used in cluster mode without introducing other dependencies.

If the cluster mode uses Etcd, the client drivers of Etcd [jetcd-core 0.5.0](https://repo1.maven.org/maven2/io/etcd/jetcd-core/0.5.0/jetcd-core-0.5.0.jar) need to be copied into the `ext-lib` directory.

6. Introduce dependencies required by distributed transactions (Optional)

It is the same with ShardingSphere-JDBC.
Please refer to [Distributed Transaction](/en/user-manual/shardingsphere-jdbc/special-api/transaction/) for more details.

7. Introduce custom algorithm (Optional)

If you need to use a user-defined algorithm class, you can configure custom algorithm in the following ways:

    1. Implement the algorithm implementation class defined by `ShardingAlgorithm`.
    2. Create a `META-INF/services` directory under the project `resources` directory.
    3. Create file `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` under the directory `META-INF/services`.
    4. Writes the fully qualified class name of the implementation class to a file `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`
    5. Package the above Java files into jar packages.
    6. Copy the above jar package to the `ext-lib` directory.
    7. Configure the Java file reference of the above custom algorithm implementation class in a YAML file, see [Configuration rule](/en/user-manual/shardingsphere-proxy/yaml-config/) for more details.

8. Start ShardingSphere-Proxy

In Linux or macOS, run `bin/start.sh`. In Windows, run `bin/start.bat` to start ShardingSphere-Proxy. The default listening port is `3307` and the default configuration directory is the `conf` directory in Proxy. The startup script can specify the listening port and the configuration file directory by running the following command:

```bash
bin/start.sh [port] [/path/to/conf]
```

9. Connect ShardingSphere-Proxy with client

Run the MySQL/PostgreSQL/openGauss client command to directly operate ShardingSphere-Proxy.

Connect ShardingSphere-Proxy with MySQL client:
```bash
mysql -h${proxy_host} -P${proxy_port} -u${proxy_username} -p${proxy_password}
```

Connect ShardingSphere-Proxy with PostgreSQL:
```bash 
psql -h ${proxy_host} -p ${proxy_port} -U ${proxy_username}
```

Connect ShardingSphere-Proxy with openGauss client:
```bash 
gsql -r -h ${proxy_host} -p ${proxy_port} -U ${proxy_username} -W ${proxy_password}
```

## Sample

Please refer to samples on ShardingSphere repository for complete configuration:
<https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-proxy-example>
