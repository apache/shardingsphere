+++
pre = "<b>3.10.2. </b>"
title = "整合测试引擎"
weight = 2
+++

## 流程

Junit 中的 `Parameterized` 会聚合起所有的测试数据，并将测试数据一一传递给测试方法进行断言。数据处理就像是沙漏中的流沙：

![](https://shardingsphere.apache.org/document/current/img/test-engine/integration-test.jpg)

### 配置

  - 环境类文件
    - /shardingsphere-test-suite/src/test/resources/integrate/env.properties
    - /shardingsphere-test-suite/src/test/resources/integrate/env/`SQL-TYPE`/dataset.xml
    - /shardingsphere-test-suite/src/test/resources/integrate/env/`SQL-TYPE`/schema.xml
  - 测试用例类文件
    - /shardingsphere-test-suite/src/test/resources/integrate/cases/`SQL-TYPE`/`SQL-TYPE`-integrate-test-cases.xml
    - /shardingsphere-test-suite/src/test/resources/integrate/cases/`SQL-TYPE`/dataset/`FEATURE-TYPE`/*.xml
  - sql-case 文件
    - /sharding-sql-test/src/main/resources/sql/sharding/`SQL-TYPE`/*.xml

### 环境配置 

集成测试需要真实的数据库环境，根据相应的配置文件创建测试环境：

首先，修改配置文件 `/shardingsphere-test-suite/src/test/resources/integrate/env.properties` ，例子如下：

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

其次，修改文件 `/shardingsphere-test-suite/src/test/resources/integrate/env/SQL-TYPE/dataset.xml` 
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

开发者可以在 `schema.xml` 中自定义建库与建表语句。 

### 断言配置

`env.properties` 与 `dataset.xml ` 确定了什么SQL在什么环境执行，下面是断言数据的配置：

断言的配置，需要两种文件，第一类文件位于 `/shardingsphere-test-suite/src/test/resources/integrate/cases/SQL-TYPE/SQL-TYPE-integrate-test-cases.xml`
这个文件类似于一个索引，定义了要执行的SQL，参数以及期待的数据的文件位置。这里的 test-case 引用的就是`sharding-sql-test`中 SQL 对应的`sql-case-id`，例子如下：

```xml
<integrate-test-cases>
    <dml-test-case sql-case-id="insert_with_all_placeholders">
       <assertion parameters="1:int, 1:int, insert:String" expected-data-file="insert_for_order_1.xml" />
       <assertion parameters="2:int, 2:int, insert:String" expected-data-file="insert_for_order_2.xml" />
    </dml-test-case>
</integrate-test-cases>
```
还有一类文件 -- 断言数据，也就是上面配置中的 expected-data-file 对应的文件，文件在 `/shardingsphere-test-suite/src/test/resources/integrate/cases/SQL-TYPE/dataset/FEATURE-TYPE/*.xml`
这个文件内容跟 dataset.xml 很相似，只不过`expected-data-file`文件中不仅定义了断言的数据，还有相应SQL执行后的返回值等。例如：

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

所有需要配置的数据，都已经配置完毕，启动相应的集成测试类即可，全程不需要修改任何 `Java` 代码，只需要在 `xml` 中做数据初始化以及断言，极大的降低了ShardingSphere数据测试的门槛以及复杂度。

## 注意事项

1. 如需测试Oracle，请在pom.xml中增加Oracle驱动依赖。
1. 为了保证测试数据的完整性，整合测试中的分库分表采用了10库10表的方式，因此运行测试用例的时间会比较长。
