+++
pre = "<b>6. </b>"
title = "测试手册"
weight = 6
chapter = true
+++

Apache ShardingSphere 提供了完善的整合测试、模块测试和性能测试。

## 整合测试

通过真实的 Apache ShardingSphere 和数据库的连接，提供端到端的测试。

整合测试引擎以 XML 方式定义 SQL，分别为各个数据库独立运行测试用例。
为了方便上手，测试引擎无需修改任何 **Java** 代码，只需修改相应的配置文件即可运行断言。
测试引擎不依赖于任何第三方环境，用于测试的 ShardingSphere-Proxy 计算节点和数据库均由 Docker 镜像提供。

## 模块测试

将复杂的模块单独提炼成为测试引擎。

模块测试引擎同样以 XML 方式定义 SQL，分别为各个数据库独立运行测试用例，包括 SQL 解析和 SQL 改写模块。

## 性能测试

提供多样性的性能测试方法，包括 Sysbench、JMH、TPCC 等。
