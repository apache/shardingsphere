+++
title = "RUL Syntax"
weight = 4
chapter = true
+++

RUL (Resource Utility Language) responsible for SQL parsing, SQL formatting, preview execution plan and more utility functions.

## SQL Utility

| Statement   | Function                                      | Example                       |
|:------------|:----------------------------------------------|:------------------------------|
| PARSE SQL   | Parse SQL and output abstract syntax tree     | PARSE SELECT * FROM t_order   |
| FORMAT SQL  | Parse SQL and output formated SQL statement   | FORMAT SELECT * FROM t_order  |
| PREVIEW SQL | Preview SQL execution plan                    | PREVIEW SELECT * FROM t_order |
