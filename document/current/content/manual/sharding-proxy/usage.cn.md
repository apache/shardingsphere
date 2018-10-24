+++
pre = "<b>4.2.1. </b>"
toc = true
title = "使用手册"
weight = 1
+++


若想使用Sharding-Proxy的数据库治理功能，则需要使用注册中心实现实例熔断和从库禁用功能。详情请参考[支持的注册中心](/cn/features/orchestration/supported-registry-repo/)。

### Zookeeper

1. Sharding-Proxy默认提供了Zookeeper的注册中心解决方案。您只需按照配置规则进行注册中心的配置，即可使用。

### Etcd

1. 将Sharding-Proxy的lib目录下的`sharding-orchestration-reg-zookeeper-curator-${sharding-sphere.version}`文件删除。
1. Maven仓库下载Etcd解决方案的[最新稳定版](http://central.maven.org/maven2/io/shardingsphere/sharding-orchestration-reg-etcd/3.0.0/sharding-orchestration-reg-etcd-3.0.0.jar)jar包。
1. 将下载下来的jar包放到Sharding-Proxy的lib目录下。
1. 按照配置规则进行注册中心的配置，即可使用。

### 其他第三方注册中心

1. 使用SPI方式实现相关逻辑编码，并将生成的jar包放到Sharding-Proxy的lib目录下。
1. 按照配置规则进行注册中心的配置，即可使用。

## 注意事项

1. Sharding-Proxy默认使用3307端口，可以通过启动脚本追加参数作为启动端口号。如: `bin/start.sh 3308`
1. Sharding-Proxy使用conf/server.yaml配置注册中心、认证信息以及公用属性。
1. Sharding-Proxy支持多逻辑数据源，每个以config-前缀命名的yaml配置文件，即为一个逻辑数据源。
