+++
pre = "<b>3.2.3. </b>"
title = "Unsupported Items"
weight = 3
+++

1. Data replication between the master and the slave database.
1. Data inconsistency caused by replication delay between databases.
1. Double or multiple master databases to provide write operation.
1. The data for transaction across Master and Slave nodes are inconsitent. In the Master-Savle replication model, the master nodes need to be used for both reading and writing in the transaction.