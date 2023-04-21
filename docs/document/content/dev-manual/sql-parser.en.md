+++
pre = "<b>5.5. </b>"
title = "SQL Parser"
weight = 5
chapter = true
+++

## DatabaseTypedSQLParserFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.SQLDialectParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLDialectParserFacade.java)

### Definition

Database typed SQL parser facade service definition

### Implementation classes

| *Configuration Type* | *Description*                        | *Fully-qualified class name*                                                                                                                                                                                                                                           |
|----------------------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL                | SQL parser entry based on MySQL      | [`org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/parser/MySQLParserFacade.java)                          |
| PostgreSQL           | SQL parser entry based on PostgreSQL | [`org.apache.shardingsphere.sql.parser.postgresql.parser.PostgreSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/parser/PostgreSQLParserFacade.java) |
| SQLServer            | SQL parser entry based on SQLServer  | [`org.apache.shardingsphere.sql.parser.sqlserver.parser.SQLServerParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/parser/SQLServerParserFacade.java)      |
| Oracle               | SQL parser entry based on Oracle     | [`org.apache.shardingsphere.sql.parser.oracle.parser.OracleParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/parser/OracleParserFacade.java)                     |
| SQL92                | SQL parser entry based on SQL92      | [`org.apache.shardingsphere.sql.parser.sql92.parser.SQL92ParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/parser/SQL92ParserFacade.java)                          |
| openGauss            | SQL parser entry based on openGauss  | [`org.apache.shardingsphere.sql.parser.opengauss.parser.OpenGaussParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/parser/OpenGaussParserFacade.java)      |

## SQLStatementVisitorFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLStatementVisitorFacade.java)

### Definition

SQL visitor facade class definition

### Implementation classes

| *Configuration Type* | *Description*                        | *Fully-qualified class name*                                                                                                                                                                                                                                                                                       |
|----------------------|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MySQL                | MySQL syntax tree visitor entry      | [`org.apache.shardingsphere.sql.parser.mysql.visitor.statement.facade.MySQLSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/visitor/statement/facade/MySQLSQLVisitorFacade.java)                          |
| PostgreSQL           | PostgreSQL syntax tree visitor entry | [`org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.facade.PostgreSQLSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/visitor/statement/facade/PostgreSQLSQLVisitorFacade.java) |
| SQLServer            | SQLServer syntax tree visitor entry  | [`org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.facade.SQLServerSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/visitor/statement/facade/SQLServerSQLVisitorFacade.java)      |
| Oracle               | Oracle syntax tree visitor entry     | [`org.apache.shardingsphere.sql.parser.oracle.visitor.statement.facade.OracleSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/visitor/statement/facade/OracleSQLVisitorFacade.java)                     |
| SQL92                | SQL92 syntax tree visitor entry      | [`org.apache.shardingsphere.sql.parser.sql92.visitor.statement.facade.SQL92SQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/visitor/statement/facade/SQL92SQLVisitorFacade.java)                          |
| openGauss            | openGauss syntax tree visitor entry  | [`org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.facade.OpenGaussSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/visitor/statement/facade/OpenGaussSQLVisitorFacade.java)      |
