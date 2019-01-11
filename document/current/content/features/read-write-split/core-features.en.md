+++
pre = "<b>3.2.2. </b>"
toc = true
title = "Core Features"
weight = 2
+++

1. Provide the read-write split configuration of one master database with multiple slave databases, which can be used alone as well as with sharding table and database.
1. SQL pass-through is available when using read-write split alone.
1. If there is write operation in the same thread and database connection, all the following read operations must be from the master database to ensure data consistency.
1. Mandatory master database route strategy based on SQL Hint.
