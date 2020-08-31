+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## 1. 规则配置

编辑`%SHARDINGSPHERE_SCALING_HOME%/conf/server.yaml`。详情请参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。

## 2. 引入依赖

如果后端连接 PostgreSQL 数据库，不需要引入额外依赖。

如果后端连接 MySQL 数据库，需要下载 [MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz)，
解压缩后，将 `mysql-connector-java-5.1.47.jar` 拷贝到 `%SHARDINGSPHERE_SCALING_HOME%/lib` 目录。

## 3. 启动服务

```bash
sh %SHARDINGSPHERE_SCALING_HOME%/bin/start.sh
```

## 4. 创建迁移任务

通过相应的 HTTP 接口管理迁移任务。

创建迁移任务：

```bash
curl -X POST \
  http://localhost:8888/shardingscaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  props:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\'keyGenerateStrategy
```

更多管理接口详情请参见[使用手册](/cn/user-manual/shardingsphere-scaling/usage/)。
