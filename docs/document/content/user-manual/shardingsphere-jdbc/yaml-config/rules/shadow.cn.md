+++
title = "影子库"
weight = 5
+++

## 配置项说明

```yaml
rules:
- !SHADOW
  enable: # Shadow功能开关。 可选值：true/false，默认为false
  dataSources:
    shadowDataSource:
      sourceDataSourceName: # 生产数据源名称
      shadowDataSourceName: # 影子数据源名称 
  tables:
    <table-name>:
      dataSourceNames: # 影子表关联影子数据源名称列表
        - <shadow-data-source> 
      shadowAlgorithmNames: # 影子表关联影子算法名称列表
        - <shadow-algorithm-name>
  defaultShadowAlgorithmName: # 默认影子算法名称
  shadowAlgorithms:
    <shadow-algorithm-name> (+): # 影子算法名称
      type: # 影子算法类型
      props: # 影子算法属性配置
        # ...
```
