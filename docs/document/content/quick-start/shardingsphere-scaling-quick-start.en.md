+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## 1. Rule Configuration

Edit `%SHARDINGSPHERE_SCALING_HOME%/conf/server.yaml`. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-scaling/usage/) for more details.

## 2. Import Dependencies

If the backend database is PostgreSQL, there's no need for additional dependencies.

If the backend database is MySQL, download [MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz) 
and decompress, then copy `mysql-connector-java-5.1.47.jar` to `%SHARDINGSPHERE_SCALING_HOME%/lib` directory.

## 3. Start Server

```bash
sh %SHARDINGSPHERE_SCALING_HOME%/bin/start.sh
```

## 4. Create Migration Job

Use HTTP interface to manage the migration jobs.

Create migration job:

```bash
curl -X POST \
  http://localhost:8888/scaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  props:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\'keyGenerateStrategy
```

Please refer to [Configuration Manual](/en/user-manual/shardingsphere-scaling/usage/) for more details. 
