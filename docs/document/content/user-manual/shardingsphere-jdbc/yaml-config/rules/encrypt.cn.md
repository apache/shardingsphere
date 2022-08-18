+++
title = "数据加密"
weight = 5
+++

## 背景信息

数据加密 YAML 配置方式具有非凡的可读性，通过 YAML 格式，能够快速地理解加密规则之间的依赖关系，ShardingSphere 会根据 YAML 配置，自动完成 ShardingSphereDataSource 对象的创建，减少用户不必要的编码工作。

## 参数解释

```yaml
rules:
- !ENCRYPT
  tables:
    <table-name> (+): # 加密表名称
      columns:
        <column-name> (+): # 加密列名称
          cipherColumn: # 密文列名称
          assistedQueryColumn (?):  # 查询辅助列名称
          plainColumn (?): # 原文列名称
          encryptorName: # 加密算法名称
      queryWithCipherColumn(?): # 该表是否使用加密列进行查询
    
  # 加密算法配置
  encryptors:
    <encrypt-algorithm-name> (+): # 加解密算法名称
      type: # 加解密算法类型
      props: # 加解密算法属性配置
        # ...

  queryWithCipherColumn: # 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/common-config/builtin-algorithm/encrypt)。

## 操作步骤

1. 在 YAML 文件中配置数据加密规则，包含数据源、加密规则、全局属性等配置项；
2. 调用 YamlShardingSphereDataSourceFactory 对象的 createDataSource 方法，根据 YAML 文件中的配置信息创建 ShardingSphereDataSource。

## 配置示例

数据加密 YAML 配置如下：

```yaml
dataSources:
  unique_ds:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          plainColumn: username_plain
          cipherColumn: username
          encryptorName: name-encryptor
        pwd:
          cipherColumn: pwd
          assistedQueryColumn: assisted_query_pwd
          encryptorName: pwd_encryptor
  encryptors:
    name-encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    pwd_encryptor:
      type: assistedTest
```

然后通过 YamlShardingSphereDataSourceFactory 的 createDataSource 方法创建数据源。

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile());
```

## 相关参考

- [核心特性：数据加密](/cn/features/encrypt/)
- [开发者指南：数据加密](/cn/dev-manual/encrypt/)
