+++
title = "Core Concept"
weight = 1
+++

## Shadow DB Switch

Shadow DB switch.

Pressure testing is a requirement for a specific period of time, turned on when needed.

## Production DB

The database used for production data.

## Shadow DB

The Shadow database for Pressure testing data isolation.

## Shadow Table

Pressure testing data related tables.

The shadow table has the same table structure in the production DB and shadow DB.

## Shadow Algorithm

Provides 2 types of shadow algorithms.

Since the shadow algorithm is closely related to business, no default shadow algorithm provided.

- Column shadow algorithm
  
It is suitable for scenarios where the value of a field involved in the executed SQL satisfies certain matching conditions in the testing.

- Note shadow algorithm

It is suitable for scenarios where the field values involved in executing SQL cannot meet certain matching conditions in the testing.

## Default Shadow Algorithm

Default shadow algorithm, optional item. The default matching algorithm for table that is not configured with the shadow algorithm.

**Note**: The default shadow algorithm only supports note shadow algorithm.
