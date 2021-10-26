+++
title = "Core Concept"
weight = 1
+++

## Shadow DB Switch

Shadow library switch.

A stress test is a demand for a specific period of time, and it can be turned on when needed.

## Production DB

The database used for production data.

## Shadow DB

The database used for the test data.

## Shadow Table

Perform pressure test data related tables.

It has the same table structure as the corresponding table in the production database.

## Shadow Algorithm

Provides 2 types of shadow algorithms.

Since the shadow algorithm is closely related to business implementation, no default shadow algorithm is provided.

- Column shadow algorithm
  
It is suitable for scenarios where the value of a field involved in the executed SQL satisfies certain matching conditions in the test.

- Note shadow algorithm

It is suitable for scenarios where the field values involved in executing SQL cannot meet certain matching conditions in the test.

## Default Shadow Algorithm

Default shadow algorithm, optional item. For the default matching algorithm that is not configured with the shadow algorithm table.

**Note**: The default shadow algorithm only supports note shadow algorithm.
