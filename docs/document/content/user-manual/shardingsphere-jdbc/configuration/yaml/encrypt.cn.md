+++
title = "数据加密"
weight = 3
+++

## 配置项说明

```yaml
dataSource: # 省略数据源配置

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
  
  # 加密算法配置
  encryptors:
    <encrypt-algorithm-name> (+): # 加解密算法名称
      type: # 加解密算法类型
      props: # 加解密算法属性配置
        # ...

props:
  # ...
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。
