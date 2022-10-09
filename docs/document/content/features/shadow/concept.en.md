+++
title = "Core Concept"
weight = 1
+++

## Production Database

Database for production data

## Shadow Database

The Database for stress test data isolation. Configurations should be the same as the Production Database.

## Shadow Algorithm

Shadow Algorithm, which is closely related to business operations, currently has 2 types.

- Column based shadow algorithm
Routing to shadow database by recognizing data from SQL. Suitable for stress test scenario that has an emphasis on data list.
- Hint based shadow algorithm
Routing to shadow database by recognizing comments from SQL. Suitable for stress test driven by the identification of upstream system passage.
