+++
pre = "<b>3.10.4. </b>"
title = "SQL改写测试引擎"
weight = 4
+++

## 目标

面向逻辑库与逻辑表书写的SQL，并不能够直接在真实的数据库中执行，SQL改写用于将逻辑SQL改写为在真实数据库中可以正确执行的SQL。 它包括正确性改写和优化改写两部分，所以 SQL 改写的测试都是基于这些改写方向进行校验的。

### 测试

SQL 改写测试用例位于 `sharding-core/sharding-core-rewrite` 下的 test 中。SQL 改写的测试主要依赖如下几个部分：

  - 测试引擎
  - 环境配置
  - 验证数据

测试引擎是 SQL 改写测试的入口，跟其他引擎一样，通过 Junit 的 [Parameterized](https://github.com/junit-team/junit4/wiki/Parameterized-tests) 逐条读取 `test\resources` 目录中测试类型下对应的 xml 文件，然后按读取顺序一一进行验证。

环境配置存放在 `test\resources\yaml` 路径中测试类型下对应的 yaml 中。配置了dataSources，shardingRule，encryptRule 等信息，例子如下：

```yaml
dataSources:
  db: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: org.h2.Driver
    jdbcUrl: jdbc:h2:mem:db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password:

## sharding 规则
shardingRule:
  tables:
    t_account:
      actualDataNodes: db.t_account_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: account_id
          algorithmExpression: t_account_${account_id % 2}
      keyGenerateStrategy:
        type: TEST
        column: account_id
    t_account_detail:
      actualDataNodes: db.t_account_detail_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_account_detail_${account_id % 2}
  bindingTables:
    - t_account, t_account_detail
```

验证数据存放在 `test\resources` 路径中测试类型下对应的 xml 文件中。验证数据中， `yaml-rule` 指定了环境以及 rule 的配置文件，`input` 指定了待测试的 SQL 以及参数，`output` 指定了期待的 SQL 以及参数。
其中 `db-type` 决定了 SQL 解析的类型，默认为 `SQL92`, 例如：

```xml
<rewrite-assertions yaml-rule="yaml/sharding/sharding-rule.yaml">
    <!-- 替换数据库类型需要在这里更改 db-type --> 
    <rewrite-assertion id="create_index_for_mysql" db-type="MySQL">
        <input sql="CREATE INDEX index_name ON t_account ('status')" />
        <output sql="CREATE INDEX index_name ON t_account_0 ('status')" />
        <output sql="CREATE INDEX index_name ON t_account_1 ('status')" />
    </rewrite-assertion>
</rewrite-assertions>
```
只需在 xml 文件中编写测试数据，配置好相应的 yaml 配置文件，就可以在不更改任何 Java 代码的情况下校验对应的 SQL 了。
