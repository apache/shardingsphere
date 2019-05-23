+++
pre = "<b>3.4.3.2 </b>"
toc = true
title = "BASE Transaction"
weight = 3

+++

## Design

![柔性事务设计](https://shardingsphere.apache.org/document/current/img/transaction/transaction-base-design_cn.png)

The BASE transaction in ShardingSphere requires to implement Sharding transaction manager SPI to take charge of its life cycle. Also, through SQL Hook in ShardingSphere, BASE transaction will also acquire necessary SQL information to help the transaction manager control distributed transactions. The transaction isolation manager is still planning, so resource isolation is not available now.