+++
pre = "<b>6.1. </b>"
title = "集成测试"
weight = 1
+++

## 设计

集成测试包括 3 个模块：测试用例、测试环境以及测试引擎。

### 测试用例

用于定义待测试的 SQL 以及测试结果的断言数据。
每个用例定义一条 SQL，SQL 可定义多种数据库执行类型。

### 测试环境

用于搭建运行测试用例的数据库和 ShardingSphere-Proxy 环境。
环境又具体分为环境准备方式，数据库类型和场景。

环境准备方式分为 Native 和 Docker，未来还将增加 Embed 类型的支持。

  - Native 环境用于测试用例直接运行在开发者提供的测试环境中，适用于调试场景。
  - Docker 环境由 Testcontainer 创建，适用于云编译环境和测试 ShardingSphere-Proxy 的场景，如：GitHub Action。
  - Embed 环境由测试框架自动搭建嵌入式 MySQL，适用于 ShardingSphere-JDBC 的本地环境测试。

当前默认采用 Docker 环境，使用 Testcontainer 创建运行时环境并执行测试用例。
未来将采用 Embed 环境的 ShardingSphere-JDBC + MySQL，替换 Native 执行测试用例的默认环境类型。 

数据库类型目前支持 MySQL、PostgreSQL、SQLServer 和 Oracle，并且可以支持使用 ShardingSphere-JDBC 或是使用 ShardingSphere-Proxy 执行测试用例。

场景用于对 ShardingSphere 支持规则进行测试，目前支持数据分片和读写分离的相关场景，未来会不断完善场景的组合。

### 测试引擎

用于批量读取测试用例，并逐条执行和断言测试结果。

测试引擎通过将用例和环境进行排列组合，以达到用最少的用例测试尽可能多场景的目的。

每条 SQL 会以 `数据库类型 * 接入端类型 * SQL 执行模式 * JDBC 执行模式 * 场景` 的组合方式生成测试报告，目前各个维度的支持情况如下：

  - 数据库类型：H2、MySQL、PostgreSQL、SQLServer 和 Oracle；
  - 接入端类型：ShardingSphere-JDBC 和 ShardingSphere-Proxy；
  - SQL 执行模式：Statement 和 PreparedStatement；
  - JDBC 执行模式：execute 和 executeQuery（查询） / executeUpdate（更新）；
  - 场景：分库、分表、读写分离和分库分表 + 读写分离。

因此，1 条 SQL 会驱动：`数据库类型（5） * 接入端类型（2） * SQL 执行模式（2） * JDBC 执行模式（2） * 场景（4） = 160` 个测试用例运行，以达到项目对于高质量的追求。

## 使用指南

模块路径：`shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-suite`

### 测试用例配置

SQL 用例在 `resources/cases/${SQL-TYPE}/${SQL-TYPE}-integration-test-cases.xml`。

用例文件格式如下：

```xml
<integration-test-cases>
    <test-case sql="${SQL}">
        <assertion parameters="${value_1}:${type_1}, ${value_2}:${type_2}" expected-data-file="${dataset_file_1}.xml" />
        <!-- ... more assertions -->
        <assertion parameters="${value_3}:${type_3}, ${value_4}:${type_4}" expected-data-file="${dataset_file_2}.xml" />
     </test-case>
    
    <!-- ... more test cases -->
</integration-test-cases>
```

`expected-data-file` 的查找规则是：
  1. 查找同级目录中 `dataset\${SCENARIO_NAME}\${DATABASE_TYPE}\${dataset_file}.xml` 文件；
  2. 查找同级目录中 `dataset\${SCENARIO_NAME}\${dataset_file}.xml` 文件；
  3. 查找同级目录中 `dataset\${dataset_file}.xml` 文件；
  4. 都找不到则报错。

断言文件格式如下：

```xml
<dataset>
    <metadata>
        <column name="column_1" />
        <!-- ... more columns -->
        <column name="column_n" />
    </metadata>
    <row values="value_01, value_02" />
    <!-- ... more rows -->
    <row values="value_n1, value_n2" />
</dataset>
```

### 环境配置

`${SCENARIO-TYPE}` 表示场景名称，在测试引擎运行中用于标识唯一场景。
`${DATABASE-TYPE}` 表示数据库类型。

#### Native 环境配置

目录：`src/test/resources/env/scenario/${SCENARIO-TYPE}`

  - `scenario-env.properties`: 数据源配置；
  - `rules.yaml`: 规则配置；
  - `databases.xml`: 真实库名称；
  - `dataset.xml`: 初始化数据；
  - `init-sql\${DATABASE-TYPE}\init.sql`: 初始化数据库表结构；
  - `authority.xml`: 待补充。

#### Docker 环境配置

目录：`src/test/resources/env/${SCENARIO-TYPE}`

  - `proxy/conf/config-${SCENARIO-TYPE}.yaml`: 规则配置。

**Docker 环境配置为 ShardingSphere-Proxy 提供了远程调试端口，可以在 `shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-fixture/src/test/assembly/bin/start.sh` 文件的 `JAVA_OPTS` 中找到第 2 个暴露的端口用于远程调试。**

### 运行测试引擎

#### 配置测试引擎运行环境

通过配置 `src/test/resources/env/engine-env.properties` 控制测试引擎。

所有的属性值都可以通过 Maven 命令行 `-D` 的方式动态注入。

```properties
# 运行模式，多个值可用逗号分隔。可选值：Standalone, Cluster
it.run.modes=Cluster

# 场景类型，多个值可用逗号分隔。可选值：db, tbl, dbtbl_with_replica_query, replica_query
it.scenarios=db,tbl,dbtbl_with_replica_query,replica_query

# 是否运行附加测试用例
it.run.additional.cases=false

# 配置环境类型，只支持单值。可选值：docker或空，默认值：空
it.cluster.env.type=${it.env}
# 待测试的接入端类型，多个值可用逗号分隔。可选值：jdbc, proxy, 默认值：jdbc
it.cluster.adapters=jdbc

# 场景类型，多个值可用逗号分隔。可选值：H2, MySQL, Oracle, SQLServer, PostgreSQL
it.cluster.databases=H2,MySQL,Oracle,SQLServer,PostgreSQL
```

#### 运行调试模式

  - 标准测试引擎
    运行 `org.apache.shardingsphere.test.integration.engine.${SQL-TYPE}.General${SQL-TYPE}IT` 以启动不同 SQL 类型的测试引擎。

  - 批量测试引擎
    运行 `org.apache.shardingsphere.test.integration.engine.dml.BatchDMLIT`，以启动为 DML 语句提供的测试 `addBatch()` 的批量测试引擎。

  - 附加测试引擎
    运行 `org.apache.shardingsphere.test.integration.engine.${SQL-TYPE}.Additional${SQL-TYPE}IT` 以启动使用更多 JDBC 方法调用的测试引擎。
    附加测试引擎需要通过设置 `it.run.additional.cases=true` 开启。

#### 运行 Docker 模式

```bash
./mvnw -B clean install -f shardingsphere-test/shardingsphere-integration-test/pom.xml -Pit.env.docker -Dit.cluster.adapters=proxy,jdbc -Dit.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -Dit.cluster.databases=MySQL
```

运行以上命令会构建出一个用于集成测试的 Docker 镜像 `apache/shardingsphere-proxy-test:latest`。
如果仅修改了测试代码，可以复用已有的测试镜像，无须重新构建。使用以下命令可以跳过镜像构建，直接运行集成测试：

```bash
./mvnw -B clean install -f shardingsphere-test/shardingsphere-integration-test/shardingsphere-integration-test-suite/pom.xml -Pit.env.docker -Dit.cluster.adapters=proxy,jdbc -Dit.scenarios=${scenario_name_1,scenario_name_2,scenario_name_n} -Dit.cluster.databases=MySQL
```

#### 远程 debug Docker 容器中的 Proxy 代码

IT 测试的 Proxy 镜像默认开启了 3308 端口用于远程调试容器中的实例。  
使用 IDEA 等 IDE 工具可以通过如下方式连接并 debug 容器中的 Proxy 代码：

IDEA -> Run -> Edit Configurations -> Add New Configuration -> Remote JVM Debug

编辑对应的信息：
  - Name：一个描述性的名字，例如 docker-debug。
  - Host：可以访问 docker 的 IP，例如 127.0.0.1。
  - Port：调试端口 3308。
  - use module classpath：项目根目录 shardingsphere。

编辑好上面的信息后，在 IDEA 中 Run -> Run -> docker-debug 即可启动 IDEA 的远程 debug。

#### 注意事项

1. 如需测试 Oracle，请在 pom.xml 中增加 Oracle 驱动依赖；
1. 为了保证测试数据的完整性和易读性，整合测试中的分库分表采用了 10 库 10 表的方式，完全运行测试用例所需时间较长。
