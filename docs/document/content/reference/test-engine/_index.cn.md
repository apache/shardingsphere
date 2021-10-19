+++
pre = "<b>8.1 </b>"
title = "测试引擎"
weight = 1
chapter = true
+++

Apache ShardingSphere 提供了完善的测试引擎。
它以 XML 方式定义 SQL，分别为各个数据库独立运行测试用例。

为了方便上手，测试引擎无需修改任何 **Java** 代码，只需修改相应的配置文件即可运行断言。
测试引擎不依赖于任何第三方环境，用于测试的 ShardingSphere-Proxy 计算节点和数据库均由 Docker 镜像提供。
