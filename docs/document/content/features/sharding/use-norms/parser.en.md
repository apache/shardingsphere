+++
title = "Parser"
weight = 3
+++

ShardingSphere supports multiple dialects of SQL using different parsers. For specific SQL dialects that do not implement parsers, the default is to use the SQL92 standard for parsing.

## Specific SQL dialect parser

* PostgreSQL parser
* MySQL parser
* Oracle parser
* SQLServer parser

Note: MySQL parser supports MySQL, H2, and MariDB dialect.

## Default SQL dialect parser

Other SQL dialects, such as SQLite, Sybase, DB2 and Informix, are parsed by default using the standard of SQL92.