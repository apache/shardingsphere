+++
icon = "<b>3. </b>"
title = "Read-write split"
weight = 0
prev = "/02-sharding/subquery/"
next = "/03-read-write-split/master-slave/"
chapter = true
+++

## Background

In order to share reading/writing pressure , we need to classify data sources into different roles. The data source classified as Master is to provide writing operations, and classified as Slaves are to provide reading operations. A single Master can connect with multiple Slaves.
