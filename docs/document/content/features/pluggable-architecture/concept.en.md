+++
pre = "<b>3.9.1. </b>"
title = "Core Concept"
weight = 1
chapter = true
+++

## TypedSPI

Type based on SPI which is a mutually exclusive interface. Only one implementation class can take effect in the life cycle of a request.
It is usually used to distinguish database type, transaction type, configuration center type, etc.

## OrderedSPI

Order based on SPI which is an appendable interface. Multiple implementation classes can take effect in the life cycle of a request.
It is usually used with rule configurations.
