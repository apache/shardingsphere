+++
pre = "<b>6.5. </b>"
title = "SQL 解析"
weight = 5
chapter = true
+++

## SPI 接口

### DatabaseTypedSQLParserFacade

| *SPI 名称*                    | *详细说明*                            |
| ---------------------------- | ----------------------------------- |
| DatabaseTypedSQLParserFacade | 配置用于 SQL 解析的词法分析器和语法分析器入口 |

### SQLVisitorFacade

| *SPI 名称*                           | *详细说明*                                  |
| ----------------------------------- | ------------------------------------------ |
| SQLVisitorFacade                    | SQL 语法树访问器入口                          |

## 示例

### DatabaseTypedSQLParserFacade

| *Implementation Class* | *Description*            |
| ---------------------- |--------------------------|
| MySQLParserFacade      | 基于 MySQL 的 SQL 解析器入口     |
| PostgreSQLParserFacade | 基于 PostgreSQL 的 SQL 解析器入口 |
| SQLServerParserFacade  | 基于 SQLServer 的 SQL 解析器入口  |
| OracleParserFacade     | 基于 Oracle 的 SQL 解析器入口     |
| SQL92ParserFacade      | 基于 SQL92 的 SQL 解析器入口      |
| OpenGaussParserFacade  | 基于 openGauss 的 SQL 解析器入口  |

### SQLVisitorFacade

| *SPI 名称*                           | *详细说明*                                  |
| ----------------------------------- | ------------------------------------------ |
| SQLVisitorFacade                    | SQL 语法树访问器入口                          |
