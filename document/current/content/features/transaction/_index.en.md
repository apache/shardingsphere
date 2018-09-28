+++
pre = "<b>3.4. </b>"
title = "Transaction"
weight = 4
chapter = true
+++

## Background

For distributed databases, performance of ACID transaction is a big problem. Use BASE transaction is a better solution for performance and data integrated balance.
Sharding-Sphere deprecated and remove Best-Effort-Delivery transaction, will instead of with saga in future.

If user do not want use BASE transaction, Sharding-Sphere also can support weak XA.
