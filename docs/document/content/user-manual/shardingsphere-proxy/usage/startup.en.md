+++
title = "Startup"
weight = 1
+++

## Startup Steps

1. Download the latest version of ShardingSphere-Proxy.
1. After the decompression, revise `conf/server.yaml` and documents begin with `config-` prefix, `conf/config-xxx.yaml` for example, to configure sharding rules and readwrite-splitting rules. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for the configuration method.
1. Please run `bin/start.sh` for Linux operating system; run `bin/start.bat` for Windows operating system to start ShardingSphere-Proxy. To configure start port and document location, please refer to [Quick Start](/en/quick-start/shardingsphere-proxy-quick-start/).
1. Using docker please refer to [Docker Image](/en/user-manual/shardingsphere-proxy/docker/) for more details.

## Using database protocol

### Using PostgreSQL

1. Use any PostgreSQL terminal to connect, such as `psql -U root -h 127.0.0.1 -p 3307`.

### Using MySQL

1. Copy MySQL's JDBC driver to folder `ext-lib/`.
1. Use any MySQL terminal to connect, such as `mysql -u root -h 127.0.0.1 -P 3307`.

### Using openGauss

1. Copy openGauss's JDBC driver to folder `ext-lib/`.
1. Use any openGauss terminal to connect, such as `gsql -U root -h 127.0.0.1 -p 3307`.

## Using metadata persist repository

### Using ZooKeeper

Default integration.

### Using Etcd

1. Copy Etcd's client driver to folder `ext-lib/`.

## Using user-defined algorithm

When developer need to use user-defined algorithm, should use the way below to configure algorithm, use sharding algorithm as example. 

1. Implement `ShardingAlgorithm` interface.
1. Package Java file to jar.
1. Copy jar to ShardingSphere-Proxy's `ext-lib/` folder.
1. Configure user-defined Java class into YAML file. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-proxy/configuration/) for more details.

## Notices

1. ShardingSphere-Proxy uses `3307` port in default. Users can start the script parameter as the start port number, like `bin/start.sh 3308`.
1. ShardingSphere-Proxy uses `conf/server.yaml` to configure the registry center, authentication information and public properties.
1. ShardingSphere-Proxy supports multi-logic data sources, with each yaml configuration document named by `config-` prefix as a logic data source.
