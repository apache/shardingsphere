+++
pre = "<b>6.4. </b>"
title = "Pipeline E2E 测试"
weight = 4
+++

## 测试目的

验证 pipeline 各个场景的功能正确性。

## 测试环境类型

目前支持 NATIVE 和 DOCKER。

1. NATIVE：运行在开发者本机环境。需要在本机启动 ShardingSphere-Proxy 实例和数据库实例。一般用于本机调试。
2. DOCKER：运行在 Maven 插件拉起的 docker 环境。一般用于 GitHub Action，也可以在本机运行。

支持的数据库：MySQL、PostgreSQL、openGauss。

## 使用指南

模块路径 `test/e2e/operation/pipeline`。

### 环境配置

`${DOCKER-IMAGE}` 表示 docker 镜像名称，如 `mysql:5.7` 。 `${DATABASE-TYPE}` 表示数据库类型。

目录：`src/test/resources/env/`
- `e2e-env.properties`：环境配置文件。
- `${DATABASE-TYPE}/global.yaml`：ShardingSphere-Proxy 配置文件。
- `${DATABASE-TYPE}/initdb.sql`：数据库初始化 SQL 文件。
- `${DATABASE-TYPE}/*.cnf,*.conf`：数据库配置文件。
- `common/*.xml`：测试用到的 DistSQL 文件。
- `scenario/`：各个测试场景的 SQL 文件。

### 测试用例

用例示例：MySQLMigrationGeneralE2EIT。
覆盖的功能点如下：
- 库级别迁移（所有表）
- 表级别迁移（任意多个表）
- 迁移数据一致性校验
- 数据迁移过程中支持重启
- 数据迁移支持整型主键
- 数据迁移支持字符串主键
- 使用非管理员账号进行数据迁移

### 运行测试用例

`e2e-env.properties` 所有属性都可以通过 Maven 命令行 `-D` 的方式传入，优先级高于配置文件。

#### NATIVE 环境启动

1. 在本地启动 ShardingSphere-Proxy（使用 3307 端口）：参考 [proxy 启动手册](/cn/user-manual/shardingsphere-proxy/startup/bin/)，或者修改 `proxy/bootstrap/src/main/resources/conf/global.yaml` 之后在 IDE 运行 `org.apache.shardingsphere.proxy.Bootstrap`。

Proxy 配置可以参考：
- test/e2e/operation/pipeline/src/test/resources/env/mysql/server-8.yaml
- test/e2e/operation/pipeline/src/test/resources/env/postgresql/global.yaml
- test/e2e/operation/pipeline/src/test/resources/env/opengauss/global.yaml

2. 启动注册中心（如 ZooKeeper）和数据库。

3. 以 MySQL 为例，`e2e-env.properties` 可以配置如下：
4. 
```
e2e.run.type=NATIVE

e2e.native.database.port=3306
e2e.native.database.username=root
e2e.native.database.password=root
```

4. 找到对应的测试类，在 IDE 启动运行。

#### DOCKER 环境启动

参考 `.github/workflows/e2e-operation.yml`。

1. 打包镜像

```
./mvnw -B clean install -am -pl test/e2e/operation/pipeline -Pit.env.docker -DskipTests
```

运行以上命令会构建出一个用于 E2E 测试的 docker 镜像 `apache/shardingsphere-proxy-test:latest`。

该镜像设置了远程调试的端口，默认是 `3308`。

如果仅修改了测试代码，可以复用已有的测试镜像。

2. 修改 `e2e-env.properties` 配置

```
e2e.run.type=DOCKER
e2e.docker.database.mysql.images=mysql:5.7
```

3. 通过 Maven 运行测试用例。以 MySQL 为例：

```
./mvnw -nsu -B install -f test/e2e/operation/pipeline/pom.xml -De2e.run.type=docker -De2e.docker.database.mysql.images=mysql:5.7
```
