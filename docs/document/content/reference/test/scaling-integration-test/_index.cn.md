+++
title = "Scaling 集成测试"
weight = 4
+++

## 测试目的

验证 Scaling 自身功能和依赖模块的正确性。

## 测试环境

环境准备方式分为 Native 和 Docker，不论哪种环境，本地都需要预先安装Docker。

- Native 环境 用于本地调试，可以使用 IDE 的 debug 模式进行调试。
- Docker 环境  环境由 Maven 运行，适用于云编译环境和测试 ShardingSphere-Proxy 的场景，如：GitHub Action。

当前默认采用 Docker 环境，涉及到的 ShardingSphere-Proxy， Zookeeper， 数据库实例(MySQL，PostgreSQL)， 都通过Docker自动启动。

数据库类型目前支持 MySQL、PostgreSQL、openGauss。

## 使用指南

模块路径：`shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling`

测试的Class分布如下：

核心用例：
- MySQLGeneralScalingIT: 覆盖的测试场景最多，包括部分表迁移，表字段最多样等。
- PostgreSQLGeneralScalingIT: 类似，只不过数据库类型是 PostgreSQL/openGauss，包含自定义schema迁移场景。

主键用例：

- TextPrimaryKeyScalingIT: 支持主键为文本类型的表迁移。


### 配置文件

目录：`resources/env/`
- /common: 存放scaling过程中用到的 DistSQL。
- /{SQL-TYPE}: 存放数据库级别的配置文件。
- /scenario: 存放测试的场景的配置文件，主要是 SQL，不同数据库可能写法不一样。

### 运行测试引擎

所有的属性值都可以通过 Maven 命令行 `-D` 的方式动态注入。

`${image-name}` 表示合法 docker image 名称，比如：mysql:5.7， 多个的话用逗号隔开。
`-Dit.env.postgresql.version=${image-name}` 表示需要测试的 PostgreSQL 版本。
`-Dit.env.mysql.version=${image-name}` 表示需要测试的 MySQL 版本。

#### Native 环境启动

Native 环境要求本地自行启动 ShardingSphere-Proxy（以及其自身依赖的 Cluster，比如 Zookeeper），同时要求 ShardingSphere-Proxy 的端口是3307，数据库会根据用户的配置自行启动，但是对应的端口都是数据库的默认端口（MySQL=3306，PostgreSQL=5432）。

因此 Native 模式下不支持运行多个 Case，每次跑完需要自行清理 Zookeeper 中的信息，以及重启 ShardingSphere-Proxy。

启动方式如下: 找到需要测试的 Case，比如 MySQLGeneralScalingIT， 在启动之前配置对应的 VM Option，新增如下配置。

```
-Dit.cluster.env.type=native -Dit.env.mysql.version=${image-name}
```

在 IDE 下使用 Junit 的方式启动即可。

#### Docker 环境启动

第一步：打包镜像

```bash
./mvnw -B clean install -am -pl shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling -Pit.env.docker -DskipTests
```

运行以上命令会构建出一个用于集成测试的 Docker 镜像 `apache/shardingsphere-proxy-test:latest`。
如果仅修改了测试代码，可以复用已有的测试镜像，无须重新构建。

**Docker 环境配置为 ShardingSphere-Proxy 提供了远程调试端口，默认是3308。**
可以在 ShardingSphereProxyDockerContainer 中自行修改。

#### 运行用例

和 Native 一样，只需要改一个参数。

```
-Dit.cluster.env.type=docker
```

可以和 Native 一样使用 IDE 的方式运行用例，或者使用 maven 的方式运行用例。

```bash
./mvnw -nsu -B install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-scaling/pom.xml -Dit.cluster.env.type=DOCKER -Dit.env.mysql.version=${image-name}
```

#### 注意事项

Scaling集成测试中的命令基本都是只连接 ShardingSphere-Proxy 中执行的，所以如果运行失败，多数情况是需要对 ShardingSphere-Proxy 进行Debug，日志中带有 `:Scaling-Proxy `前缀的，都是从 ShardingSphere-Proxy 容器中输出的日志。
