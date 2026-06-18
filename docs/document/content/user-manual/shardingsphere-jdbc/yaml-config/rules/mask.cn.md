+++
title = "数据脱敏"
weight = 6
+++

## 背景信息

数据脱敏 YAML 配置方式良好的可读性，通过 YAML 格式，能够快速地理解脱敏规则之间的依赖关系，ShardingSphere 会根据 YAML 配置，自动完成 ShardingSphereDataSource 对象的创建，减少用户不必要的编码工作。

## 参数解释

```yaml
rules:
- !MASK
  tables:
    <table_name> (+): # 脱敏表名称
      columns:
        <column_name> (+): # 脱敏列名称
          maskAlgorithm: # 脱敏算法

  # 脱敏算法配置
  maskAlgorithms:
    <mask_algorithm_name> (+): # 脱敏算法名称
      type: # 脱敏算法类型
      props: # 脱敏算法属性配置
      # ...
```

算法类型的详情，请参见[内置脱敏算法列表](/cn/user-manual/common-config/builtin-algorithm/mask)。

## 操作步骤

1. 在 YAML 文件中配置数据脱敏规则，包含数据源、脱敏规则、全局属性等配置项；
2. 调用 YamlShardingSphereDataSourceFactory 对象的 createDataSource 方法，根据 YAML 文件中的配置信息创建 ShardingSphereDataSource。

## 配置示例

数据脱敏 YAML 配置如下：

```yaml
dataSources:
  unique_ds:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !MASK
  tables:
    t_user:
      columns:
        password:
          maskAlgorithm: md5_mask
        email:
          maskAlgorithm: mask_before_special_chars_mask
        telephone:
          maskAlgorithm: keep_first_n_last_m_mask

  maskAlgorithms:
    md5_mask:
      type: MD5
    mask_before_special_chars_mask:
      type: MASK_BEFORE_SPECIAL_CHARS
      props:
        special-chars: '@'
        replace-char: '*'
    keep_first_n_last_m_mask:
      type: KEEP_FIRST_N_LAST_M
      props:
        first-n: 3
        last-m: 4
        replace-char: '*'
```

然后通过 YamlShardingSphereDataSourceFactory 的 createDataSource 方法创建数据源。

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile());
```

## 相关参考

- [核心特性：数据脱敏](/cn/features/mask/)
- [开发者指南：数据脱敏](/cn/dev-manual/mask/)
