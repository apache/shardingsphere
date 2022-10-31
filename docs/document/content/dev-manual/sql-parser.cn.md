+++
pre = "<b>5.5. </b>"
title = "SQL 解析"
weight = 5
chapter = true
+++

## DatabaseTypedSQLParserFacade

### 全限定类名

[`org.apache.shardingsphere.sql.parser.spi.DatabaseTypedSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/DatabaseTypedSQLParserFacade.java)

### 定义

配置用于 SQL 解析的词法分析器和语法分析器入口

### 已知实现

| *配置标识*              | *详细说明*                | *全限定类名*                                               |
| ---------------------- | ------------------------ | ---------------------------------------------------------- |
| MySQL                  | 基于 MySQL 的 SQL 解析器入口     | [`org.apache.shardingsphere.sql.parser.mysql.parser.MySQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/parser/MySQLParserFacade.java) |
| PostgreSQL             | 基于 PostgreSQL 的 SQL 解析器入口 | [`org.apache.shardingsphere.sql.parser.postgresql.parser.PostgreSQLParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/parser/PostgreSQLParserFacade.java) |
| SQLServer              | 基于 SQLServer 的 SQL 解析器入口  | [`org.apache.shardingsphere.sql.parser.sqlserver.parser.SQLServerParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/parser/SQLServerParserFacade.java) |
| Oracle                 | 基于 Oracle 的 SQL 解析器入口     | [`org.apache.shardingsphere.sql.parser.oracle.parser.OracleParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/parser/OracleParserFacade.java) |
| SQL92                  | 基于 SQL92 的 SQL 解析器入口      | [`org.apache.shardingsphere.sql.parser.sql92.parser.SQL92ParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/parser/SQL92ParserFacade.java) |
| openGauss              | 基于 openGauss 的 SQL 解析器入口  | [`org.apache.shardingsphere.sql.parser.opengauss.parser.OpenGaussParserFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/parser/OpenGaussParserFacade.java) |

## SQLVisitorFacade

### 全限定类名

[`org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-spi/src/main/java/org/apache/shardingsphere/sql/parser/spi/SQLVisitorFacade.java)

### 定义

SQL 语法树访问器入口

### 已知实现

| *配置标识*              | *详细说明*                | *全限定类名*                                               |
| ---------------------- | ------------------------ | ---------------------------------------------------------- |
| MySQL                  | 基于 MySQL 的 SQL 语法树访问器入口     | [`org.apache.shardingsphere.sql.parser.mysql.visitor.statement.facade.MySQLStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/java/org/apache/shardingsphere/sql/parser/mysql/visitor/statement/facade/MySQLStatementSQLVisitorFacade.java) |
| PostgreSQL             | 基于 PostgreSQL 的 SQL 语法树访问器入口 | [`org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.facade.PostgreSQLStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/java/org/apache/shardingsphere/sql/parser/postgresql/visitor/statement/facade/PostgreSQLStatementSQLVisitorFacade.java) |
| SQLServer              | 基于 SQLServer 的 SQL 语法树访问器入口  | [`org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.facade.SQLServerStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/java/org/apache/shardingsphere/sql/parser/sqlserver/visitor/statement/facade/SQLServerStatementSQLVisitorFacade.java) |
| Oracle                 | 基于 Oracle 的 SQL 语法树访问器入口     | [`org.apache.shardingsphere.sql.parser.oracle.visitor.statement.facade.OracleStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/java/org/apache/shardingsphere/sql/parser/oracle/visitor/statement/facade/OracleStatementSQLVisitorFacade.java) |
| SQL92                  | 基于 SQL92 的 SQL 语法树访问器入口      | [`org.apache.shardingsphere.sql.parser.sql92.visitor.statement.facade.SQL92StatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/java/org/apache/shardingsphere/sql/parser/sql92/visitor/statement/facade/SQL92StatementSQLVisitorFacade.java) |
| openGauss              | 基于 openGauss 的 SQL 语法树访问器入口  | [`org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.facade.OpenGaussStatementSQLVisitorFacade`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-opengauss/src/main/java/org/apache/shardingsphere/sql/parser/opengauss/visitor/statement/facade/OpenGaussStatementSQLVisitorFacade.java) |
