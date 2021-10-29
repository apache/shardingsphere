+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling (Experimental)"
weight = 3
+++

## 1. 规则配置

编辑`%SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml`。详情请参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。

> %SHARDINGSPHERE_PROXY_HOME% 为 Proxy 解压后的路径，例：/Users/ss/shardingsphere-proxy-bin/

## 2. 引入依赖

如果后端连接 PostgreSQL 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，请下载 [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar)，并将其放入 `%SHARDINGSPHERE_PROXY_HOME%/lib` 目录。

## 3. 启动服务

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

## 4. 任务管理

通过相应的 DistSQL 接口管理迁移任务。

详情请参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。

## 5. 相关文档
- [功能#弹性伸缩](/cn/features/scaling/)：核心概念、使用规范
- [用户手册#弹性伸缩](/cn/user-manual/shardingsphere-scaling/)：运行部署、使用手册
- [RAL#弹性伸缩](/cn/user-manual/shardingsphere-proxy/usage/distsql/syntax/ral/ral/#%E5%BC%B9%E6%80%A7%E4%BC%B8%E7%BC%A9)：弹性伸缩的DistSQL
- [开发者手册#弹性伸缩](/cn/dev-manual/scaling/)：SPI接口及实现类
