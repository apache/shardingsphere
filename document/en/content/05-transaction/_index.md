+++
pre = "<b>5. </b>"
title = "Transaction"
weight = 5
chapter = true
+++

## Background

Considering the performance, sharding-jdbc decides not to support strong consistency distributed transactions. In the future, it will support the B.A.S.E transaction which makes the final result of all the distributed databases consistent. Currently, in addition to supporting weak XA transactions, we have been able to provide the Best-Effort-Delivery transaction, one of the B.A.S.E transaction.
