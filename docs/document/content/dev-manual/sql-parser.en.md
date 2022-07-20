+++
pre = "<b>6.5. </b>"
title = "SQL Parser"
weight = 5
chapter = true
+++

## DatabaseTypedSQLParserFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.DatabaseTypedSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/DatabaseTypedSQLParserFacade.java)

### Definition

Database typed SQL parser facade service definition

### Implementation classes

| *Configuration Type* | *Description*                       | *Fully-qualified class name* |
| -------------------- | ----------------------------------- | ---------------------------- |
| MySQL                | SQL parser entry based on MySQL     | [`org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/parser/MySQLParserFacade.java) |
| PostgreSQL           | SQL parser entry based on PostgreSQL| [`org.apache.shardingsphere.sql.parser.postgresql.parser.PostgreSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/parser/PostgreSQLParserFacade.java) |
| SQLServer            | SQL parser entry based on SQLServer | [`org.apache.shardingsphere.sql.parser.sqlserver.parser.SQLServerParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/parser/SQLServerParserFacade.java) |
| Oracle               | SQL parser entry based on Oracle    | [`org.apache.shardingsphere.sql.parser.oracle.parser.OracleParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/parser/OracleParserFacade.java) |
| SQL92                | SQL parser entry based on SQL92     | [`org.apache.shardingsphere.sql.parser.sql92.parser.SQL92ParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/parser/SQL92ParserFacade.java) |
| openGauss            | SQL parser entry based on openGauss | [`org.apache.shardingsphere.sql.parser.opengauss.parser.OpenGaussParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/parser/OpenGaussParserFacade.java) |

## SQLVisitorFacade

### Fully-qualified class name

[`org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLVisitorFacade.java)

### Definition

SQL visitor facade class definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
