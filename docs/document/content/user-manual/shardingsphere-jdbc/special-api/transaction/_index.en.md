+++
title = "Transaction"
weight = 3
chapter = true
+++

Using distributed transactions through Apache ShardingSphere is no different from using local transactions.
Apache ShardingSphere supports LOCAL, XA, and BASE transactions. The transaction type is determined by `defaultType` in the transaction rule.
Once a transaction begins, its type remains unchanged until it is committed or rolled back.
