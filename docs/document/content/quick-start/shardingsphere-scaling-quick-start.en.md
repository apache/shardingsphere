+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling (Experimental)"
weight = 3
+++

## Rule Configuration

Edit `%SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml`.

> %SHARDINGSPHERE_PROXY_HOME% is the shardingsphere proxy extract path. for example: /opt/shardingsphere-proxy-bin/

Please refer to [Build Manual](/en/user-manual/shardingsphere-scaling/build/) for more details.

## Import Dependencies

If the backend database is PostgreSQL, there's no need for additional dependencies.

If the backend database is MySQL, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) and put it into `%SHARDINGSPHERE_PROXY_HOME%/lib` directory.

## Start Server

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh
```

## Create Migration Job

Use DistSQL interface to manage the migration jobs.

Please refer to [Usage Manual](/en/user-manual/shardingsphere-scaling/usage/) for more details. 

## Related documents

- [Features#Scaling](/en/features/scaling/) : Core Concept, User Norms
- [User Manual#Scaling](/en/user-manual/shardingsphere-scaling/) : Build, Manual
- [RAL#Scaling](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#scaling) : DistSQL for Scaling
- [Dev Manual#Scaling](/en/dev-manual/scaling/) : SPI interfaces and implementations
