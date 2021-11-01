+++
title = "Core Concept"
weight = 1
+++

## Pressure Testing Switcher

Pressure testing is a requirement for a specific period, turned on when needed.

## Production Database

The database used for production data.

## Shadow Database

The database for pressure testing data isolation.

## Shadow Algorithm

Since the shadow algorithm is closely related to business, no default shadow algorithm provided.
There are 2 types of shadow algorithms provided.

- Column based shadow algorithm
  
It is suitable for the column values involved in the executed SQL satisfies certain matching conditions in the testing.

Advantage: Only testing data need to be configured, do not need to modify codes and SQL.

Disadvantage: DML supported only.

- Note based shadow algorithm

It is suitable the column values involved in executing SQL cannot meet certain matching conditions in the testing.
