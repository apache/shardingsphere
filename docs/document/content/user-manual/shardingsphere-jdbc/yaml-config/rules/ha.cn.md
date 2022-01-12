+++
title = "高可用"
weight = 3
+++

## 配置项说明

```yaml
rules:
- !DB_DISCOVERY
  dataSources:
    <data-source-name> (+): # 逻辑数据源名称
      dataSourceNames: # 数据源名称列表
        - <data-source>
        - <data-source>
      discoveryHeartbeatName: # 检测心跳名称
      discoveryTypeName: # 数据库发现类型名称
  
  # 心跳检测配置
  discoveryHeartbeats:
    <discovery-heartbeat-name> (+): # 心跳名称
      props:
        keep-alive-cron: # cron 表达式，如：'0/5 * * * * ?'
  
  # 数据库发现类型配置
  discoveryTypes:
    <discovery-type-name> (+): # 数据库发现类型名称
      type: # 数据库发现类型，如： MGR、openGauss 
      props (?):
        group-name: 92504d5b-6dec-11e8-91ea-246e9612aaf1 # 数据库发现类型必要参数，如 MGR 的 group-name
```
