+++
title = "使用注册中心"
weight = 2
+++

若想使用 ShardingSphere-Proxy 的数据库治理功能，则需要使用注册中心实现实例熔断和从库禁用功能。
详情请参考[支持的注册中心](/cn/features/orchestration/supported-registry-repo/)。

## Zookeeper

1. ShardingSphere-Proxy 默认提供了 Zookeeper 的注册中心解决方案。
开发者只需按照[配置规则](/cn/user-manual/shardingsphere-proxy/configuration/)进行注册中心的配置，即可使用。

## 其他第三方注册中心

1. 将 ShardingSphere-Proxy 的 lib 目录下的 `shardingsphere-orchestration-reg-zookeeper-curator-${shardingsphere.version}.jar` 文件删除。
1. 使用 SPI 方式实现相关逻辑编码，并将生成的 jar 包复制至 ShardingSphere-Proxy 的 lib 目录。
1. 按照[配置规则](/cn/user-manual/shardingsphere-proxy/configuration/)进行注册中心的配置，即可使用。
