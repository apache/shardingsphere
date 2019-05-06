## 3.1.0

### API调整

1. 调整数据库治理模块的注册中心存储结构。
1. 调整Sharding-JDBC的配置相关API。

### 新功能

1. 支持XA强一致事务。
1. 路由至单一数据节点的SQL 100%全兼容（仅MySQL）。
1. 支持DISTINCT语句。
1. 支持广播表。
1. 解决使用默认分布式自增主键在TPS不高的情况下可能导致数据倾斜的问题。

###  更新日志
1. [MILESTONE #3](https://github.com/sharding-sphere/sharding-sphere/milestone/3)
1. [MILESTONE #4](https://github.com/sharding-sphere/sharding-sphere/milestone/4)


## 3.0.0

### 里程碑

1. Sharding-Proxy发布. 支持以数据库的形式使用ShardingSphere, 全面提供对MySQL命令行以及图形化客户端的支持

### 新功能

#### 内核

1. [ISSUE #290](https://github.com/sharding-sphere/sharding-sphere/issues/290) 支持批量INSERT语句
1. [ISSUE #501](https://github.com/sharding-sphere/sharding-sphere/issues/501) 支持OR语句
1. [ISSUE #980](https://github.com/sharding-sphere/sharding-sphere/issues/980) 支持DCL语句
1. [ISSUE #1111](https://github.com/sharding-sphere/sharding-sphere/issues/1111) 支持MySQL DAL语句

#### Sharding-Proxy

1. [ISSUE #902](https://github.com/sharding-sphere/sharding-sphere/issues/902) 支持XA事务
1. [ISSUE #916](https://github.com/sharding-sphere/sharding-sphere/issues/916) 支持登录认证
1. [ISSUE #936](https://github.com/sharding-sphere/sharding-sphere/issues/936) 支持注册中心进行治理
1. [ISSUE #1046](https://github.com/sharding-sphere/sharding-sphere/issues/1046) 支持多逻辑数据库

### 功能提升

#### 内核

1. [ISSUE #373](https://github.com/sharding-sphere/sharding-sphere/issues/373) 支持`order by ?`
1. [ISSUE #610](https://github.com/sharding-sphere/sharding-sphere/issues/610) 无表名称的DQL采用单播路由
1. [ISSUE #701](https://github.com/sharding-sphere/sharding-sphere/issues/701) 缓存SQL解析结果以提升性能
1. [ISSUE #773](https://github.com/sharding-sphere/sharding-sphere/issues/773) 支持不包含列名的INSERT语句的分片与自增主键
1. [ISSUE #935](https://github.com/sharding-sphere/sharding-sphere/issues/935) 取代`JSON`格式，而将`YAML`格式的配置文件存储在注册中心
1. [ISSUE #1004](https://github.com/sharding-sphere/sharding-sphere/issues/1004) props属性可在分片和读写分离规则配置时独立使用
1. [ISSUE #1205](https://github.com/sharding-sphere/sharding-sphere/issues/1205) 执行引擎提升

#### Sharding-JDBC

1. [ISSUE #652](https://github.com/sharding-sphere/sharding-sphere/issues/652) `Spring Boot Starter` 2.X支持
1. [ISSUE #702](https://github.com/sharding-sphere/sharding-sphere/issues/702) 支持以 `$->{..}` 作为行表达式的标记
1. [ISSUE #719](https://github.com/sharding-sphere/sharding-sphere/issues/719) 支持Spring bean的方式在命名空间中注入自增序列生成器对象
1. [ISSUE #720](https://github.com/sharding-sphere/sharding-sphere/issues/720) 支持Spring bean的方式在命名空间中注入分片算法对象

#### Sharding-Opentracing

1. [ISSUE #1172](https://github.com/sharding-sphere/sharding-sphere/issues/1172) Opentracing提升

### API调整

1. [ISSUE #1153](https://github.com/sharding-sphere/sharding-sphere/issues/1153) 调整Orchestration模块Maven坐标
1. [ISSUE #1203](https://github.com/sharding-sphere/sharding-sphere/issues/1203) 调整数据分片和读写分离的Spring命名空间
1. [ISSUE #1289](https://github.com/sharding-sphere/sharding-sphere/issues/1289) 调整Hint API
1. [ISSUE #1302](https://github.com/sharding-sphere/sharding-sphere/issues/1302) 调整包结构
1. [ISSUE #1305](https://github.com/sharding-sphere/sharding-sphere/issues/1305) 废弃并删除sharding-jdbc-transaction-parent模块
1. [ISSUE #1382](https://github.com/sharding-sphere/sharding-sphere/issues/1328) 去除Orchestration模块中type的配置

### 缺陷修正

#### 内核

1. [ISSUE #569](https://github.com/sharding-sphere/sharding-sphere/issues/569) 当Oracle的SQL中ROWNUM不在语句尾部时解析错误
1. [ISSUE #628](https://github.com/sharding-sphere/sharding-sphere/issues/628) 支持PostgreSQL的数据类型jsonb
1. [ISSUE #646](https://github.com/sharding-sphere/sharding-sphere/issues/646) 当SELECT ITEMS中的别名与GROUP BY或ORDER BY的真实列名对应时无需补列
1. [ISSUE #806](https://github.com/sharding-sphere/sharding-sphere/issues/806) `NOT IN`解析异常
1. [ISSUE #827](https://github.com/sharding-sphere/sharding-sphere/issues/827) 将`SELECT * FROM table WHERE id IN ()`这种SQL跳出死循环
1. [ISSUE #919](https://github.com/sharding-sphere/sharding-sphere/issues/919) 使用Groovy解析行表达式可能导致内存泄漏
1. [ISSUE #993](https://github.com/sharding-sphere/sharding-sphere/issues/993) 无法解析PostgreSQL的双引号占位符
1. [ISSUE #1015](https://github.com/sharding-sphere/sharding-sphere/issues/1015) 支持SQL `SELECT id, COUNT(*) FROM table GROUP BY 1,2`
1. [ISSUE #1120](https://github.com/sharding-sphere/sharding-sphere/issues/1120) `GROUP BY / ORDER BY`产生的补列不应展现在查询结果中
1. [ISSUE #1186](https://github.com/sharding-sphere/sharding-sphere/issues/1186) 在MEMORY_STRICTLY模式中，并发环境下可能产生死锁
1. [ISSUE #1265](https://github.com/sharding-sphere/sharding-sphere/issues/1265) 当AtomicInteger溢出后，RoundRobinMasterSlaveLoadBalanceAlgorithm抛出ArrayIndexOutOfBoundsException异常

#### Sharding-JDBC

1. [ISSUE #372](https://github.com/sharding-sphere/sharding-sphere/issues/372) 同一PreparedStatement反复使用导致路由缓存未清理
1. [ISSUE #629](https://github.com/sharding-sphere/sharding-sphere/issues/629) 支持JDBC中设置事务隔离级别
1. [ISSUE #735](https://github.com/sharding-sphere/sharding-sphere/issues/735) 在Mybatis中使用`Round-robin`的读写分离算法路由存在问题
1. [ISSUE #1011](https://github.com/sharding-sphere/sharding-sphere/issues/1011) 无法在`Spring Boot`的`YAML`中处理占位符

## 2.0.3

### 新功能

#### 内核

1. [ISSUE #600](https://github.com/sharding-sphere/sharding-sphere/issues/600) 支持TCL

### 缺陷修正

#### 内核

1. [ISSUE #540](https://github.com/sharding-sphere/sharding-sphere/issues/540) 梳理并支持别名为关键字SQL
1. [ISSUE #577](https://github.com/sharding-sphere/sharding-sphere/issues/577) 支持`YAML`配置换行

#### Sharding-JDBC

1. [ISSUE #522](https://github.com/sharding-sphere/sharding-sphere/issues/522) 读写分离的从库不需要执行DDL语句


## 2.0.2

### 功能提升

#### 内核
1. [ISSUE #475](https://github.com/sharding-sphere/sharding-sphere/issues/475) 支持`CREATE INDEX`
1. [ISSUE #525](https://github.com/sharding-sphere/sharding-sphere/issues/525) 支持`DROP INDEX`

### 缺陷修正

#### 内核

1. [ISSUE #521](https://github.com/sharding-sphere/sharding-sphere/issues/521) `YAML`文件中`ShardingProperties`设置无效
1. [ISSUE #529](https://github.com/sharding-sphere/sharding-sphere/issues/529) 表名大写无法查询
1. [ISSUE #541](https://github.com/sharding-sphere/sharding-sphere/issues/541) 无法解析`IS NOT NULL`
1. [ISSUE #557](https://github.com/sharding-sphere/sharding-sphere/issues/557) `GROUP BY`和`ORDER BY`仅别名不一致问题应使用流式归并
1. [ISSUE #559](https://github.com/sharding-sphere/sharding-sphere/issues/559) 支持解析以负号和小数点开头的数字(如: `-.12`)
1. [ISSUE #567](https://github.com/sharding-sphere/sharding-sphere/issues/567) MySQL补列时增加转义符以防止使用关键字作为列名或别名导致错误

#### Sharding-JDBC

1. [ISSUE #520](https://github.com/sharding-sphere/sharding-sphere/issues/520) 唯一键冲突时异常类型不是`DuplicateKeyException`


## 2.0.1

### 功能提升

#### 内核

1. [ISSUE #490](https://github.com/sharding-sphere/sharding-sphere/issues/490) Oracle使用`rownum`大于等于或小于等于分页结果不正确
1. [ISSUE #496](https://github.com/sharding-sphere/sharding-sphere/issues/496) 分片配置中逻辑表名可以大小写不敏感
1. [ISSUE #497](https://github.com/sharding-sphere/sharding-sphere/issues/497) 注册中心优雅关闭

### 缺陷修正

#### Sharding-JDBC

1. [ISSUE #489](https://github.com/sharding-sphere/sharding-sphere/issues/489) 在Spring namespace中使用`RuntimeBeanReference`防止创建`InnerBean`
1. [ISSUE #491](https://github.com/sharding-sphere/sharding-sphere/issues/491) 通过`ResultSet.getStatement().getConnection().close()`无法释放连接

## 2.0.0

### 里程碑

1. API调整. 全新的`Maven`坐标名称, 包名称和spring命名空间名称. 简化和提升API配置, 行表达式全配置支持
1. 提供`Sharding-JDBC`的`spring-boot-starter`
1. 配置动态化. 可以通过`ZooKeeper`和`etcd`作为注册中心动态修改数据源以及分片配置
1. 数据库治理. 熔断数据库访问程序对数据库的访问和禁用从库的访问
1. ConfigMap支持. 可以在分片和读写分离策略中获取预定义的元数据
1. 追踪系统支持. 可以通过`sky-walking`等基于`Opentracing`协议的APM系统中查看`Sharding-JDBC`的调用链

### 功能提升

#### 内核

1. [ISSUE #386](https://github.com/sharding-sphere/sharding-sphere/issues/386) 支持不包含表名称的SQL, 例如: `SELECT 1`

#### Sharding-JDBC

1. [ISSUE #407](https://github.com/sharding-sphere/sharding-sphere/issues/407) `sharding-jdbc-spring-boot-starter`兼容使用减号和驼峰两种方式进行属性配置
1. [ISSUE #424](https://github.com/sharding-sphere/sharding-sphere/issues/424) 提供SQL总体执行情况事件

### 缺陷修正

#### 内核

1. [ISSUE #387](https://github.com/sharding-sphere/sharding-sphere/issues/387) 当函数 + 列名中存在'`'防止关键字时处理出错
1. [ISSUE #419](https://github.com/sharding-sphere/sharding-sphere/issues/419) SQL改写时, 未判断别名是否为关键字未加转义符导致了SQL异常
1. [ISSUE #464](https://github.com/sharding-sphere/sharding-sphere/issues/464) SQL如果varchar类型由于没有匹配单引号并未关闭, 而恰好sql中的下一个varchar又是汉字的错误SQL, 将导致CPU使用增高

#### Sharding-JDBC

1. [ISSUE #394](https://github.com/sharding-sphere/sharding-sphere/issues/394) 无法单独close statement
1. [ISSUE #398](https://github.com/sharding-sphere/sharding-sphere/issues/398) 使用Hint路由屏蔽表和列名称的大小写区别
1. [ISSUE #404](https://github.com/sharding-sphere/sharding-sphere/issues/404) sharding-jdbc的spring-boot-starter不支持HikariDataSource
1. [ISSUE #436](https://github.com/sharding-sphere/sharding-sphere/issues/436) 读写分离当从库配置RoundRobin算法并使用MyBatis时，只能路由到同一从库
1. [ISSUE #452](https://github.com/sharding-sphere/sharding-sphere/issues/452) DDL语句分片至多个表会造成连接泄漏的问题
1. [ISSUE #472](https://github.com/sharding-sphere/sharding-sphere/issues/472) Connection执行createStatement之前, 先调用getMetaData再setAutoCommit无法对之后创建的数据库真实连接生效

## 1.5.4.1

### 缺陷修正

1. [ISSUE #382](https://github.com/sharding-sphere/sharding-sphere/issues/382) 使用完全未配置分片策略的表无法完成查询

## 1.5.4

### 缺陷修正

1. [ISSUE #356](https://github.com/sharding-sphere/sharding-sphere/issues/356) 在SQL的Where条件中兼容不是分片列的REGEXP操作符
1. [ISSUE #362](https://github.com/sharding-sphere/sharding-sphere/issues/362) 读写分离使用PreparedStatement并未调用setParameter方法导致出错
1. [ISSUE #370](https://github.com/sharding-sphere/sharding-sphere/issues/370) 使用原生自增主键调用getGeneratedKeys出错
1. [ISSUE #375](https://github.com/sharding-sphere/sharding-sphere/issues/375) 路由至单节点的分页第二页以后的查询取不到数据
1. [ISSUE #379](https://github.com/sharding-sphere/sharding-sphere/issues/379) 使用Mybatis时框架调用Connection.getMetaData()时释放连接不正确

## 1.5.3

### 功能提升

1. [ISSUE #98](https://github.com/sharding-sphere/sharding-sphere/issues/98) 读写分离负载均衡策略支持配置
1. [ISSUE #196](https://github.com/sharding-sphere/sharding-sphere/issues/196) 读写分离与分库分表配置独立

### 缺陷修正

1. [ISSUE #349](https://github.com/sharding-sphere/sharding-sphere/issues/349) ResultSet.wasNull功能不正确导致DB中的数字类型空值取出为零
1. [ISSUE #351](https://github.com/sharding-sphere/sharding-sphere/issues/351) 包含在默认数据源但不在TableRule配置的表无法正确执行
1. [ISSUE #353](https://github.com/sharding-sphere/sharding-sphere/issues/353) 在SQL的Where条件中兼容不是分片列的!=, !> 和 !< 操作符
1. [ISSUE #354](https://github.com/sharding-sphere/sharding-sphere/issues/354) 在SQL的Where条件中兼容不是分片列的NOT操作符

## 1.5.2

### 里程碑

1. 质量保障的测试引擎，每条SQL可以运行60个不同维度的测试用例

### 功能提升

1. [ISSUE #335](https://github.com/sharding-sphere/sharding-sphere/issues/335) 支持GROUP BY + 自定义函数的SQL
1. [ISSUE #341](https://github.com/sharding-sphere/sharding-sphere/issues/341) 支持Oracle中的ORDER BY xxx NULLS FIRST | LAST 语句

### 缺陷修正

1. [ISSUE #334](https://github.com/sharding-sphere/sharding-sphere/issues/334) 解析有函数的ORDER BY会将后面的ASC, DESC解析到OrderItem的name属性中
1. [ISSUE #335](https://github.com/sharding-sphere/sharding-sphere/issues/339) 使用表全名关联的JOIN解析不正确
1. [ISSUE #346](https://github.com/sharding-sphere/sharding-sphere/issues/346) DDL语句 DROP TABLE IF EXISTS USER 解析表名错误

## 1.5.1

### 新功能

1. [ISSUE #314](https://github.com/sharding-sphere/sharding-sphere/issues/314) 支持DDL类型的SQL

### 功能调整

1. [ISSUE #327](https://github.com/sharding-sphere/sharding-sphere/issues/327) 默认关闭sql.show配置

### 缺陷修正

1. [ISSUE #308](https://github.com/sharding-sphere/sharding-sphere/issues/308) 数据库原生的自增GeneratedKey的返回无效
1. [ISSUE #309](https://github.com/sharding-sphere/sharding-sphere/issues/310) 子查询中的ORDER BY和GROUP BY不列入解析上下文
1. [ISSUE #313](https://github.com/sharding-sphere/sharding-sphere/issues/313) 支持<>操作符
1. [ISSUE #317](https://github.com/sharding-sphere/sharding-sphere/issues/317) LIMIT参数不能是Long类型
1. [ISSUE #320](https://github.com/sharding-sphere/sharding-sphere/issues/320) GROUP BY + LIMIT的SQL改写错误
1. [ISSUE #323](https://github.com/sharding-sphere/sharding-sphere/issues/323) 解析ORDER BY + 聚合表达式错误

## 1.5.0

### 里程碑

1. 全新的SQL解析模块，去掉对Druid的依赖。仅解析分片上下文，对于SQL采用"半理解"理念，进一步提升性能和兼容性，并降低代码复杂度
1. 全新的SQL改写模块，增加优化性改写模块
1. 全新的SQL归并模块，重构为流式、内存以及装饰者3种归并引擎

### 新功能

1. 增加对Oracle，SQLServer和PostgreSQL的支持
1. 非功能型子查询支持

### 功能提升

1. [ISSUE #256](https://github.com/sharding-sphere/sharding-sphere/issues/256) 可配置显示分片执行SQL日志
1. [ISSUE #291](https://github.com/sharding-sphere/sharding-sphere/issues/291) 用流式方式处理仅包含GroupBy的SQL

### 功能调整

1. 简化分布式自增序列。将每个表支持多自增序列简化为单表仅支持单一的分布式自增序列，并不再支持通过环境变量设置workerID
1. 去掉对OR的支持

### 缺陷修正

1. [ISSUE #239](https://github.com/sharding-sphere/sharding-sphere/issues/239) LIMIT路由至多查询结果集，若只有一个不为空的结果集，分页结果不正确
1. [ISSUE #263](https://github.com/sharding-sphere/sharding-sphere/issues/263) 分片列和逻辑表配置可忽略大小写
1. [ISSUE #292](https://github.com/sharding-sphere/sharding-sphere/issues/292) 内存方式处理GROUP BY语句如有分页信息则需改写
1. [ISSUE #295](https://github.com/sharding-sphere/sharding-sphere/issues/295) LIMIT 0的情况并未按照分页限制条件过滤结果集

## 1.4.2

### 功能提升

1. [ISSUE #219](https://github.com/sharding-sphere/sharding-sphere/issues/219) 线程性能优化
1. [ISSUE #215](https://github.com/sharding-sphere/sharding-sphere/issues/215) 流式排序的聚集结果集 StreamingOrderByReducerResultSet性能优化
1. [ISSUE #161](https://github.com/sharding-sphere/sharding-sphere/issues/161) 结果集归并的时候可以采用堆排序来提升性能

### 缺陷修正

1. [ISSUE #212](https://github.com/sharding-sphere/sharding-sphere/issues/212) 对缺少数据源规则给出更有意义的提示
1. [ISSUE #214](https://github.com/sharding-sphere/sharding-sphere/issues/214) where中 table_name.column_name in (?,?)无法解析表达式
1. [ISSUE #180](https://github.com/sharding-sphere/sharding-sphere/issues/180) 批量执行Update返回值不准确
1. [ISSUE #225](https://github.com/sharding-sphere/sharding-sphere/issues/225) 自动生成Id最后一位不归零

## 1.4.1

### 功能提升

1. [ISSUE #191](https://github.com/sharding-sphere/sharding-sphere/issues/191) 根据主机的IP生成workerId的KeyGenerator实现
1. [ISSUE #192](https://github.com/sharding-sphere/sharding-sphere/issues/192) 根据HOSTNAME的数字尾缀获取workerId的KeyGenerator
1. [ISSUE #210](https://github.com/sharding-sphere/sharding-sphere/issues/210) 路由到单库单表移除补充的SQL语句片段

### 缺陷修正

1. [ISSUE #194](https://github.com/sharding-sphere/sharding-sphere/issues/194) Connection, Statement, ResultSet等接口中的close方法中部分组件异常造成另外一部分组件的close方法没有被调用
1. [ISSUE #199](https://github.com/sharding-sphere/sharding-sphere/issues/199) 分表且复用PreparedStatement对象造成数据路由错误
1. [ISSUE #201](https://github.com/sharding-sphere/sharding-sphere/issues/201) 批量操作执行前事件发送缺失
1. [ISSUE #203](https://github.com/sharding-sphere/sharding-sphere/issues/203) 合并batch操作发送的事件
1. [ISSUE #209](https://github.com/sharding-sphere/sharding-sphere/issues/209) 并行执行多个limit查询导致IndexOutOfBoundsException

## 1.4.0

### 功能提升

自动生成键实现，包含

1. [ISSUE #162](https://github.com/sharding-sphere/sharding-sphere/issues/162) 分布式主键算法实现
1. [ISSUE #163](https://github.com/sharding-sphere/sharding-sphere/issues/163) 获取自增序列jdbc接口实现
1. [ISSUE #171](https://github.com/sharding-sphere/sharding-sphere/issues/171) sharding-jdbc-core配合自动生成序列改造
1. [ISSUE #172](https://github.com/sharding-sphere/sharding-sphere/issues/172) YAML与Spring的配置方式增加对于自增序列的支持

### 缺陷修正

1. [ISSUE #176](https://github.com/sharding-sphere/sharding-sphere/issues/176) AbstractMemoryResultSet的wasNull标志位没有及时复位

## 1.3.3

### 功能提升

1. [ISSUE #59](https://github.com/sharding-sphere/sharding-sphere/issues/59) PreparedStatement设置参数时可以根据参数类型调用正确的底层set方法

### 缺陷修正

1. [ISSUE #149](https://github.com/sharding-sphere/sharding-sphere/issues/149) INSERT IGNORE INTO时如果数据重了忽略时返回的成-1了，应该返回0 
1. [ISSUE #118](https://github.com/sharding-sphere/sharding-sphere/issues/118) 同一个线程内先执行DQL后执行DML，DML操作在从库上执行
1. [ISSUE #122](https://github.com/sharding-sphere/sharding-sphere/issues/122) 在连接不可用的情况下(如网络中断),应该直接中断事务,而不是重试
1. [ISSUE #152](https://github.com/sharding-sphere/sharding-sphere/issues/152) PreparedStatement的缓存导致数组越界
1. [ISSUE #150](https://github.com/sharding-sphere/sharding-sphere/issues/150) 与最新SQLServer jdbc驱动兼容问题，应该将Product Name由SQLServer改为Microsoft SQL Server
1. [ISSUE #166](https://github.com/sharding-sphere/sharding-sphere/issues/166) Druid数据源stat过滤器多线程报错，应该增加数据库连接级别的同步

## 1.3.2

### 功能提升

1. [ISSUE #79](https://github.com/sharding-sphere/sharding-sphere/issues/79) 对于只有一个目标表的情况优化limit，不修改limit的偏移量

### 缺陷修正

1. [ISSUE #36](https://github.com/sharding-sphere/sharding-sphere/issues/36) ShardingPreparedStatement无法反复设置参数
1. [ISSUE #114](https://github.com/sharding-sphere/sharding-sphere/issues/114) ShardingPreparedStatement执行批处理任务时,反复解析sql导致OOM
1. [ISSUE #33](https://github.com/sharding-sphere/sharding-sphere/issues/33) 根据MySQL文档，不支持类似limit 100 , -1格式的查询
1. [ISSUE #124](https://github.com/sharding-sphere/sharding-sphere/issues/124) com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractStatementAdapter.getUpdateCount返回值不符合JDBC规范
1. [ISSUE #141](https://github.com/sharding-sphere/sharding-sphere/issues/141) 多线程执行器参数设置失效


## 1.3.1

### 功能提升

1. [ISSUE #91](https://github.com/sharding-sphere/sharding-sphere/issues/91) 开放对Statement.getGeneratedKeys的支持，可返回原生的数据库自增主键
1. [ISSUE #92](https://github.com/sharding-sphere/sharding-sphere/issues/92) 查询类DQL语句事件发送

### 缺陷修正

1. [ISSUE #89](https://github.com/sharding-sphere/sharding-sphere/issues/89) 读写分离和分片的hint一起使用导致冲突
1. [ISSUE #95](https://github.com/sharding-sphere/sharding-sphere/issues/95) 同一线程内写入操作后的读操作均从主库读取改为同一线程且同一连接内

## 1.3.0

### 新功能

1. [ISSUE #85](https://github.com/sharding-sphere/sharding-sphere/issues/85) 读写分离

### 功能提升

1. [ISSUE #82](https://github.com/sharding-sphere/sharding-sphere/issues/82) TableRule可传入dataSourceName属性，用于指定该TableRule对应的数据源
1. [ISSUE #88](https://github.com/sharding-sphere/sharding-sphere/issues/88) 放开对其他数据库的限制，可支持标准SQL, 对个性化分页等语句不支持

### 缺陷修正

1. [ISSUE #81](https://github.com/sharding-sphere/sharding-sphere/issues/81) 关联表查询使用or查询条件解析结果异常

## 1.2.1

### 结构调整

1. [ISSUE #60](https://github.com/sharding-sphere/sharding-sphere/issues/60) API调整，抽离ShardingDataSource，使用工厂代替
1. [ISSUE #76](https://github.com/sharding-sphere/sharding-sphere/issues/76) ShardingRule和TableRule调整为Builder模式
1. [ISSUE #77](https://github.com/sharding-sphere/sharding-sphere/issues/77) ShardingRule和TableRule调整为Builder模式

### 功能提升

1. [ISSUE #61](https://github.com/sharding-sphere/sharding-sphere/issues/61) 在ShardingValue类中加入逻辑表名
1. [ISSUE #66](https://github.com/sharding-sphere/sharding-sphere/issues/66) 在JDBC层的Statement增加对get/set MaxFieldSize，MaxRows和QueryTimeout的支持
1. [ISSUE #72](https://github.com/sharding-sphere/sharding-sphere/issues/72) 对于select union all形式的批量插入支持
1. [ISSUE #78](https://github.com/sharding-sphere/sharding-sphere/issues/78) 简化只分库配置，无需配置逻辑表和真实表对应关系
1. [ISSUE #80](https://github.com/sharding-sphere/sharding-sphere/issues/80) 简化包含不分片库表的配置，可指定默认数据源，不分片无需配置TableRule

### 缺陷修正

1. [ISSUE #63](https://github.com/sharding-sphere/sharding-sphere/issues/63) ORDER BY与GROUP BY衍生列未添加表名或表别名
1. [ISSUE #65](https://github.com/sharding-sphere/sharding-sphere/issues/65) 解析条件上下文性能提升
1. [ISSUE #67](https://github.com/sharding-sphere/sharding-sphere/issues/67) 分片路由到多表时柔性事务日志无法删除
1. [ISSUE #71](https://github.com/sharding-sphere/sharding-sphere/issues/71) 路由单分片LIMIT的OFFSET计算错误
1. [ISSUE #75](https://github.com/sharding-sphere/sharding-sphere/issues/75) MemoryTransactionLogStorage重试次数更新并发问题

## 1.2.0

### 新功能

1. [ISSUE #53](https://github.com/sharding-sphere/sharding-sphere/issues/53) 可以不配置真实表和逻辑表的对应关系，通过分片算法动态计算真实表
1. [ISSUE #58](https://github.com/sharding-sphere/sharding-sphere/issues/58) 柔性事务：最大努力送达型初始版本

### 结构调整

1. [ISSUE #49](https://github.com/sharding-sphere/sharding-sphere/issues/49) 调整属性配置
1. [ISSUE #51](https://github.com/sharding-sphere/sharding-sphere/issues/51) 重构Hint接口

### 缺陷修正

1. [ISSUE #43](https://github.com/sharding-sphere/sharding-sphere/issues/43) yaml文件中包含中文，且操作系统模式不是utf-8编码导致的yaml不能解析
1. [ISSUE #48](https://github.com/sharding-sphere/sharding-sphere/issues/48) yaml文件读取后未关闭
1. [ISSUE #57](https://github.com/sharding-sphere/sharding-sphere/issues/57) 在解析层面对子查询进行识别，保证补充列行为可以进行精准定位

## 1.1.0

### 新功能

1. [ISSUE #40](https://github.com/sharding-sphere/sharding-sphere/issues/40) 支持YAML文件配置
1. [ISSUE #41](https://github.com/sharding-sphere/sharding-sphere/issues/41) 支持Spring命名空间配置
1. [ISSUE #42](https://github.com/sharding-sphere/sharding-sphere/issues/42) 支持inline表达式配置

### 缺陷修正

1. [ISSUE #25](https://github.com/sharding-sphere/sharding-sphere/issues/25) OR表达式下会出现重复结果问题

## 1.0.1

### 功能提升

1. [ISSUE #39](https://github.com/sharding-sphere/sharding-sphere/issues/39) 增加使用暗示(Hint)方式注册分片键值的方式进行SQL路由的功能

### 缺陷修正

1. [ISSUE #11](https://github.com/sharding-sphere/sharding-sphere/issues/11) count函数在没有别名的情况下返回不正确
1. [ISSUE #13](https://github.com/sharding-sphere/sharding-sphere/issues/13) Insert语句没有写列名，或者写列名但列名不包含分片字段，进行了全路由
1. [ISSUE #16](https://github.com/sharding-sphere/sharding-sphere/issues/16) 由每次执行SQL时新建连接池，应改为每个ShardingDataSource对象共享一个连接池
1. [ISSUE #18](https://github.com/sharding-sphere/sharding-sphere/issues/18) 查询Count时，调用getObject()抛出异常: Unsupported data type: Object
1. [ISSUE #19](https://github.com/sharding-sphere/sharding-sphere/issues/19) sum和avg函数，不加别名不执行merger，加了空指针异常
1. [ISSUE #38](https://github.com/sharding-sphere/sharding-sphere/issues/38) JPA与Sharding-JDBC的兼容问题。JPA会自动增加SELECT的列别名，导致ORDER BY只能通过别名，而非列名称获取ResultSet的数据

## 1.0.0

1. 初始版本
