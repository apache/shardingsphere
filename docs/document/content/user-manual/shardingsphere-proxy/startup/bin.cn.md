+++
title = "使用二进制发布包"
weight = 1
+++

## 背景信息

本节主要介绍如何通过二进制发布包启动 ShardingSphere-Proxy。

## 前提条件

使用二进制发布包启动 Proxy，需要环境具备 Java JRE 8 或更高版本。

## 操作步骤

1. 获取 ShardingSphere-Proxy 二进制发布包

在[下载页面](https://shardingsphere.apache.org/document/current/cn/downloads/)获取。

2. 配置 `conf/server.yaml`

ShardingSphere-Proxy 运行模式在 `server.yaml` 中配置，配置格式与 ShardingSphere-JDBC 一致，请参考[模式配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/mode/)。

其他配置项请参考：
* [权限配置](/cn/user-manual/shardingsphere-proxy/yaml-config/authentication/)
* [属性配置](/cn/user-manual/shardingsphere-proxy/yaml-config/props/)

3. 配置 `conf/config-*.yaml`

修改 `conf` 目录下以 `config-` 前缀开头的文件，如：`conf/config-sharding.yaml` 文件，进行分片规则、读写分离规则配置。配置方式请参考[配置手册](/cn/user-manual/shardingsphere-proxy/yaml-config/)。`config-*.yaml` 文件的 `*` 部分可以任意命名。
ShardingSphere-Proxy 支持配置多个逻辑数据源，每个以 `config-` 前缀命名的 YAML 配置文件，即为一个逻辑数据源。

4. （可选）引入数据库驱动

如果后端连接 PostgreSQL 或 openGauss 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，请下载 [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) 或者 [mysql-connector-java-8.0.11.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar)，并将其放入 `ext-lib` 目录。

5. （可选）引入集群模式所需依赖

ShardingSphere-Proxy 默认集成 ZooKeeper Curator 客户端，集群模式使用 ZooKeeper 无须引入其他依赖。

如果集群模式使用 Etcd，需要将 Etcd 的客户端驱动程序 [jetcd-core 0.5.0](https://repo1.maven.org/maven2/io/etcd/jetcd-core/0.5.0/jetcd-core-0.5.0.jar) 复制至目录 `ext-lib`。

6. （可选）引入分布式事务所需依赖

与 ShardingSphere-JDBC 使用方式相同。
具体可参考[分布式事务](/cn/user-manual/shardingsphere-jdbc/special-api/transaction/)。

7. （可选）引入自定义算法

当用户需要使用自定义的算法类时，可通过以下方式配置使用自定义算法，以分片为例：

    1. 实现 `ShardingAlgorithm` 接口定义的算法实现类。
    2. 在项目 `resources` 目录下创建 `META-INF/services` 目录。
    3. 在 `META-INF/services` 目录下新建文件 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`
    4. 将实现类的全限定类名写入至文件 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`
    5. 将上述 Java 文件打包成 jar 包。
    6. 将上述 jar 包拷贝至 `ext-lib` 目录。
    7. 将上述自定义算法实现类的 Java 文件引用配置在 YAML 文件中，具体可参考[配置规则](/cn/user-manual/shardingsphere-proxy/yaml-config/)。

8. 启动 ShardingSphere-Proxy

Linux/macOS 操作系统请运行 `bin/start.sh`，Windows 操作系统请运行 `bin/start.bat` 启动 ShardingSphere-Proxy。默认监听端口 `3307`，默认配置目录为 Proxy 内的 `conf` 目录。启动脚本可以指定监听端口、配置文件所在目录，命令如下：

```bash
bin/start.sh [port] [/path/to/conf]
```

9. 使用客户端连接 ShardingSphere-Proxy

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

## 配置示例

完整配置请参考 ShardingSphere 仓库中的示例：
<https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-proxy-example>
