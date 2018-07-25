+++
pre = "<b>4.2.1. </b>"
toc = true
title = "使用手册"
weight = 1
+++

1. 下载Sharding-Proxy的最新发行版，地址:https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.M1.tar.gz
1. 如果使用docker，可以执行`docker pull shardingsphere/sharding-proxy`获取镜像。详细信息请参考[Docker镜像](/cn/manual/sharding-proxy/docker/)。
1. 解压缩后修改conf/config.yaml文件，进行分片规则、读写分离规则配置. 配置方式请参考[配置手册](/cn/manual/sharding-proxy/configuration/)。如需使用自定义
的配置文件，可在conf/下创建配置文件，并在启动时进行配置。
1. Linux操作系统请运行`bin/start.sh`，Windows操作系统请运行`bin/start.bat`启动Sharding-Proxy。如需配置启动端口、配置文件位置，可参考[快速入门](/cn/quick-start/sharding-proxy-quick-start/)
进行启动。
1. 使用任何MySQL的客户端连接。如: `mysql -u root -h 127.0.0.1 -P3307`

## 注意事项

1. Sharding-Proxy默认使用3307端口，可以通过启动脚本追加参数作为启动端口号。如: `bin/start.sh 3308`
1. Sharding-Proxy默认使用conf/config.yaml配置文件，可以通过启动脚本追加参数设置配置文件名称。如 `bin/start.sh 3308 config_master_slave.yaml`
1. Sharding-Proxy目前只有一个逻辑数据源，名称为`sharding_db`。
1. Sharding-Proxy可在conf/config.yaml配置用户名密码，并使用配置的用户名和密码进行proxy的登录。
