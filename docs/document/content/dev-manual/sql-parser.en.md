+++
pre = "<b>5.5. </b>"
title = "SQL Parser"
weight = 5
chapter = true
+++

## DatabaseTypedSQLParserFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.DatabaseTypedSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/DatabaseTypedSQLParserFacade.java)

### Definition

Database typed SQL parser facade service definition

### Implementation classes

| *Configuration Type* | *Description*                       | *Fully-qualified class name* |
| -------------------- | ----------------------------------- | ---------------------------- |
| MySQL                | SQL parser entry based on MySQL     | [`org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/parser/MySQLParserFacade.java) |
| PostgreSQL           | SQL parser entry based on PostgreSQL| [`org.apache.shardingsphere.sql.parser.postgresql.parser.PostgreSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/parser/PostgreSQLParserFacade.java) |
| SQLServer            | SQL parser entry based on SQLServer | [`org.apache.shardingsphere.sql.parser.sqlserver.parser.SQLServerParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/parser/SQLServerParserFacade.java) |
| Oracle               | SQL parser entry based on Oracle    | [`org.apache.shardingsphere.sql.parser.oracle.parser.OracleParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/parser/OracleParserFacade.java) |
| SQL92                | SQL parser entry based on SQL92     | [`org.apache.shardingsphere.sql.parser.sql92.parser.SQL92ParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/parser/SQL92ParserFacade.java) |
| openGauss            | SQL parser entry based on openGauss | [`org.apache.shardingsphere.sql.parser.opengauss.parser.OpenGaussParserFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/parser/OpenGaussParserFacade.java) |

## SQLVisitorFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLVisitorFacade.java)

### Definition

SQL visitor facade class definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| MySQL                  | MySQL syntax tree visitor entry         | [`org.apache.shardingsphere.sql.parser.mysql.visitor.statement.facade.MySQLStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/visitor/statement/facade/MySQLStatementSQLVisitorFacade.java) |
| PostgreSQL             | PostgreSQL syntax tree visitor entry    | [`org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.facade.PostgreSQLStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/visitor/statement/facade/PostgreSQLStatementSQLVisitorFacade.java) |
| SQLServer              | SQLServer syntax tree visitor entry     | [`org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.facade.SQLServerStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/visitor/statement/facade/SQLServerStatementSQLVisitorFacade.java) |
| Oracle                 | Oracle syntax tree visitor entry        | [`org.apache.shardingsphere.sql.parser.oracle.visitor.statement.facade.OracleStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/visitor/statement/facade/OracleStatementSQLVisitorFacade.java) |
| SQL92                  | SQL92 syntax tree visitor entry         | [`org.apache.shardingsphere.sql.parser.sql92.visitor.statement.facade.SQL92StatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/visitor/statement/facade/SQL92StatementSQLVisitorFacade.java) |
| openGauss              | openGauss syntax tree visitor entry     | [`org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.facade.OpenGaussStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/sql-parser/dialect/opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/visitor/statement/facade/OpenGaussStatementSQLVisitorFacade.java) |
