+++
pre = "<b>3.9. </b>"
title = "Pluggable Architecture"
weight = 9
chapter = true
+++

## Background

In Apache ShardingSphere, many functionality implementations are uploaded through [SPI (Service Provider Interface)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html),
which is a kind of API for the third party to implement or expand, and can be applied in framework expansion or component replacement.

## Challenges

Pluggable architecture is very difficult to design for the project architecture. 
It needs to make each module decouple to independent and imperceptible to each other totally, and enables appendable functions in a way of superposition through a pluggable kernel.
Design an architecture to completely isolate each function, not only can stimulate the enthusiasm of the open source community, but also can guarantee the quality of the project.

Apache ShardingSphere begin to focus on pluggable architecture from version 5.x, features can be embedded into project flexibility.
Currently, the features such as data sharding, primary-replica replication, consensus replication, data encrypt, shadow test, and SQL dialects / database protocols such as MySQL, PostgreSQL, SQLServer, Oracle supported are all weaved by plugins.
There are lots of SPI extensions for Apache ShardingSphere now and increase continuously.

## Goal

**It is the design goal of Apache shardingsphere pluggable architecture to enable developers to customize their own unique systems just like building blocks.**
