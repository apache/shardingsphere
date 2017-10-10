## 2.0.0.M1

### 里程碑

1. 配置动态化. 可以通过zookeeper作为注册中心动态修改数据源以及分片配置
1. API调整. 全新的Maven坐标名称, 包名称和spring命名空间名称. 简化和提升API配置, inline表达式全配置支持

### 新功能

1. 新增sharding-jdbc-spring-boot-starter功能

### 功能提升

1. [ISSUE #386](https://github.com/shardingjdbc/sharding-jdbc/issues/386) 支持SELECT 1这种不包含表名称的SQL

### 缺陷修正

1. [ISSUE #387](https://github.com/shardingjdbc/sharding-jdbc/issues/387) 当函数+列名中存在'`'防止关键字时处理出错
1. [ISSUE #394](https://github.com/shardingjdbc/sharding-jdbc/issues/394) 无法单独close statement
1. [ISSUE #398](https://github.com/shardingjdbc/sharding-jdbc/issues/398) 使用Hint路由屏蔽表和列名称的大小写区别

## 1.5.4.1

### 缺陷修正

1. [ISSUE #382](https://github.com/shardingjdbc/sharding-jdbc/issues/382) 使用完全未配置分片策略的表无法完成查询

## 1.5.4

### 缺陷修正

1. [ISSUE #356](https://github.com/shardingjdbc/sharding-jdbc/issues/356) 在SQL的Where条件中兼容不是分片列的REGEXP操作符
1. [ISSUE #362](https://github.com/shardingjdbc/sharding-jdbc/issues/362) 读写分离使用PreparedStatement并未调用setParameter方法导致出错
1. [ISSUE #370](https://github.com/shardingjdbc/sharding-jdbc/issues/370) 使用原生自增主键调用getGeneratedKeys出错
1. [ISSUE #375](https://github.com/shardingjdbc/sharding-jdbc/issues/375) 路由至单节点的分页第二页以后的查询取不到数据
1. [ISSUE #379](https://github.com/shardingjdbc/sharding-jdbc/issues/379) 使用Mybatis时框架调用Connection.getMetaData()时释放连接不正确

## 1.5.3

### 功能提升

1. [ISSUE #98](https://github.com/shardingjdbc/sharding-jdbc/issues/98) 读写分离负载均衡策略支持配置
1. [ISSUE #196](https://github.com/shardingjdbc/sharding-jdbc/issues/196) 读写分离与分库分表配置独立

### 缺陷修正

1. [ISSUE #349](https://github.com/shardingjdbc/sharding-jdbc/issues/349) ResultSet.wasNull功能不正确导致DB中的数字类型空值取出为零
1. [ISSUE #351](https://github.com/shardingjdbc/sharding-jdbc/issues/351) 包含在默认数据源但不在TableRule配置的表无法正确执行
1. [ISSUE #353](https://github.com/shardingjdbc/sharding-jdbc/issues/353) 在SQL的Where条件中兼容不是分片列的!=, !> 和 !< 操作符
1. [ISSUE #354](https://github.com/shardingjdbc/sharding-jdbc/issues/354) 在SQL的Where条件中兼容不是分片列的NOT操作符

## 1.5.2

### 里程碑

1. 质量保障的测试引擎，每条SQL可以运行60个不同维度的测试用例

### 功能提升

1. [ISSUE #335](https://github.com/shardingjdbc/sharding-jdbc/issues/335) 支持GROUP BY + 自定义函数的SQL
1. [ISSUE #341](https://github.com/shardingjdbc/sharding-jdbc/issues/341) 支持Oracle中的ORDER BY xxx NULLS FIRST | LAST 语句

### 缺陷修正

1. [ISSUE #334](https://github.com/shardingjdbc/sharding-jdbc/issues/334) 解析有函数的ORDER BY会将后面的ASC, DESC解析到OrderItem的name属性中
1. [ISSUE #335](https://github.com/shardingjdbc/sharding-jdbc/issues/339) 使用表全名关联的JOIN解析不正确
1. [ISSUE #346](https://github.com/shardingjdbc/sharding-jdbc/issues/346) DDL语句 DROP TABLE IF EXISTS USER 解析表明错误

## 1.5.1

### 新功能

1. [ISSUE #314](https://github.com/shardingjdbc/sharding-jdbc/issues/314) 支持DDL类型的SQL

### 功能调整

1. [ISSUE #327](https://github.com/shardingjdbc/sharding-jdbc/issues/327) 默认关闭sql.show配置

### 缺陷修正

1. [ISSUE #308](https://github.com/shardingjdbc/sharding-jdbc/issues/308) 数据库原生的自增GeneratedKey的返回无效
1. [ISSUE #309](https://github.com/shardingjdbc/sharding-jdbc/issues/310) 子查询中的ORDER BY和GROUP BY不列入解析上下文
1. [ISSUE #313](https://github.com/shardingjdbc/sharding-jdbc/issues/313) 支持<>操作符
1. [ISSUE #317](https://github.com/shardingjdbc/sharding-jdbc/issues/317) LIMIT参数不能是Long类型
1. [ISSUE #320](https://github.com/shardingjdbc/sharding-jdbc/issues/320) GROUP BY + LIMIT的SQL改写错误
1. [ISSUE #323](https://github.com/shardingjdbc/sharding-jdbc/issues/323) 解析ORDER BY + 聚合表达式错误

## 1.5.0

### 里程碑

1. 全新的SQL解析模块，去掉对Druid的依赖。仅解析分片上下文，对于SQL采用"半理解"理念，进一步提升性能和兼容性，并降低代码复杂度
1. 全新的SQL改写模块，增加优化性改写模块
1. 全新的SQL归并模块，重构为流式、内存以及装饰者3种归并引擎

### 新功能

1. 增加对Oracle，SQLServer和PostgreSQL的支持
1. 非功能型子查询支持

### 功能提升

1. [ISSUE #256](https://github.com/shardingjdbc/sharding-jdbc/issues/256) 可配置显示分片执行SQL日志
1. [ISSUE #291](https://github.com/shardingjdbc/sharding-jdbc/issues/291) 用流式方式处理仅包含GroupBy的SQL

### 功能调整

1. 简化分布式自增序列。将每个表支持多自增序列简化为单表仅支持单一的分布式自增序列，并不再支持通过环境变量设置workerID。
1. 去掉对OR的支持

### 缺陷修正

1. [ISSUE #239](https://github.com/shardingjdbc/sharding-jdbc/issues/239) LIMIT路由至多查询结果集，若只有一个不为空的结果集，分页结果不正确
1. [ISSUE #263](https://github.com/shardingjdbc/sharding-jdbc/issues/263) 分片列和逻辑表配置可忽略大小写
1. [ISSUE #292](https://github.com/shardingjdbc/sharding-jdbc/issues/292) 内存方式处理GROUP BY语句如有分页信息则需改写
1. [ISSUE #295](https://github.com/shardingjdbc/sharding-jdbc/issues/295) LIMIT 0的情况并未按照分页限制条件过滤结果集

## 1.4.2

### 功能提升

1. [ISSUE #219](https://github.com/shardingjdbc/sharding-jdbc/issues/219) 线程性能优化
1. [ISSUE #215](https://github.com/shardingjdbc/sharding-jdbc/issues/215) 流式排序的聚集结果集 StreamingOrderByReducerResultSet性能优化
1. [ISSUE #161](https://github.com/shardingjdbc/sharding-jdbc/issues/161) 结果集归并的时候可以采用堆排序来提升性能

### 缺陷修正

1. [ISSUE #212](https://github.com/shardingjdbc/sharding-jdbc/issues/212) 对去缺少数据源规则给出更有意义的提示
1. [ISSUE #214](https://github.com/shardingjdbc/sharding-jdbc/issues/214) where中 table_name.column_name in (?,?)无法解析表达式
1. [ISSUE #180](https://github.com/shardingjdbc/sharding-jdbc/issues/180) 批量执行Update返回值不准确
1. [ISSUE #225](https://github.com/shardingjdbc/sharding-jdbc/issues/225) 自动生成Id最后一位不归零

## 1.4.1

### 功能提升

1. [ISSUE #191](https://github.com/shardingjdbc/sharding-jdbc/issues/191) 根据主机的IP生成workerId的KeyGenerator实现
1. [ISSUE #192](https://github.com/shardingjdbc/sharding-jdbc/issues/192) 根据HOSTNAME的数字尾缀获取workerId的KeyGenerator
1. [ISSUE #210](https://github.com/shardingjdbc/sharding-jdbc/issues/210) 路由到单库单表移除补充的SQL语句片段

### 缺陷修正

1. [ISSUE #194](https://github.com/shardingjdbc/sharding-jdbc/issues/194) jdbc接口中资源释放错误
1. [ISSUE #199](https://github.com/shardingjdbc/sharding-jdbc/issues/199) 分表且复用PreparedStatement对象造成数据路由错误
1. [ISSUE #201](https://github.com/shardingjdbc/sharding-jdbc/issues/201) 批量操作执行前事件发送缺失
1. [ISSUE #203](https://github.com/shardingjdbc/sharding-jdbc/issues/203) 合并batch操作发送的事件
1. [ISSUE #209](https://github.com/shardingjdbc/sharding-jdbc/issues/209) limit并行异常

## 1.4.0

### 功能提升

自动生成键实现，包含

1. [ISSUE #162](https://github.com/shardingjdbc/sharding-jdbc/issues/162) 分布式主键算法实现
1. [ISSUE #163](https://github.com/shardingjdbc/sharding-jdbc/issues/163) 获取自增序列jdbc接口实现
1. [ISSUE #171](https://github.com/shardingjdbc/sharding-jdbc/issues/171) sharding-jdbc-core配合自动生成序列改造
1. [ISSUE #172](https://github.com/shardingjdbc/sharding-jdbc/issues/172) YAML与Spring的配置方式增加对于自增序列的支持

### 缺陷修正

1. [ISSUE #176](https://github.com/shardingjdbc/sharding-jdbc/issues/176) AbstractMemoryResultSet对SQL的wasNull实现有问题

## 1.3.3

### 功能提升

1. [ISSUE #59](https://github.com/shardingjdbc/sharding-jdbc/issues/59) PreparedStatement设置参数时可以根据参数类型调用正确的底层set方法

### 缺陷修正

1. [ISSUE #149](https://github.com/shardingjdbc/sharding-jdbc/issues/149) INSERT IGNORE INTO时如果数据重了忽略时返回的成-1了，应该返回0 
1. [ISSUE #118](https://github.com/shardingjdbc/sharding-jdbc/issues/118) 同一个线程内先执行DQL后执行DML，DML操作在从库上执行
1. [ISSUE #122](https://github.com/shardingjdbc/sharding-jdbc/issues/122) bed的fail重试问题
1. [ISSUE #152](https://github.com/shardingjdbc/sharding-jdbc/issues/152) 可能同一个connection多线程导致问题
1. [ISSUE #150](https://github.com/shardingjdbc/sharding-jdbc/issues/150) 与最新SQLServer jdbc驱动兼容问题
1. [ISSUE #166](https://github.com/shardingjdbc/sharding-jdbc/issues/166) druid数据源stat过滤器多线程报错

## 1.3.2

### 功能提升

1. [ISSUE #79](https://github.com/shardingjdbc/sharding-jdbc/issues/79) 关于MySQL 分页limit

### 缺陷修正

1. [ISSUE #36](https://github.com/shardingjdbc/sharding-jdbc/issues/36) ShardingPreparedStatement无法反复设置参数
1. [ISSUE #114](https://github.com/shardingjdbc/sharding-jdbc/issues/114) ShardingPreparedStatement执行批处理任务时,反复解析sql导致oom
1. [ISSUE #33](https://github.com/shardingjdbc/sharding-jdbc/issues/33) Limit支持问题
1. [ISSUE #124](https://github.com/shardingjdbc/sharding-jdbc/issues/124) com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractStatementAdapter.getUpdateCount返回值不符合JDBC规范
1. [ISSUE #141](https://github.com/shardingjdbc/sharding-jdbc/issues/141) 多线程执行器参数设置失效


## 1.3.1

### 功能提升

1. [ISSUE #91](https://github.com/shardingjdbc/sharding-jdbc/issues/91) 开放对Statement.getGeneratedKeys的支持，可返回原生的数据库自增主键
1. [ISSUE #92](https://github.com/shardingjdbc/sharding-jdbc/issues/92) 查询类DQL语句事件发送

### 缺陷修正

1. [ISSUE #89](https://github.com/shardingjdbc/sharding-jdbc/issues/89) 读写分离和分片的hint一起使用导致冲突
1. [ISSUE #95](https://github.com/shardingjdbc/sharding-jdbc/issues/95) 同一线程内写入操作后的读操作均从主库读取改为同一线程且同一连接内

## 1.3.0

### 新功能

1. [ISSUE #85](https://github.com/shardingjdbc/sharding-jdbc/issues/85) 读写分离

### 功能提升

1. [ISSUE #82](https://github.com/shardingjdbc/sharding-jdbc/issues/82) TableRule可传入dataSourceName属性，用于指定该TableRule对应的数据源
1. [ISSUE #88](https://github.com/shardingjdbc/sharding-jdbc/issues/88) 放开对其他数据库的限制，可支持标准SQL, 对个性化分页等语句不支持

### 缺陷修正

1. [ISSUE #81](https://github.com/shardingjdbc/sharding-jdbc/issues/81) 关联表查询使用or查询条件解析结果异常

## 1.2.1

### 结构调整

1. [ISSUE #60](https://github.com/shardingjdbc/sharding-jdbc/issues/60) API调整，抽离ShardingDataSource，使用工厂代替
1. [ISSUE #76](https://github.com/shardingjdbc/sharding-jdbc/issues/76) ShardingRule和TableRule调整为Builder模式
1. [ISSUE #77](https://github.com/shardingjdbc/sharding-jdbc/issues/77) ShardingRule和TableRule调整为Builder模式

### 功能提升

1. [ISSUE #61](https://github.com/shardingjdbc/sharding-jdbc/issues/61) 在ShardingValue类中加入逻辑表名
1. [ISSUE #66](https://github.com/shardingjdbc/sharding-jdbc/issues/66) 在JDBC层的Statement增加对get/set MaxFieldSize，MaxRows和QueryTimeout的支持
1. [ISSUE #72](https://github.com/shardingjdbc/sharding-jdbc/issues/72) 对于select union all形式的批量插入支持
1. [ISSUE #78](https://github.com/shardingjdbc/sharding-jdbc/issues/78) 简化只分库配置，无需配置逻辑表和真实表对应关系
1. [ISSUE #80](https://github.com/shardingjdbc/sharding-jdbc/issues/80) 简化包含不分片库表的配置，可指定默认数据源，不分片无需配置TableRule

### 缺陷修正

1. [ISSUE #63](https://github.com/shardingjdbc/sharding-jdbc/issues/63) ORDER BY与GROUP BY衍生列未添加表名或表别名
1. [ISSUE #65](https://github.com/shardingjdbc/sharding-jdbc/issues/65) 解析条件上下文性能提升
1. [ISSUE #67](https://github.com/shardingjdbc/sharding-jdbc/issues/67) 分片路由到多表时柔性事务日志无法删除
1. [ISSUE #71](https://github.com/shardingjdbc/sharding-jdbc/issues/71) 路由单分片LIMIT的OFFSET计算错误
1. [ISSUE #75](https://github.com/shardingjdbc/sharding-jdbc/issues/75) MemoryTransactionLogStorage重试次数更新并发问题

## 1.2.0

### 新功能

1. [ISSUE #53](https://github.com/shardingjdbc/sharding-jdbc/issues/53) 动态表配置
1. [ISSUE #58](https://github.com/shardingjdbc/sharding-jdbc/issues/58) 柔性事务：最大努力送达型初始版本

### 结构调整

1. [ISSUE #49](https://github.com/shardingjdbc/sharding-jdbc/issues/49) 调整属性配置
1. [ISSUE #51](https://github.com/shardingjdbc/sharding-jdbc/issues/51) 重构Hint接口

### 缺陷修正

1. [ISSUE #43](https://github.com/shardingjdbc/sharding-jdbc/issues/43) yaml文件中包含中文，且操作系统模式不是utf-8编码导致的yaml不能解析
1. [ISSUE #48](https://github.com/shardingjdbc/sharding-jdbc/issues/48) yaml文件读取后未关闭
1. [ISSUE #57](https://github.com/shardingjdbc/sharding-jdbc/issues/57) SQL解析子查询改进

## 1.1.0

### 新功能

1. [ISSUE #40](https://github.com/shardingjdbc/sharding-jdbc/issues/40) 支持YAML文件配置
1. [ISSUE #41](https://github.com/shardingjdbc/sharding-jdbc/issues/41) 支持Spring命名空间配置
1. [ISSUE #42](https://github.com/shardingjdbc/sharding-jdbc/issues/42) 支持inline表达式配置

### 缺陷修正

1. [ISSUE #25](https://github.com/shardingjdbc/sharding-jdbc/issues/25) OR表达式下会出现重复结果问题

## 1.0.1

### 功能提升

1. [ISSUE #39](https://github.com/shardingjdbc/sharding-jdbc/issues/39) 增加使用暗示(Hint)方式注册分片键值的方式进行SQL路由的功能

### 缺陷修正

1. [ISSUE #11](https://github.com/shardingjdbc/sharding-jdbc/issues/11) count函数在某些情况下返回不正确
1. [ISSUE #13](https://github.com/shardingjdbc/sharding-jdbc/issues/13) Insert 语句 没有写列名 进行了全路由
1. [ISSUE #16](https://github.com/shardingjdbc/sharding-jdbc/issues/16) 改造多线程执行模型
1. [ISSUE #18](https://github.com/shardingjdbc/sharding-jdbc/issues/18) 查询Count时，使用getObject()取数会报异常
1. [ISSUE #19](https://github.com/shardingjdbc/sharding-jdbc/issues/19) sum和avg函数，不加别名不执行merger，加了空指针异常
1. [ISSUE #38](https://github.com/shardingjdbc/sharding-jdbc/issues/38) JPA与Sharding-JDBC的兼容问题。JPA会自动增加SELECT的列别名，导致ORDER BY只能通过别名，而非列名称获取ResultSet的数据

## 1.0.0
1. 初始版本
