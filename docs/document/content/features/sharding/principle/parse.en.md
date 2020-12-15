+++
title = "Parse Engine"
weight = 1
+++

Compared to other programming languages, SQL is relatively simple, but it is still a complete set of programming language, so there is no essential difference between parsing SQL grammar and parsing other languages (Java, C and Go, etc.).

## Abstract Syntax Tree

The parsing process can be divided into lexical parsing and syntactic parsing. Lexical parser is used to divide SQL into indivisible atomic signs, i.e., Token. According to the dictionary provided by different database dialect, it is categorized into keyword, expression, literal value and operator. SQL is then converted into abstract syntax tree by syntactic parser.

For example, the following SQL:

```sql
SELECT id, name FROM t_user WHERE status = 'ACTIVE' AND age > 18
```

Its parsing AST (Abstract Syntax Tree) is this:

![SQL AST](https://shardingsphere.apache.org/document/current/img/sharding/sql_ast.png)

To better understand, the Token of keywords in abstract syntax tree is shown in green; that of variables is shown in red; what’s to be further divided is shown in grey.

At last, through traversing the abstract syntax tree, the context needed by sharding is extracted and the place that may need to be rewritten is also marked out. Parsing context for the use of sharding includes select items, table information, sharding conditions, auto-increment primary key information, Order By information, Group By information, and pagination information (Limit, Rownum and Top). One-time SQL parsing process is irreversible, each Token is parsed according to the original order of SQL in a high performance. Considering similarities and differences between SQL of all kinds of database dialect, SQL dialect dictionaries of different types of databases are provided in the parsing module.

## SQL Parser

### History

As the core of database sharding and table sharding, SQL parser takes the performance and compatibility as its most important index. ShardingSphere SQL parser has undergone the upgrade and iteration of 3 generations of products.

To pursue good performance and quick achievement, the first generation of SQL parser uses `Druid` before 1.4.x version. As tested in practice, its performance exceeds other parsers a lot.

The second generation of SQL parsing engine begins from 1.5.x version, ShardingSphere has adopted fully self-developed parsing engine ever since. Due to different purposes, ShardingSphere does not need to transform SQL into a totally abstract syntax tree or traverse twice through visitor. Using `half parsing` method, it only extracts the context required by data sharding, so the performance and compatibility of SQL parsing is further improved.

The third generation of SQL parsing engine begins from 3.0.x version. ShardingSphere tries to adopts ANTLR as a generator for the SQL parsing engine, and uses Visit to obtain SQL Statement from AST. Starting from version 5.0.x, the architecture of the parsing engine has been refactored. At the same time, it is convenient to directly obtain the parsing results of the same SQL to improve parsing efficiency by putting the AST obtained from the first parsing into the cache. Therefore, we recommend that users adopt `PreparedStatement` this SQL pre-compilation method to improve performance. Currently, users can also use ShardingSphere's SQL parsing engine independently to obtain AST and SQL Statements for a variety of mainstream relational databases. In the future, the SQL parsing engine will continue to provide powerful functions such as SQL formatting and SQL templating.

### Features

* Independent SQL parsing engine
* The syntax rules can be easily expanded and modified (using `ANTLR`)
* Support multiple dialects

| DB    | Status |
|----------|--------|
|MySQL     |supported|
|PostgreSQL|supported|
|SQLServer |supported|
|Oracle    |supported|
|SQL92     |supported|

* SQL format (developing)
* SQL parameterize (developing)

### API Usage

Maven config
```
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-parser-engine</artifactId>
    <version>${project.version}</version>
</dependency>
// According to the needs, introduce the parsing module of the specified dialect (take MySQL as an example), you can add all the supported dialects, or just what you need
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-parser-mysql</artifactId>
    <version>${project.version}</version>
</dependency>
```

demo:

- Get AST

```
/**
 * databaseType type:String values: MySQL,Oracle，PostgreSQL，SQL92，SQLServer
 * sql type:String SQL to be parsed
 * useCache type:boolean whether use cache
 * @return parse tree
 */
ParseTree tree = new SQLParserEngine(databaseType).parse(sql, useCache)
```

- GET SQLStatement

```
/**
 * databaseType type:String values: MySQL,Oracle，PostgreSQL，SQL92，SQLServer
 * useCache type:boolean whether use cache
 * @return SQLStatement
 */
ParseTree tree = new SQLParserEngine(databaseType).parse(sql, useCache); 
SQLVisitorEngine sqlVisitorEngine = new SQLVisitorEngine(databaseType, "STATEMENT");
SQLStatement sqlStatement = sqlVisitorEngine.visit(tree);
```

- SQL Format

```
/**
 * databaseType type:String values MySQL
 * useCache type:boolean whether use cache
 * @return String 
 */
ParseTree tree = new SQLParserEngine(databaseType).parse(sql, useCache); 
SQLVisitorEngine sqlVisitorEngine = new SQLVisitorEngine(databaseType, "FORMAT");
String formatedSql = sqlVisitorEngine.visit(tree);
```

example：

| sql      | formatedSql |
|----------|-------------|
|select a+1 as b, name n from table1 join table2 where id=1 and name='lu';    |SELECT a + 1 AS b, name n<br>FROM table1 JOIN table2<br>WHERE<br>&emsp;&emsp;&emsp;&emsp;id = 1<br>&emsp;&emsp;&emsp;&emsp;and name = 'lu';|
|select id, name, age, sex, ss, yy from table1 where id=1;|SELECT id , name , age , <br>&emsp;&emsp;&emsp;&emsp;sex , ss , yy <br>FROM table1<br>WHERE <br>&emsp;&emsp;&emsp;&emsp;id = 1;|
|select id, name, age, count(*) as n, (select id, name, age, sex from table2 where id=2) as sid, yyyy from table1 where id=1;|SELECT id , name , age , <br>&emsp;&emsp;&emsp;&emsp;COUNT(*) AS n, <br>&emsp;&emsp;&emsp;&emsp;(<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;SELECT id , name , age , <br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;sex <br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;FROM table2<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;WHERE <br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;id = 2<br>&emsp;&emsp;&emsp;&emsp;) AS sid, yyyy <br>FROM table1<br>WHERE <br>&emsp;&emsp;&emsp;&emsp;id = 1;|
|select id, name, age, sex, ss, yy from table1 where id=1 and name=1 and a=1 and b=2 and c=4 and d=3;|SELECT id , name , age , <br>&emsp;&emsp;&emsp;&emsp;sex , ss , yy <br>FROM table1<br>WHERE <br>&emsp;&emsp;&emsp;&emsp;id = 1<br>&emsp;&emsp;&emsp;&emsp;and name = 1<br>&emsp;&emsp;&emsp;&emsp;and a = 1<br>&emsp;&emsp;&emsp;&emsp;and b = 2<br>&emsp;&emsp;&emsp;&emsp;and c = 4<br>&emsp;&emsp;&emsp;&emsp;and d = 3;|
|ALTER TABLE t_order ADD column4 DATE, ADD column5 DATETIME, engine ss max_rows 10,min_rows 2, <br>ADD column6 TIMESTAMP, ADD column7 TIME;|ALTER TABLE t_order<br>&emsp;&emsp;&emsp;&emsp;ADD column4 DATE,<br>&emsp;&emsp;&emsp;&emsp;ADD column5 DATETIME,<br>&emsp;&emsp;&emsp;&emsp;ENGINE ss<br>&emsp;&emsp;&emsp;&emsp;MAX_ROWS 10,<br>&emsp;&emsp;&emsp;&emsp;MIN_ROWS 2,<br>&emsp;&emsp;&emsp;&emsp;ADD column6 TIMESTAMP,<br>&emsp;&emsp;&emsp;&emsp;ADD column7 TIME|
|CREATE TABLE IF NOT EXISTS <br>`runoob_tbl`(`runoob_id` INT UNSIGNED AUTO_INCREMENT,`runoob_title` VARCHAR(100) NOT NULL,<br>`runoob_author` VARCHAR(40) NOT NULL,`runoob_test` NATIONAL CHAR(40),<br>`submission_date` DATE,PRIMARY KEY (`runoob_id`))ENGINE=InnoDB DEFAULT CHARSET=utf8;|CREATE TABLE IF NOT EXISTS `runoob_tbl` (<br>&emsp;&emsp;&emsp;&emsp;`runoob_id` INT UNSIGNED AUTO_INCREMENT,<br>&emsp;&emsp;&emsp;&emsp;`runoob_title` VARCHAR(100) NOT NULL,<br>&emsp;&emsp;&emsp;&emsp;`runoob_author` VARCHAR(40) NOT NULL,<br>&emsp;&emsp;&emsp;&emsp;`runoob_test` NATIONAL CHAR(40),<br>&emsp;&emsp;&emsp;&emsp;`submission_date` DATE,<br>&emsp;&emsp;&emsp;&emsp;PRIMARY KEY (`runoob_id`)<br>) ENGINE = InnoDB DEFAULT CHARSET = utf8;|
|INSERT INTO t_order_item(order_id, user_id, status, creation_date) <br>values (1, 1, 'insert', '2017-08-08'), (2, 2, 'insert', '2017-08-08') ON DUPLICATE KEY UPDATE status = 'init';|INSERT  INTO t_order_item (order_id , user_id , status , creation_date)<br>VALUES<br>&emsp;&emsp;&emsp;&emsp;(1, 1, 'insert', '2017-08-08'),<br>&emsp;&emsp;&emsp;&emsp;(2, 2, 'insert', '2017-08-08')<br>ON DUPLICATE KEY UPDATE status = 'init';|
|INSERT INTO t_order SET order_id = 1, user_id = 1, status = convert(to_base64(aes_encrypt(1, 'key')) USING utf8)<br> ON DUPLICATE KEY UPDATE status = VALUES(status);|INSERT  INTO t_order SET order_id = 1,<br>&emsp;&emsp;&emsp;&emsp;user_id = 1,<br>&emsp;&emsp;&emsp;&emsp;status = CONVERT(to_base64(aes_encrypt(1 , 'key')) USING utf8)<br>ON DUPLICATE KEY UPDATE status = VALUES(status);|
|INSERT INTO t_order (order_id, user_id, status) SELECT order_id, user_id, status FROM t_order WHERE order_id = 1；|INSERT  INTO t_order (order_id , user_id , status) <br>SELECT order_id , user_id , status <br>FROM t_order<br>WHERE <br>&emsp;&emsp;&emsp;&emsp;order_id = 1;|
