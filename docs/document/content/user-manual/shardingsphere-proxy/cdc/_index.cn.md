+++
title = "CDC"
weight = 9
+++

CDC（Change Data Capture）增量数据捕捉。CDC 可以监控 ShardingSphere-Proxy 的存储节点中的数据变化，捕捉到数据操作事件，过滤并提取有用信息，最终将这些变化数据发送到指定的目标上。

CDC 可以用于数据同步，数据备份和恢复等方面，目前支持 openGauss、MySQL 和 PostgreSQL。
