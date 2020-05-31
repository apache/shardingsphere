+++
pre = "<b>5.1. </b>"
title = "SQL Parser"
weight = 1
chapter = true
+++

## SQLParserConfiguration

| *SPI Name*                    | *Description*                                        |
| ----------------------------- | ----------------------------------------------------- |
| SQLParserConfiguration        | Regulate for SQL parser ANTLR G4 file and AST visitor |

| *Implementation Class*        | *Description*                                         |
| ----------------------------- | ----------------------------------------------------- |
| MySQLParserConfiguration      | Based on MySQL's SQL parser                           |
| PostgreSQLParserConfiguration | Based on PostgreSQL's SQL parser                      |
| SQLServerParserConfiguration  | Based on SQLServer's SQL parser                       |
| OracleParserConfiguration     | Based on Oracle's SQL parser                          |
| SQL92ParserConfiguration      | Based on SQL92's SQL parser                           |

## ParsingHook

| *SPI Name*             | *Description*                                     |
| ---------------------- | ------------------------------------------------- |
| ParsingHook            | Used to trace SQL parse process                   |

| *Implementation Class* | *Description*                                     |
| ---------------------- | ------------------------------------------------- |
| OpenTracingParsingHook | Use OpenTrace protocol to trace SQL parse process |
