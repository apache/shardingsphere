+++
pre = "<b>5.2. </b>"
title = "SQL Parser"
weight = 2
chapter = true
+++

## DatabaseTypedSQLParserFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.DialectSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/DialectSQLParserFacade.java)

### Definition

Database typed SQL parser facade service definition

### Implementation classes

| *Configuration Type* | *Description*                        | *Fully-qualified class name*                                                                                                                                                                                                                                                                |
|----------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL                | SQL parser entry based on MySQL      | [`org.apache.shardingsphere.sql.parser.engine.mysql.parser.MySQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/engine/mysql/parser/MySQLParserFacade.java)                          |
| PostgreSQL           | SQL parser entry based on PostgreSQL | [`org.apache.shardingsphere.sql.parser.engine.postgresql.parser.PostgreSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/engine/postgresql/parser/PostgreSQLParserFacade.java) |
| openGauss            | SQL parser entry based on openGauss  | [`org.apache.shardingsphere.sql.parser.engine.opengauss.parser.OpenGaussParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/engine/opengauss/parser/OpenGaussParserFacade.java)      |
| Oracle               | SQL parser entry based on Oracle     | [`org.apache.shardingsphere.sql.parser.engine.oracle.parser.OracleParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/engine/oracle/parser/OracleParserFacade.java)                     |
| SQLServer            | SQL parser entry based on SQLServer  | [`org.apache.shardingsphere.sql.parser.engine.sqlserver.parser.SQLServerParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/engine/sqlserver/parser/SQLServerParserFacade.java)      |
| ClickHouse           | SQL parser entry based on ClickHouse | [`org.apache.shardingsphere.sql.parser.engine.clickhouse.parser.ClickHouseParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/clickhouse/src/main/java/org/apache/shardingsphere/sql/parser/engine/clickhouse/parser/ClickHouseParserFacade.java) |
| Doris                | SQL parser entry based on Doris      | [`org.apache.shardingsphere.sql.parser.engine.doris.parser.DorisParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/doris/src/main/java/org/apache/shardingsphere/sql/parser/engine/doris/parser/DorisParserFacade.java)                          |
| Hive                 | SQL parser entry based on Hive       | [`org.apache.shardingsphere.sql.parser.engine.hive.parser.HiveParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/hive/src/main/java/org/apache/shardingsphere/sql/parser/engine/hive/parser/HiveParserFacade.java)                               |
| Presto               | SQL parser entry based on Presto     | [`org.apache.shardingsphere.sql.parser.engine.presto.parser.PrestoParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/presto/src/main/java/org/apache/shardingsphere/sql/parser/engine/presto/parser/PrestoParserFacade.java)                     |
| SQL92                | SQL parser entry based on SQL92      | [`org.apache.shardingsphere.sql.parser.engine.sql92.parser.SQL92ParserFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/engine/sql92/parser/SQL92ParserFacade.java)                          |


## SQLStatementVisitorFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLStatementVisitorFacade.java)

### Definition

SQL visitor facade class definition

### Implementation classes

| *Configuration Type* | *Description*                        | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                          |
|----------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL                | MySQL syntax tree visitor entry      | [`org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.MySQLStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/engine/mysql/visitor/statement/MySQLStatementVisitorFacade.java)                          |
| PostgreSQL           | PostgreSQL syntax tree visitor entry | [`org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.PostgreSQLStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/engine/postgresql/visitor/statement/PostgreSQLStatementVisitorFacade.java) |
| SQLServer            | SQLServer syntax tree visitor entry  | [`org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.SQLServerStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/engine/sqlserver/visitor/statement/SQLServerStatementVisitorFacade.java)      |
| Oracle               | Oracle syntax tree visitor entry     | [`org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.OracleStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/engine/oracle/visitor/statement/OracleStatementVisitorFacade.java)                     |
| openGauss            | openGauss syntax tree visitor entry  | [`org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/engine/opengauss/visitor/statement/OpenGaussStatementVisitorFacade.java)      |
| SQL92                | SQL92 syntax tree visitor entry      | [`org.apache.shardingsphere.sql.parser.engine.sql92.visitor.statement.SQL92StatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/parser/sql/engine/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/engine/sql92/visitor/statement/SQL92StatementVisitorFacade.java)                          |
