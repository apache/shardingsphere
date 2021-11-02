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

The shadow algorithms are closely related to business, there are 2 types of shadow algorithms provided.

- Column based shadow algorithm

Recognize data from SQL and route to shadow databases.
Suitable for test data driven scenario.

- Note based shadow algorithm

Recognize comment from SQL and route to shadow databases.
Suitable for identify passed by upstream system scenario.
