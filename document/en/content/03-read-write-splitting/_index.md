+++
pre = "<b>3. </b>"
title = "Read-write splitting"
weight = 3
chapter = true
+++

## Background

In order to share reading/writing pressure , we need to classify data sources into different roles. The data source classified as Master is to provide writing operations, and classified as Slaves are to provide reading operations. A single Master can connect with multiple Slaves.
