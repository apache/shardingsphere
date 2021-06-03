+++
title = "变更历史"
weight = 3
+++

## 5.0.0-alpha

### 配置中心

#### 配置中心数据结构

```yaml
namespace
├──users                                     # 权限配置
├──props                                     # 属性配置
├──schemas                                   # Schema 配置
├      ├──${schema_1}                        # Schema 名称1
├      ├      ├──datasource                  # 数据源配置
├      ├      ├──rule                        # 规则配置
├      ├      ├──table                       # 表结构配置
├      ├──${schema_2}                        # Schema 名称2
├      ├      ├──datasource                  # 数据源配置
├      ├      ├──rule                        # 规则配置
├      ├      ├──table                       # 表结构配置
```

### 注册中心

#### 注册中心数据结构

```yaml
namespace
   ├──states
   ├    ├──proxynodes
   ├    ├     ├──${your_instance_ip_a}@${your_instance_pid_x}@${UUID}
   ├    ├     ├──${your_instance_ip_b}@${your_instance_pid_y}@${UUID}
   ├    ├     ├──....
   ├    ├──datanodes
   ├    ├     ├──${schema_1}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──${schema_2}
   ├    ├     ├      ├──${ds_0}
   ├    ├     ├      ├──${ds_1}
   ├    ├     ├──....
```
