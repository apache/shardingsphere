+++
title = "Change History"
weight = 3
+++

## 5.0.0-alpha

### Config Center

#### Structure in Configuration Center

```yaml
namespace
├──users                                     # Users configuration
├──props                                     # Properties configuration
├──schemas                                   # Schema configuration
├      ├──${schema_1}                        # Schema name 1
├      ├      ├──datasource                  # Datasource configuration
├      ├      ├──rule                        # Rule configuration
├      ├      ├──table                       # Table configuration
├      ├──${schema_2}                        # Schema name 2
├      ├      ├──datasource                  # Datasource configuration
├      ├      ├──rule                        # Rule configuration
├      ├      ├──table                       # Table configuration
```

### Registry Center

#### Data Structure in Registry Center

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
