+++
title = "分布式事务"
weight = 2
chapter = true
+++

通过 Apache ShardingSphere 使用分布式事务，与本地事务并无区别。
除了透明化分布式事务的使用之外，Apache ShardingSphere 还能够在每次数据库访问时切换分布式事务类型。
支持的事务类型包括 本地事务、XA事务 和 柔性事务。可在创建数据库连接之前设置，缺省为 Apache ShardingSphere 启动时的默认事务类型。
