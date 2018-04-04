+++
pre = "<b>3. </b>"
title = "读写分离"
weight = 3
prev = "/02-sharding/subquery/"
next = "/03-read-write-split/master-slave/"
chapter = true
+++

## 背景

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。
