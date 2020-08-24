+++
pre = "<b>5.1. </b>"
title = "SQL 解析"
weight = 1
chapter = true
+++

## SQLParserConfiguration

| *SPI 名称*                    | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| SQLParserConfiguration        | 用于规定用于解析 SQL 的 ANTLR 语法文件及其语法树访问器 |

| *已知实现类*                   | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| MySQLParserConfiguration      | 基于 MySQL 的SQL 解析器实现                        |
| PostgreSQLParserConfiguration | 基于 PostgreSQL 的SQL 解析器实现                   |
| SQLServerParserConfiguration  | 基于 SQLServer 的SQL 解析器实现                    |
| OracleParserConfiguration     | 基于 Oracle 的SQL 解析器实现                       |
| SQL92ParserConfiguration      | 基于 SQL92 的SQL 解析器实现                        |

## ParsingHook

| *SPI 名称*             | *详细说明*                            |
| ---------------------- | ------------------------------------ |
| ParsingHook            | 用于SQL 解析过程追踪                   |

| *已知实现类*            | *详细说明*                            |
| ---------------------- | ------------------------------------ |
| OpenTracingParsingHook | 使用 OpenTracing 协议追踪 SQL 解析过程 |
