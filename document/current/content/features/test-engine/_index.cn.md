+++
pre = "<b>3.6. </b>"
title = "测试引擎"
weight = 6
chapter = true
+++

ShardingSphere提供了完善的测试引擎。它以XML方式定义SQL，每条SQL由SQL解析单元测试引擎和整合测试引擎驱动，每个引擎分别为H2、MySQL、PostgreSQL、SQLServer和Oracle数据库运行测试用例。

SQL解析单元测试全面覆盖SQL占位符和字面量维度。整合测试进一步拆分为策略和JDBC两个维度，策略维度包括分库分表、仅分表、仅分库、读写分离等策略，JDBC维度包括Statement、PreparedStatement。

因此，1条SQL会驱动5种数据库的解析 * 2种参数传递类型 + 5种数据库 * 5种分片策略 * 2种JDBC运行方式 = 60个测试用例，以达到ShardingSphere对于高质量的追求。

## 整合测试引擎

### 配置文件

为了让测试更加容易上手，整合测试引擎无需修改任何`Java`代码，通过配置以下几种类型的配置文件即可运行断言：

  - 环境类文件
    - /incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/env.properties
    - /incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/env/`SQL-TYPE`/dataset.xml
    - /incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/env/`SQL-TYPE`/schema.xml
  - 测试用例类文件
    - /incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/cases/`SQL-TYPE`/`SQL-TYPE`-integrate-test-cases.xml
    - /incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/cases/`SQL-TYPE`/dataset/`SHARDING-TYPE`/*.xml
  - sql-case 文件
    - /incubator-shardingsphere/sharding-sql-test/src/main/resources/sql/sharding/`SQL-TYPE`/*.xml

#### 环境配置 

整合测试需要真实的数据库环境，需要根据要测试的数据库创建相关环境并修改相应的配置文件：

首先，修改 `/incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/env.properties` 文件，例如：

```properties
# 测试主键，并发，column index等的开关
run.additional.cases=false

# 分片策略，可指定多种策略
sharding.rule.type=db,tbl,dbtbl_with_masterslave,masterslave

# 要测试的数据库，可以指定多种数据库(H2,MySQL,Oracle,SQLServer,PostgreSQL)
databases=MySQL,PostgreSQL

# MySQL配置
mysql.host=127.0.0.1
mysql.port=13306
mysql.username=root
mysql.password=root

## PostgreSQL配置
postgresql.host=db.psql
postgresql.port=5432
postgresql.username=postgres
postgresql.password=

## SQLServer配置
sqlserver.host=db.mssql
sqlserver.port=1433
sqlserver.username=sa
sqlserver.password=Jdbc1234

## Oracle配置
oracle.host=db.oracle
oracle.port=1521
oracle.username=jdbc
oracle.password=jdbc
```

其次，修改`/incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/env/SQL-TYPE/dataset.xml` 文件。
在`dataset.xml`文件中定义元数据和测试数据。例如：

```xml
<dataset>
    <metadata data-nodes="tbl.t_order_${0..9}">
        <column name="order_id" type="numeric" />
        <column name="user_id" type="numeric" />
        <column name="status" type="varchar" />
    </metadata>
    <row data-node="tbl.t_order_0" values="1000, 10, init" />
    <row data-node="tbl.t_order_1" values="1001, 10, init" />
    <row data-node="tbl.t_order_2" values="1002, 10, init" />
    <row data-node="tbl.t_order_3" values="1003, 10, init" />
    <row data-node="tbl.t_order_4" values="1004, 10, init" />
    <row data-node="tbl.t_order_5" values="1005, 10, init" />
    <row data-node="tbl.t_order_6" values="1006, 10, init" />
    <row data-node="tbl.t_order_7" values="1007, 10, init" />
    <row data-node="tbl.t_order_8" values="1008, 10, init" />
    <row data-node="tbl.t_order_9" values="1009, 10, init" />
</dataset>
```

开发者可以在`schema.xml`中定制化建库与建表语句。

#### SQL配置

设置好集成测试的相关环境以及初始化的数据之后，接下来开发者需要定义待测试的SQL。
待测试的SQL存放在 `/incubator-shardingsphere/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml`文件中。例如：

```xml
<sql-cases>
    <sql-case id="update_without_parameters" value="UPDATE t_order SET status = 'update' WHERE order_id = 1000 AND user_id = 10" />
    <sql-case id="update_with_alias" value="UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?" db-types="MySQL,H2" />
  </sql-cases>
```

开发者通过该文件指定待断言的SQL以及该SQL所适配的数据库类型。`sharding-sql-test`提取为单独的模块，以保证每个SQL用例可以在不同模块的测试引擎中共享。 

#### 断言配置

通过前面的配置，我们确定了什么SQL在什么环境执行的问题，这里我们定义下需要断言的数据。
断言的配置，需要两种文件，第一类文件位于 `/incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/cases/SQL-TYPE/SQL-TYPE-integrate-test-cases.xml`
这个文件类似于一个索引，定义了要执行的SQL，参数以及期待的数据的位置。这里的SQL，引用的就是`sharding-sql-test`中SQL对应的`sql-case-id`，例子如下：

```xml
<integrate-test-cases>
    <dml-test-case sql-case-id="insert_with_all_placeholders">
       <assertion parameters="1:int, 1:int, insert:String" expected-data-file="insert_for_order_1.xml" />
       <assertion parameters="2:int, 2:int, insert:String" expected-data-file="insert_for_order_2.xml" />
    </dml-test-case>
</integrate-test-cases>
```
还有一类文件，就是具体的断言数据，也就是上面配置中的 expected-data-file 对应的文件，文件在 `/incubator-shardingsphere/sharding-integration-test/sharding-jdbc-test/src/test/resources/integrate/cases/SQL-TYPE/dataset/SHARDING-TYPE/*.xml`
这个文件内容根前面提及的 dataset.xml 的内容特别相似，只不过`expected-data-file`文件中不仅定义了断言的数据，还有相应SQL执行后的返回值等。例如：

```xml
<dataset update-count="1">
    <metadata data-nodes="db_${0..9}.t_order">
        <column name="order_id" type="numeric" />
        <column name="user_id" type="numeric" />
        <column name="status" type="varchar" />
    </metadata>
    <row data-node="db_0.t_order" values="1000, 10, update" />
    <row data-node="db_0.t_order" values="1001, 10, init" />
    <row data-node="db_0.t_order" values="2000, 20, init" />
    <row data-node="db_0.t_order" values="2001, 20, init" />
</dataset>
```

至此，所有需要配置的数据，都已经配置完毕，接了来我们启动相应的集成测试类即可，全程不需要修改任何`Java`代码，只需要在`xml`中做数据初始化以及断言，极大的降低了ShardingSphere数据测试的门槛以及复杂度。

### 注意事项

1. 如需测试Oracle，请在pom.xml中增加Oracle驱动依赖。
1. 为了保证测试数据的完整性，整合测试中的分库分表采用了10库10表的方式，因此运行测试用例的时间会比较长。

## SQL解析测试引擎

### 数据准备

SQL解析不需要真实的测试环境，开发者只需定义好待测试的SQL，以及解析后的断言数据即可：

#### SQL数据

在集成测试的部分，我们提到过`sql-case-id`，其对应的SQL，可以在不同模块共享。开发者只需要在`/incubator-shardingsphere/sharding-sql-test/src/main/resources/sql/sharding/SQL-TYPE/*.xml` 添加待测试的SQL即可。

#### 断言解析数据

断言的解析数据保存在 `/incubator-shardingsphere/sharding-core/sharding-core-parse/sharding-core-parse-test/src/test/resources/sharding/SQL-TYPE/*.xml`
在`xml`文件中，我们可以针对表名，token，SQL条件等去进行断言，例如如下的配置：

```xml
<parser-result-sets>
<parser-result sql-case-id="insert_with_multiple_values">
        <tables>
            <table name="t_order" />
        </tables>
        <tokens>
            <table-token start-index="12" table-name="t_order" length="7" />
        </tokens>
        <sharding-conditions>
            <and-condition>
                <condition column-name="order_id" table-name="t_order" operator="EQUAL">
                    <value literal="1" type="int" />
                </condition>
                <condition column-name="user_id" table-name="t_order" operator="EQUAL">
                    <value literal="1" type="int" />
                </condition>
            </and-condition>
            <and-condition>
                <condition column-name="order_id" table-name="t_order" operator="EQUAL">
                    <value literal="2" type="int" />
                </condition>
                <condition column-name="user_id" table-name="t_order" operator="EQUAL">
                    <value literal="2" type="int" />
                </condition>
            </and-condition>
        </sharding-conditions>
    </parser-result>
</parser-result-sets>
```

设置好上面两类数据，开发者就可以通过`sharding-core-parse-test`下对应的engine启动SQL解析的测试了。
