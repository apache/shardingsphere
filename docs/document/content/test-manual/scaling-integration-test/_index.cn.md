+++
pre = "<b>6.4. </b>"
title = "Scaling 集成测试"
weight = 4
+++

## 测试目的

验证数据迁移以及依赖模块功能的正确性。

## 测试环境

目前支持 Native 和 Docker 两种环境。

1. Native 环境直接运行在开发者提供的测试环境中，需要用户自己启动 ShardingSphere-Proxy 和对应的数据库实例，适于调试场景；
1. Docker 环境由 Maven 运行，适用于云编译环境和测试 ShardingSphere-Proxy 的场景，如：GitHub Action。

目前支持的数据库类型：MySQL、PostgreSQL、openGauss。

## 使用指南

模块路径 `shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling` 。

### 环境配置
`${DOCKER-IMAGE}` 表示 docker 镜像名称，如 `mysql:8` 。 `${DATABASE-TYPE}` 表示数据库类型。
目录：`src/test/resources/env`
- `it-env.properties`：集成测试启动参数。
- `${DATABASE-TYPE}/server.yaml`：数据库对应的 ShardingSphere-Proxy 配置文件。
- `${DATABASE-TYPE}/initdb.sql`：数据库初始化 SQL。
- `${DATABASE-TYPE}/*.cnf,*.conf`：以 cnf 或者 conf 结尾的文件，是数据库的配置文件，用于 Docker 挂载。
- `common/command.xml`：测试中用到的DistSQL。
- `scenario/`：存放测试场景中的 SQL。

### 测试用例
目前所有的测试用例，都直接继承自 `BaseExtraSQLITCase`，间接继承了 `BaseITCase`。
- `BaseITCase`：提供了通用方法给子类
- `BaseExtraSQLITCase`：提供了建表、CRUD 语句执行方法

用例示例：MySQLGeneralScalingIT。
覆盖的功能点如下：
- 库级别迁移（所有表）
- 表级别迁移（任意多个表）
- 迁移数据一致性校验
- 数据迁移过程中支持停写
- 数据迁移过程中支持重启
- 数据迁移支持整型主键
- 数据迁移支持字符串主键
- 使用非管理员账号进行数据迁移

### 运行测试用例
`it-env.properties` 所有属性值都可以通过 Maven 命令行 `-D` 的方式传入，优先级高于配置文件。

#### Native 环境启动

使用者在本地提前启动 ShardingSphere-Proxy 以及依赖的配置中心（如 ZooKeeper）和数据库。
要求 ShardingSphere-Proxy 的端口是 3307。
以 MySQL 为例，`it-env.properties` 可以配置如下：
```
scaling.it.env.type=NATIVE
scaling.it.native.database=mysql
scaling.it.native.mysql.username=root
scaling.it.native.mysql.password=root
scaling.it.native.mysql.port=3306
```

找到对应的用例，在 IDE 下使用 Junit 的方式启动即可。

#### Docker环境启动

第一步：打包镜像

```
./mvnw -B clean install -am -pl shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling -Pit.env.docker -DskipTests
```

运行以上命令会构建出一个用于集成测试的 Docker 镜像 apache/shardingsphere-proxy-test:latest，该镜像设置了远程调试的端口，默认是3308。 如果仅修改了测试代码，可以复用已有的测试镜像，无须重新构建。

Docker 模式下，如果需要对 Docker 镜像启动参数进行调整，可以对修改 ShardingSphereProxyDockerContainer 文件中的相关配置。

ShardingSphere-Proxy 输出的日志带有 :Scaling-Proxy 前缀。

使用 Maven 的方式运行用例。以 MySQL 为例：

```
./mvnw -nsu -B install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling/pom.xml -Dscaling.it.env.type=DOCKER -Dscaling.it.docker.mysql.version=${image-name}
```

也可以使用 IDE 的方式运行用例。`it-env.properties` 可以配置如下：

```
scaling.it.env.type=DOCKER
scaling.it.docker.mysql.version=mysql:5.7
```