+++
pre = "<b>8.1 </b>"
title = "Test Engine"
weight = 1
chapter = true
+++

Apache ShardingSphere provided a full functionality test engine.
They define SQLs in XML files, engine run for each database independently.

All test engines designed to modify the configuration files to execute all assertions without any **Java code** modification.
It does not depend on any third-party environment, ShardingSphere-Proxy and database used for testing are provided by docker image.
