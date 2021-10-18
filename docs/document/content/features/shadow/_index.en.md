+++
pre = "<b>3.7. </b>"
title = "Shadow DB"
weight = 7
chapter = true
+++

## Background

Under the distributed application architecture based on microservices, since the overall service completes business requirements through a series of microservice calls and middleware calls, the stress testing of a single service can no longer represent the real scenario.

In the offline environment, if you rebuild a complete set of stress testing environment similar to the production environment, the cost is too high, and it is often impossible to simulate the volume and complexity of the online environment.

In this scenario, the industry usually chooses the full-link stress test method, that is, the stress test is performed in the production environment, so that the test results obtained can more accurately reflect the real capacity level and performance of the system.

## Challenges

Full-link stress testing is a complex and huge task that requires corresponding adjustments and cooperation between various middleware and microservices to deal with the transparent transmission of different flows and stress test identifiers. 
Usually there should be a complete set of stress test platform and test plan.

Among them, at the database level, in order to ensure the reliability and integrity of the production data and do a good job of data isolation, it is necessary to put pressure test data requests into the shadow database to prevent the pressure test data from being written into the production database and causing pollution to the real data.

This requires business applications to perform data classification based on the transparently transmitted pressure test identification before executing SQL, and route the corresponding SQL to the corresponding data source.

## Goal

Apache ShardingSphere focuses on database-level solutions in full-link stress testing scenarios.

Kernel-based SQL analysis capabilities and a pluggable platform architecture realize the isolation of pressure test data and production data, help application automatic routing, and support full-link pressure test.

It is the main design goal of the Apache ShardingSphere shadow database module.
