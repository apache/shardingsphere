+++
title = "MySQL"
weight = 1
+++

The unsupported SQL list for MySQL are as follows:

| SQL                                                          |
| ------------------------------------------------------------ |
| FLUSH PRIVILEGES                                             |
| CLONE LOCAL DATA DIRECTORY = 'clone_dir'                     |
| INSTALL COMPONENT 'file://component1', 'file://component2'   |
| UNINSTALL COMPONENT 'file://component1', 'file://component2' |
| SHOW CREATE USER user                                        |
| REPAIR TABLE t_order                                         |
| OPTIMIZE TABLE t_order                                       |
| CHECKSUM TABLE t_order                                       |
| CHECK TABLE t_order                                          |
| SET RESOURCE GROUP group_name                                |
| DROP RESOURCE GROUP group_name                               |
| CREATE RESOURCE GROUP group_name TYPE = SYSTEM               |
| ALTER RESOURCE GROUP rg1 VCPU = 0-63                         |
