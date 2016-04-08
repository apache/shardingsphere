+++
date = "2016-01-08T16:14:21+08:00"
title = "使用指南"
weight = 2
+++

# 使用指南
阅读本指南前，请先阅读快速起步。本文档使用更复杂的场景进一步介绍`Sharding-JDBC`的分库分表能力。
## 数据库模式
本文档中提供了两个数据源`db0`和`db1`，每个数据源之中包含了两组表`t_order_0`和`t_order_1`，`t_order_item_0`和`t_order_item_1 `。这两组表的建表语句为：
```sql
CREATE TABLE IF NOT EXISTS `t_order_x` (
  `order_id` INT NOT NULL,
  `user_id`  INT NOT NULL,
  PRIMARY KEY (`order_id`)
);
CREATE TABLE IF NOT EXISTS `t_order_item_x` (
  `item_id`  INT NOT NULL,
  `order_id` INT NOT NULL,
  `user_id`  INT NOT NULL,
  PRIMARY KEY (`item_id`)
);
```
## 逻辑表与实际表映射关系
### 均匀分布
数据表在每个数据源内呈现均匀分布的态势
```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_0 
  └── t_order_1
```
表规则可以使用默认的配置
```java
 TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule);
```
### 自定义分布
数据表呈现有特定规则的分布
```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_2
  ├── t_order_3
  └── t_order_4
```
表规则可以指定每张表在数据源中的分布情况
```java
 TableRule orderTableRule = new TableRule("t_order", Arrays.asList("db0.t_order_0", "db0.t_order_1", "db1.t_order_2", "db1.t_order_3", "db1.t_order_4"), dataSourceRule);
```

### 本教程采用的数据分布例子
```
db0
  ├── t_order_0               user_id为偶数   order_id为偶数
  ├── t_order_1               user_id为偶数   order_id为奇数
  ├── t_order_item_0          user_id为偶数   order_id为偶数
  └── t_order_item_1          user_id为偶数   order_id为奇数
db1
  ├── t_order_0               user_id为奇数   order_id为偶数
  ├── t_order_1               user_id为奇数   order_id为奇数
  ├── t_order_item_0          user_id为奇数   order_id为偶数
  └── t_order_item_1          user_id为奇数   order_id为奇数
```

## 逻辑表与实际表
配置分库分表的目的是将原有一张表的数据分散到不同库不同表中，且不改变原有`SQL`语句的情况下来使用这一张表。那么从一张表到多张的映射关系需要使用逻辑表与实际表这两种概念。下面通过一个例子来解释一下。假设在使用`PreparedStatement`访问数据库，`SQL`如下：
```sql
select * from t_order where user_id = ? and order_id = ?;
```
当`user_id=0`且`order=0`时，`Sharding-JDBC`将会将`SQL`语句转换为如下形式：
```sql
select * from db0.t_order_0 where user_id = ? and order_id = ?;
```
其中原始`SQL`中的`t_order`就是 __逻辑表__，而转换后的`db0.t_order_0`就是 __实际表__

## 规则配置
以上分库分表的形式`Sharding-JDBC`是通过规则配置来进行的描述的，下面讲通过几个小节来描述规则的详细配置：
```java
 ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
                Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
```
## 数据源配置
首先我们来构造`DataSourceRule`对象，它是来描述数据源的分布规则的。
```java
 DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
```
这里构造器需要一个入参：数据源名称与真实数据源之间的映射关系，这个关系的构造方法如下
```java
Map<String, DataSource> dataSourceMap = new HashMap<>(2);
dataSourceMap.put("ds_0", createDataSource("ds_0"));
dataSourceMap.put("ds_1", createDataSource("ds_1"));
```
真实的数据源可以使用任意一种数据库连接池，这里使用DBCP来举例
```java
private static DataSource createDataSource(final String dataSourceName) {
    BasicDataSource result = new BasicDataSource();
    result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
    result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
    result.setUsername("root");
    result.setPassword("");
    return result;
}
```

## 策略配置
### 数据源策略与表策略

![策略类图](../../img/StrategyClass.900.png)
Sharding-JDBC认为对于分片策略存有两种维度
- 数据源分片策略`DatabaseShardingStrategy`：数据被分配的目标数据源
- 表分片策略`TableShardingStrategy`：数据被分配的目标表，该目标表存在与该数据的目标数据源内。故表分片策略是依赖与数据源分片策略的结果的
这里注意的是两种策略的API完全相同，以下针对策略API的讲解将适用于这两种策略
### 全局默认策略与特定表策略
策略是作用在特定的表规则上的，数据源策略与表策略与特定表相关
```java
TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"),
                  new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
                  new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()),
                  dataSourceRule);
```

如果分片规则中的所有表或大部分表的分片策略相同，可以使用默认策略来简化配置。一下两种配置是等价的

```java
  //使用了默认策略配置
  TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule);
  TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList("t_order_item_0", "t_order_item_1"), dataSourceRule);
  ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
                Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))),
                new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
                new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
```

```java
  //未使用默认策略配置
  TableRule orderTableRule = new TableRule("t_order", Arrays.asList("t_order_0", "t_order_1"), dataSourceRule,
          new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
          new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
  TableRule orderItemTableRule = new TableRule("t_order_item", Arrays.asList("t_order_item_0", "t_order_item_1"), dataSourceRule,
          new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()),
          new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()));
  ShardingRule shardingRule = new ShardingRule(dataSourceRule, Arrays.asList(orderTableRule, orderItemTableRule),
          Arrays.asList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))));
```

### 分片键
分片键是分片策略的第一个参数。分片键表示的是SQL语句中WHERE中的条件列。分片键可以配置多个

- 单分片策略

```java
new TableShardingStrategy("order_id", new SingleKeyShardingAlgorithm()))
```
- 多分片策略

```java
new TableShardingStrategy(Arrays.asList("order_id", "order_type", "order_date"), new MultiKeyShardingAlgorithm()))
```

### 分片算法
分片算法接口类图关系如下：

![算法](../../img/AlgorithmClass.900.png)

### 绑定表
绑定表代表一组表，这组表的逻辑表与实际表之间的映射关系是相同的。比如`t_order`与`t_order_item`就是这样一组绑定表关系,它们的分库与分表策略是完全相同的,那么可以使用它们的表规则将它们配置成绑定表
```java
new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))
```
那么在进行SQL路由时，如果SQL为
```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?
```
其中`t_order`在FROM的最左侧，Sharding-JDBC将会以它作为整个绑定表的主表。所有路由计算将会只使用主表的策略，那么`t_order_item`表的分片计算将会使用`t_order`的条件。故绑定表之间的分区键要完全相同。

## 分片算法详解

### 单分片键算法与多分片键算法
这两种算法从名字上就可以知道前者是针对只有一个分片键，后者是针对有多个分片键的。单分片键算法是多分片键算法的一种简便形式，所以完全可以使用多分片算法去替代单分片键算法。下面两种形式是等价的
```java
new TableShardingStrategy("order_id", new SingleKeyShardingAlgorithm()))
new TableShardingStrategy(Arrays.asList("order_id"), new MultiKeyShardingAlgorithm()))
```

同时在算法内部，`doSharding`等方法的`shardingValue`入参根据使用算法类型不同而不同
单分片键算法，方法签名
```java 
public String doEqualSharding(final Collection<String> dataSourceNames, final ShardingValue<Integer> shardingValue) 
```
多分片键算法，方法签名
```java 
public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues)
```
### 分片键算法类型
根据数据源策略与表策略、单分片与多分片，这两种组合，一共产生了4种可供实现的分片算法的接口

- 单分片键数据源分片算法`SingleKeyDatabaseShardingAlgorithm`
- 单分片表分片算法`SingleKeyTableShardingAlgorithm`
- 多分片键数据源分片算法`MultipleKeyDatabaseShardingAlgorithm`
- 多分片表分片算法`MultipleKeyTableShardingAlgorithm`

### 单分片键算法
单分片键算法需要实现三个方法，下面以”单分片键数据源分片算法“举例
```java
@Override
public String doEqualSharding(final Collection<String> availableTargetNames, final ShardingValue<Integer> shardingValue)

@Override
public Collection<String> doInSharding(final Collection<String> availableTargetNames, final ShardingValue<Integer> shardingValue)

@Override
public Collection<String> doBetweenSharding(final Collection<String> availableTargetNames, final ShardingValue<Integer> shardingValue)
```

这三种算法作用如下
- `doEqualSharding`在WHERE使用`=`作为条件分片键。算法中使用`shardingValue.getValue()`获取等`=`后的值
- `doInSharding`在WHERE使用`IN`作为条件分片键。算法中使用`shardingValue.getValues()`获取`IN`后的值
- `doBetweenSharding`在WHERE使用`BETWEEN`作为条件分片键。算法中使用`shardingValue.getValueRange()`获取`BETWEEN`后的值

下面是一个余2的算法的例子，当分片键的值除以2余数就是实际表的结尾。注意注释中提供了一些算法生成SQL的结果，参数`tableNames`集合中有两个参数`t_order_0`和`t_order_1`
```java
 public final class ModuloTableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Integer> {
    
    /**
    *  select * from t_order from t_order where order_id = 11 
    *          └── SELECT *  FROM t_order_1 WHERE order_id = 11
    *  select * from t_order from t_order where order_id = 44
    *          └── SELECT *  FROM t_order_0 WHERE order_id = 44
    */
    public String doEqualSharding(final Collection<String> tableNames, final ShardingValue<Integer> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(shardingValue.getValue() % 2 + "")) {
                return each;
            }
        }
        throw new IllegalArgumentException();
    }
    
    /**
    *  select * from t_order from t_order where order_id in (11,44)  
    *          ├── SELECT *  FROM t_order_0 WHERE order_id IN (11,44) 
    *          └── SELECT *  FROM t_order_1 WHERE order_id IN (11,44) 
    *  select * from t_order from t_order where order_id in (11,13,15)  
    *          └── SELECT *  FROM t_order_1 WHERE order_id IN (11,13,15)  
    *  select * from t_order from t_order where order_id in (22,24,26)  
    *          └──SELECT *  FROM t_order_0 WHERE order_id IN (22,24,26) 
    */
    public Collection<String> doInSharding(final Collection<String> tableNames, final ShardingValue<Integer> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        for (Integer value : shardingValue.getValues()) {
            for (String tableName : tableNames) {
                if (tableName.endsWith(value % 2 + "")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
    
    /**
    *  select * from t_order from t_order where order_id between 10 and 20 
    *          ├── SELECT *  FROM t_order_0 WHERE order_id BETWEEN 10 AND 20 
    *          └── SELECT *  FROM t_order_1 WHERE order_id BETWEEN 10 AND 20 
    */
    public Collection<String> doBetweenSharding(final Collection<String> tableNames, final ShardingValue<Integer> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        Range<Integer> range = (Range<Integer>) shardingValue.getValueRange();
        for (Integer i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            for (String each : tableNames) {
                if (each.endsWith(i % 2 + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}
```

### 多分片键算法
多分片键试用于使用场景比较复杂，为了能提供更高的灵活性，故只提供实现一个方法。
```java
@Override
public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues)
```
算法实现的时候根据`shardingValue.getType()`来获取条件是`=`，`IN`或者`BETWEEN`。然后根据业务进行灵活的实现。

如果表的数据分布如下
```
db0
  ├── t_order_00               user_id以a偶数   order_id为偶数
  ├── t_order_01               user_id以a偶数   order_id为奇数
  ├── t_order_10               user_id以b奇数   order_id为偶数
  └── t_order_11               user_id以b奇数   order_id为奇数

```

算法实现如下:
```java
public final class MultipleKeysModuloTableShardingAlgorithm implements MultipleKeysTableShardingAlgorithm {
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues) {
        Set<Integer> orderIdValueSet = getShardingValue(shardingValues, "order_id");
        Set<Integer> userIdValueSet = getShardingValue(shardingValues, "user_id");
    
        List<String> result = new ArrayList<>();
        /*
        userIdValueSet[10,11] + orderIdValueSet[101,102] => valueResult[[10,101],[10,102],[11,101],[11,102]]
         */
        Set<List<Integer>> valueResult = Sets.cartesianProduct(userIdValueSet, orderIdValueSet);
        for (List<Integer> value : valueResult) {
            String suffix = Joiner.on("").join(value.get(0) % 2, value.get(1) % 2);
            for (String tableName : availableTargetNames) {
                if (tableName.endsWith(suffix)) {
                    result.add(tableName);
                }
            }
        
        }
        return result;
    }
    
    private Set<Integer> getShardingValue(final Collection<ShardingValue<?>> shardingValues, final String shardingKey) {
        Set<Integer> valueSet = new HashSet<>();
        ShardingValue<Integer> shardingValue = null;
        for (ShardingValue<?> each : shardingValues) {
            if (each.getColumnName().equals(shardingKey)) {
                shardingValue = (ShardingValue<Integer>) each;
                break;
            }
        }
        if (null == shardingValue) {
            return valueSet;
        }
        switch (shardingValue.getType()) {
            case SINGLE:
                valueSet.add(shardingValue.getValue());
                break;
            case LIST:
                valueSet.addAll(shardingValue.getValues());
                break;
            case RANGE:
                for (Integer i = shardingValue.getValueRange().lowerEndpoint(); i <= shardingValue.getValueRange().upperEndpoint(); i++) {
                    valueSet.add(i);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return valueSet;
    }
}
```

## 构造ShardingDataSource
完成规则配置后，我们就可以得到`ShardingDataSource`
```java
new ShardingDataSource(shardingRule);
```

## 使用ShardingDataSource
通过一个例子来看看如何使用该数据源
```java
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 1001);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                System.out.println(rs.getInt(3));
            }
            rs.close();
        }
```
该数据源与普通数据源完全相同，你可以通过上例的API形式来使用，也可以将其配置在Spring，Hibernate等框架中使用。

> 如果希望不依赖于表中的列传入分片键值，参考：[基于暗示(Hint)的分片键值注册方法](../hint_shardingvalue)
