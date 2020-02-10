+++
pre = "<b>3.2.2. </b>"
toc = true
title = "Core Features"
weight = 2
+++

1. Provide the read-write split configuration of one master database with multiple slave databases, which can be used alone or with sharding table and database.
1. Support SQL pass-through in independent use of read-write split.
1. If there is write operation in the same thread and database connection, all the following read operations are from the master database to ensure data consistency.
1. Forcible master database route based on SQL Hint.
