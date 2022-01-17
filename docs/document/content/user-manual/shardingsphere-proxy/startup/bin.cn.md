+++
title = "使用二进制发布包"
weight = 1
+++

## 启动步骤

1. 下载 ShardingSphere-Proxy 的最新发行版。
1. 解压缩后修改 `conf/server.yaml` 和以 `config-` 前缀开头的文件，如：`conf/config-xxx.yaml` 文件，进行分片规则、读写分离规则配置。配置方式请参考[配置手册](/cn/user-manual/shardingsphere-proxy/yaml-config/)。
1. Linux 操作系统请运行 `bin/start.sh`，Windows 操作系统请运行 `bin/start.bat` 启动 ShardingSphere-Proxy。如需配置启动端口、配置文件位置，可参考[快速入门](/cn/quick-start/shardingsphere-proxy-quick-start/)。

## 选择数据库协议

### 使用 PostgreSQL

1. 使用任何 PostgreSQL 的客户端连接。如: `psql -U root -h 127.0.0.1 -p 3307`

### 使用 MySQL

1. 将 MySQL 的 JDBC 驱动程序复制至目录 `ext-lib/`。
1. 使用任何 MySQL 的客户端连接。如: `mysql -u root -h 127.0.0.1 -P 3307`

### 使用 openGauss

1. 将以 `org.opengauss` 包名为前缀的 openGauss 的 JDBC 驱动程序复制至目录 `ext-lib/`。
1. 使用任何 openGauss 的客户端连接。如: `gsql -U root -h 127.0.0.1 -p 3307`

## 选择元数据持久化仓库

### 使用 ZooKeeper

默认集成 ZooKeeper Curator 客户端。

### 使用 Etcd

1. 将 Etcd 的客户端驱动程序复制至目录 `ext-lib/`。

## 使用分布式事务

与 ShardingSphere-JDBC 使用方式相同。
具体可参考[分布式事务](/cn/user-manual/shardingsphere-jdbc/special-api/transaction/)。

## 使用自定义算法

当用户需要使用自定义的算法类时，可通过以下方式配置使用自定义算法，以分片为例：

1. 实现 `ShardingAlgorithm` 接口定义的算法实现类。
1. 将上述 Java 文件打包成 jar 包。
1. 将上述 jar 包拷贝至 ShardingSphere-Proxy 解压后的 `ext-lib/` 目录。
1. 将上述自定义算法实现类的 Java 文件引用配置在 YAML 文件中，具体可参考[配置规则](/cn/user-manual/shardingsphere-proxy/yaml-config/)。

## 注意事项

1. ShardingSphere-Proxy 默认使用 `3307` 端口，可以通过启动脚本追加参数作为启动端口号。如: `bin/start.sh 3308`
1. ShardingSphere-Proxy 使用 `conf/server.yaml` 配置注册中心、认证信息以及公用属性。
1. ShardingSphere-Proxy 支持多逻辑数据源，每个以 `config-` 前缀命名的 YAML 配置文件，即为一个逻辑数据源。
