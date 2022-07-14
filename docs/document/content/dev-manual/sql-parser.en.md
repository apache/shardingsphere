+++
pre = "<b>6.5. </b>"
title = "SQL Parser"
weight = 5
chapter = true
+++

## SPI Interface

### DatabaseTypedSQLParserFacade

| *SPI Name*                    | *Description*                            |
| ---------------------------- | ----------------------------------- |
| DatabaseTypedSQLParserFacade | Configure the lexical and syntactic parser entry for SQL parsing |

### SQLVisitorFacade

| *SPI Name*                           | *Description*                                  |
| ----------------------------------- | ------------------------------------------ |
| SQLVisitorFacade                    | SQL syntax tree access portal                        |

## Sample

### DatabaseTypedSQLParserFacade

| *Implementation Class* | *Description*            |
| ---------------------- |--------------------------|
| MySQLParserFacade      | SQL parser entry based on MySQL |
| PostgreSQLParserFacade | SQL parser entry based on PostgreSQL|
| SQLServerParserFacade  | SQL parser entry based on SQLServer |
| OracleParserFacade     | SQL parser entry based on Oracle|
| SQL92ParserFacade      | SQL parser entry based on SQL92 |
| OpenGaussParserFacade  | SQL parser entry based on openGauss |

### SQLVisitorFacade

| *SPI Name*                           | *Description*                                  |
| ----------------------------------- | ------------------------------------------ |
| SQLVisitorFacade                    | SQL syntax tree access portal                            |
