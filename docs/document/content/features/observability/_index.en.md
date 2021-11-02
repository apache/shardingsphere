+++
pre = "<b>4.9. </b>"
title = "Observability"
weight = 9
chapter = true
+++

## Background

In order to grasp the distributed system status, observe running state of the cluster is a new challenge.
The point-to-point operation mode of logging in to a specific server cannot suite to large number of distributed servers.
Observability and telemetry are the recommended operation way for them.
APM (application performance monitoring) and metrics (statistical indicator monitoring) are important system health indicators.

## Challenges

APM and metrics need to collect system information through event tracking.
Lots of events tracking make kernel code mess, difficult to maintain, and difficult to customize extend.

## Goal

The goal of Apache ShardingSphere observability module is providing as many performance and statistical indicators as possible and isolating kernel code and embedded code.
