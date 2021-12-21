+++
title = "HA"
weight = 3
+++

```yaml
rules:
- !DB_DISCOVERY
  dataSources:
    <data-source-name> (+): # Logic data source name
      dataSourceNames: # Data source names
        - <data-source>
        - <data-source>
      discoveryHeartbeatName: # Detect heartbeat name
      discoveryTypeName: # Highly available type name
  
  # Heartbeat Configuration
  discoveryHeartbeats:
    <discovery-heartbeat-name> (+): # heartbeat name
      props:
        keep-alive-cron: # This is cron expression, such as：'0/5 * * * * ?'
  
  # Highly available Configuration
  discoveryTypes:
    <discovery-type-name> (+): # Highly available type name
      type: # Highly available type, such as: MGR、openGauss
      props (?):
        group-name: 92504d5b-6dec-11e8-91ea-246e9612aaf1 # Required parameters for high-availability types, such as MGR's group-name
```
