+++
title = "读写分离"
weight = 3
+++

## 背景信息

读写分离 YAML 配置方式可读性高，通过 YAML 格式，能够快速地理解读写分片规则之间的依赖关系，ShardingSphere 会根据 YAML 配置，自动完成 ShardingSphereDataSource 对象的创建，减少用户不必要的编码工作。

## 参数解释

### 读写分离

```yaml
rules:
- !READWRITE_SPLITTING
  dataSourceGroups:
    <data_source_name> (+): # 读写分离逻辑数据源名称，默认使用 Groovy 的行表达式 SPI 实现来解析
       write_data_source_name: # 写库数据源名称，默认使用 Groovy 的行表达式 SPI 实现来解析
       read_data_source_names: # 读库数据源名称，多个从数据源用逗号分隔，默认使用 Groovy 的行表达式 SPI 实现来解析
       transactionalReadQueryStrategy (?): # 事务内读请求的路由策略，可选值：PRIMARY（路由至主库）、FIXED（同一事务内路由至固定数据源）、DYNAMIC（同一事务内路由至非固定数据源）。默认值：PRIMARY，**注意：`FIXED` 和 `DYNAMIC` 需要数据库支持主从强一致同步能力才能使用，例如：openGauss。**
       loadBalancerName: # 负载均衡算法名称
  
  # 负载均衡算法配置
  loadBalancers:
    <load_balancer_name> (+): # 负载均衡算法名称
      type: # 负载均衡算法类型
      props: # 负载均衡算法属性配置
        # ...
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/common-config/builtin-algorithm/load-balance)。

## 操作步骤
1. 添加读写分离数据源
2. 设置负载均衡算法
3. 使用读写分离数据源

## 配置示例
```yaml
rules:
- !READWRITE_SPLITTING
  dataSourceGroups:
    readwrite_ds:
      writeDataSourceName: write_ds
      readDataSourceNames:
        - read_ds_0
        - read_ds_1
      transactionalReadQueryStrategy: PRIMARY
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

## 相关参考

- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [Java API：读写分离](/cn/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
