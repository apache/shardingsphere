+++
title = "分布式治理"
weight = 2
+++

ShardingSphere-Proxy 支持使用 SPI 方式接入[分布式治理](/cn/features/governance/management/)，实现配置和元数据统一管理以及实例熔断和从库禁用等功能。

## Zookeeper

ShardingSphere-Proxy 默认提供了 Zookeeper 解决方案，实现了配置中心、注册中心和元数据中心功能。
[配置规则](/cn/user-manual/shardingsphere-jdbc/configuration/yaml/governance/)同 ShardingSphere-JDBC YAML 保持一致。

## 其他第三方组件
详情请参考[支持的第三方组件](/cn/features/governance/management/dependency/)。

1. 使用 SPI 方式实现相关逻辑编码，并将生成的 jar 包复制至 ShardingSphere-Proxy 的 lib 目录。
1. 按照[配置规则](/cn/user-manual/shardingsphere-jdbc/configuration/yaml/governance/)进行配置，即可使用。
