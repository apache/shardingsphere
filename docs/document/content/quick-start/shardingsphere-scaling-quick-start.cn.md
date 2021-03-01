+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## 1. 规则配置

编辑`%SHARDINGSPHERE_SCALING_HOME%/conf/server.yaml`。详情请参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。

## 2. 引入依赖

如果后端连接 PostgreSQL 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，请下载 [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar)，并将其放入 `%SHARDINGSPHERE_SCALING_HOME%/lib` 目录。

## 3. 启动服务

```bash
sh %SHARDINGSPHERE_SCALING_HOME%/bin/start.sh
```

## 4. 任务管理

通过相应的 HTTP 接口管理迁移任务。

详情参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。
