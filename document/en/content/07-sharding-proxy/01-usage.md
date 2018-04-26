+++
toc = true
title = "The usage manual"
weight = 1
+++

## Introduction

Sharding-Proxy is the MySQL protocol agent based on Sharding-JDBC, and more friendly for management and debugging. You can use any clients compatible with MySQL protocol, e.g. MySQL Command Client, MySQL Workbench to connect to Sharding-Proxy to query and modify data.

In brief, Sharding-Proxy is a standard MySQL (Other databases might be included in future) that also supports Sharding, Read-write splitting, and orchestration and is also like the management interface for all back-end MySQL Sharding. In fact, it is the control node, the management node, and the routing node.

Sharding-Proxy is suitable for uses to manage and debug, and can be connected easily by various MySQL clients, and Sharding-JDBC is more suitable for online programs for it reduces the cost of secondary transmit.

## Usage

1. Download the latest version of Sharding-Proxy from https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz
2. Modify the conf/sharding-config.yaml file after decompression, and configure the sharding rule. Refer to [The YAML configuration in Sharding-JDBC](/02-guide/configuration/)。
3. Run bin/start.sh in Linux, or bin/start.bat in Windows to start Sharding-Proxy.
4. Connect to it by means of any client tools, e.g. mysql -u root -h 127.0.0.1 -P3307

## Notices

1. The default port of Sharding-Proxy is 3307, and can be changed by passing the port parameter to the startup script, e.g. bin/start.sh 3308.
2. There is only one logical data source in Sharding-Proxy, named `sharding_db`.
3. Currently, Sharding-Proxy does not support authorization, therefore you can login with any username and password at startup.
