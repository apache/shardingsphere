+++
pre = "<b>3.4.2. </b>"
toc = true
title = "2PC Transaction"
weight = 2
+++

## Function

* Fully support cross-database transactions.

* Use Atomikos by default; support to use SPI to upload other XA transaction managers.

## Supported Situation

* Sharding-JDBC can support users' own configurations of XA data source.

* Sharding-Proxy support.
