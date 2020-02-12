+++
toc = true
title = "注册中心"
weight = 2
+++

## 注册中心配置

首先需要添加并激活注册中心。可以添加多个注册中心，但只能有一个处于激活状态，后面的配置管理和编排治理功能都是针对当前已激活的注册中心进行操作。
目前提供Zookeeper的支持，后续会添加第三方注册中心的支持。

![Registry Center](https://shardingsphere.apache.org/document/current/img/sharding-ui/registry-center.png)

## 配置管理

可以获取当前注册中心中所有数据源的相关配置，包括数据分片，读写分离、Properties配置等。

![Configuration Management](https://shardingsphere.apache.org/document/current/img/sharding-ui/config-management.png)

可以通过YAML格式对相关配置信息进行修改。

![Configuration ditor](https://shardingsphere.apache.org/document/current/img/sharding-ui/config-edit.png)

## 编排治理

通过注册中心，可以进行熔断数据库访问程序对数据库的访问和禁用从库的操作。

### 熔断实例
可以查看当前运行实例信息，并进行熔断与恢复操作。

![Circuit Breaker](https://shardingsphere.apache.org/document/current/img/sharding-ui/circuit-breaker.png)

### 禁用从库

可以查看所有从库信息，并进行从库禁用与恢复操作。

![Disable Slave Database](https://shardingsphere.apache.org/document/current/img/sharding-ui/disable-slave-database.png)
