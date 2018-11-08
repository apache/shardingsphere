+++
toc = true
title = "Parse Engine"
weight = 1
+++

SQL is relatively simple compared to other programming languages, but it is still a complete set of programming language, 
so there is no essential difference between parsing SQL grammar and parsing other languages (Java, C and Go, etc.).

## Abstract Syntax Tree

The parsing process can be divided into lexical parsing and syntactic parsing. 
Lexical parser is used to divide SQL into indivisible atomic signs, i.e., Token. 
According to the dictionary provided by different database dialect, it is categorized into keyword, expression, literal and operator. 
SQL is then converted into abstract syntax tree by syntactic parser.

For example, the following SQL:

```sql
SELECT id, name FROM t_user WHERE status = 'ACTIVE' AND age > 18
```

Its abstract grammar tree after parsing is as:

![SQL AST](http://shardingsphere.jd.com/document/current/img/sharding/sql_ast.png)

To better understand, the Token of abstract syntax tree keywords is shown in green; that of variables is shown in red; what’s to be further divided is shown in grey.
At last, through traversing the abstract syntax tree, the context needed by sharding is extracted and the place that may need to be rewritten is also marked out. 
Parsing context for the use of sharding includes select items, table information, sharding conditions, auto-increment primary key information, Order By information, Group By information, and pagination information (Limit, Rownum and Top). 
One-time parsing process of SQL is irreversible, each Token is parsed according to the original order of SQL with a high performance. 
Considering similarities and differences between SQL of all kinds of database dialect, SQL dialect dictionaries of different types of databases are provided in the parsing module.

## SQL Parsing Engine

As the core of database sharding and table sharding, SQL parser takes the performance and compatibility as its most important index. 
Common SQL parser includes `fdb`, `jsqlparser` and `Druid`. As the predecessor of Sharding-Sphere, Druid was used as the SQL parser of Sharding-Sphere before 1.4.x version. 
As tested in practice, its performance exceeds other parsers a lot.
Sharding-Sphere has adopted fully self-developed SQL parsing engine since its 1.5.x version. 
Because of different purposes, Sharding-Sphere does not need to transform SQL into a totally abstract syntax tree or traverse twice through visitor pattern. 
Using half parsing method, it only extracts the context required by data sharding, so the performance and compatibility of SQL parsing is further improved.

In the latest 3.x version, Sharding-Sphere tries to adopts `ANTLR` as the SQL parsing engine, and plans to replace the former parsing engine according to the order of `DDL -> TCL -> DAL –> DCL -> DML –>DQL`. 
Hoping for a better compatibility with SQL, we use ANTLR in the parsing engine of Sharding-Sphere. 
Though complex expressions, recursions, sub-queries and other sentences are not focused by the sharding core of Sharding-Sphere, they can influence the friendliness to understand SQL. 
After testing in actual cases, the performance of ANTLR is about 3 times slower than the self-developed parsing engine when parsing SQL. 
To compensate for this gap, Sharding-Sphere will use the SQL parsing tree of PreparedStatement to put in the cache. 
Therefore, PreparedStatement is recommended to be used as the pre-compile method to improve the performance.

Sharding-Sphere will provide options to include both of the parsing engines and give users the right to choose between the competitiveness and performance of SQL parsing.