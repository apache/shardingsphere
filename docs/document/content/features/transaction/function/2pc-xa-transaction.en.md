+++
pre = "<b>3.4.2.2 </b>"
toc = true
title = "2PC transaction-XA"
weight = 2
+++

## Function

* Support cross-database XA transactions after sharding.
* Operation atomicity and high data consistency in 2PC transactions.
* When service is down and restarted, commit and rollback transactions can be recovered automatically.
* An SPI mechanism that integrates mainstream XA managers: Atomikos default and Narayana and Bitronix optional.
* Support XA and non-XA connection pool in the same time.
* Provide spring-boot and namespace access.

## Not Support

* Recover committing and rolling back in other machines after the service is down.
