+++
pre = "<b>4.2.1. </b>"
toc = true
title = "使用手册"
weight = 1
+++

1. 下载Sharding-Proxy的最新发行版，地址:https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-3.0.0.M1-SNAPSHOT-v1.tar.gz
1. 解压缩后修改conf/sharding-config.yaml文件，进行分片规则配置. 配置方式请参考[配置手册](/cn/manual/sharding-proxy/configuration/)。
1. Linux操作系统请运行`bin/start.sh`，Windows操作系统请运行`bin/start.bat`启动Sharding-Proxy。
1. 使用任何MySQL的客户端连接。如: `mysql -u root -h 127.0.0.1 -P3307`

## 注意事项

1. Sharding-Proxy默认使用3307端口，可以通过启动脚本追加一个参数作为启动端口号。如: `bin/start.sh 3308`
1. Sharding-Proxy目前只有一个逻辑数据源，名称为`sharding_db`。
1. Sharding-Proxy目前并无授权功能，启动时可用任何用户名和密码登录。
