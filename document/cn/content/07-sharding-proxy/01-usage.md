+++
toc = true
title = "使用手册"
weight = 1
+++

## 简介

Sharding-Proxy是基于Sharding-JDBC基础上增加了针对MySQL协议的代理端，对运维以及调试更加友好，可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQL Workbench等)连接Sharding-Proxy以查询和操作数据。

简单来说，Sharding-Proxy是一个可以分片，读写分离以及数据治理的标准的MySQL(未来也可能推出兼容其他数据库的代理端)。他是星散在后端的繁多的MySQL的门面，它既是控制节点，也是管理节点，还是路由节点。

相对于通过客户端分片的方式的Sharding-JDBC，Sharding-Proxy更加适合运维以及调试时查询数据，可以轻松结合各种MySQL客户端使用。Sharding-JDBC由于减少二次转发成本，性能最高，适合线上程序使用。

## 使用方法

1. 下载Sharding-Proxy的最新发行版，地址:https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz
2. 解压缩后修改conf/sharding-config.yaml文件，进行分片规则配置. 配置方式同[Sharding-JDBC的YAML配置](/02-guide/configuration/)。
3. linux操作系统请运行bin/start.sh，windowa操作系统请运行bin/start.bat启动Sharding-Proxy。
4. 使用任何MySQL的客户端连接。如: mysql -u root -h 127.0.0.1 -P3307

## 注意事项

1. Sharding-Proxy默认使用3307端口，可以通过启动脚本追加一个参数作为启动端口号。如: bin/start.sh 3308
2. Sharding-Proxy目前只有一个逻辑数据源，名称为`sharding_db`。
3. Sharding-Proxy目前并无授权功能，启动时可用任何用户名和密码登录。
