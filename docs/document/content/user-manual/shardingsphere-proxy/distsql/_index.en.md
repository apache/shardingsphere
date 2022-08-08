+++
title = "DistSQL"
weight = 3
chapter = true
+++

This chapter will introduce the detailed syntax of [DistSQL](/en/overview/distsql/).

## Syntax Rule

In DistSQL statement, except for keywords, the input format of other elements shall conform to the following rules.

### Identifier

1. The identifier represents an object in the SQL statement, including:
- database name
- table name
- column name
- index name
- resource name
- rule name
- algorithm name

1. The allowed characters in the identifier are: [`A-Z, A-Z, 0-9, _`] (letters, numbers, underscores) and should start with a letter.

1. When keywords or special characters appear in the identifier, use the backticks (`).

### Literal

Types of literals include:

- string: enclosed in single quotes (') or double quotes (")
- int: it is generally a positive integer, such as 0-9;
Note: some DistSQL syntax allows negative values. In this case, a negative sign (-) can be added before the number, such as -1.
- boolean, containing only true & false. Case insensitive.