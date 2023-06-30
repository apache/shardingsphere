+++ 
title = "SQL Parse Format Function — A Technical Deep Dive by Apache ShardingSphere"
weight = 40
chapter = true 
+++

Complicted SQL statements are some of the most common problems that data scientists and engineers encounter. For example, can you comprehend at first glance the complex SQL statement below?

```sql
select a.order_id,a.status,sum(b.money) as money from t_order a inner join (select c.order_id as order_id, c.number * d.price as money from t_order_detail c inner join t_order_price d on c.s_id = d.s_id) b on a.order_id = b.order_id where b.money > 100 group by a.order_id
```

How about formatting it? Is it easier to understand the formatted formatted version below?

```sql
SELECT a . order_id , a . status , SUM(b . money) AS money
FROM t_order a INNER JOIN
(
SELECT c . order_id AS order_id, c . number * d . price AS money
FROM t_order_detail c INNER JOIN t_order_price d ON c . s_id = d . s_id
) b ON a . order_id = b . order_id
WHERE
b . money > 100
GROUP BY a . order_id;
```

The first step to parse such a complex SQL is always formatting, and then its SQL semantics can be parsed based on the formatted content. SQL Formatter is, therefore, one of the essential functions of for any database software.

Accordingly, [Apache ShardingSphere](https://shardingsphere.apache.org/) now offers a SQL formatting tool called SQL Parse Format that depends on ShardingSphere’s SQL dialect parser.

**SQL Parse Format is an important function of the ShardingSphere Parser Engine, and also lays the foundation for ShardingSphere’s SQL Audit (TODO).** This article offers a deep dive into the SQL Parse Format function:

- What’s its core concept?
- How you can use it?
- How can you develop SQL Parse Format?

## Parser Engine

To begin, we need to introduce more about Apache ShardingSphere’s Parser Engine because SQL Parse Format is a unique and relatively independent function of the parser engine.

Apache ShardingSphere developed the parser engine to extract key information in SQL, such as fields of data shards and rewritten columns for data encryption. So far, Apache ShardingSphere’s parser engine has undergone three iterations.

The initial parser engine leveraged [Druid](https://druid.apache.org/) as its SQL parser and performed quite well before ShardingSphere Version 1.4.x.

Later the ShardingSphere community decided to develop its second-generation parser engine on its own. Since the use purpose was changed, ShardingSphere adopted another approach to comprehend SQL: only the contextual information that data sharding needs was extracted, without generating a parse tree or a secondary traversal, to improve performance and compatibility.

Currently, the third generation of ShardingSphere Parser Engine uses [ANTLR](https://www.antlr.org/) as the parse tree generator and then extracts the contextual information by doing a secondary tree traversal. It is substantially compatible with more SQL dialects, which further accelerates developing other functions in Apache ShardingSphere.

In version 5.0.x, ShardingSphere developers further enhanced the performance of the newest parser engine by changing its tree traversal method from Listener to Visitor and adding parsing results cache for pre-compiled SQL statements.

The implementation of SQL Parse Format is attributable to the new parser engine. Next, let’s take a look at SQL Parse Format function.

## SQL Parser Format
SQL Parse Format is used to format SQL statements. Additionally, SQL Parse Format function will be used in SQL Audit in the future to provide users with viewing SQL history, displaying formatted SQL with reports, or further analyzing or processing SQL.

For instance, each part of the following SQL formatted by SQL Parse Format becomes clearer with wrapping and keywords in all caps:

```sql
select age as b, name as n from table1 join table2 where id = 1 and name = 'lu';
-- After Formatting
SELECT age AS b, name AS n
FROM table1 JOIN table2
WHERE 
        id = 1
        and name = 'lu';
```

So far, we have covered the basics of the SQL Parse Format.

> Next, let’s answer the question: what is the concept of SQL Parse Format?

How a SQL statement is formatted in Apache ShardingSphere? Take the following SQL as an example:

```sql
select order_id from t_order where status = 'OK'
```

1. Apache ShardingSphere uses ANTLR4 as its parser engine generator. First, we need to follow the ANTLR4 method to define the syntax of select in the .g4 file (take MySQL as an example).

```
simpleSelect
    : SELECT ALL? targetList? intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
    | SELECT distinctClause targetList intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
    | valuesClause
    | TABLE relationExpr
    ;
 ```
 
2. We can use IDEA’s ANTLR4 plugin to easily view the syntax tree of the SQL statement.

For more information of ANTLR4 , please refer to: [https://plugins.jetbrains.com/plugin/7358-antlr-v4](https://plugins.jetbrains.com/plugin/7358-antlr-v4.).
![Image description](https://miro.medium.com/max/700/1*EiWkP_kYN3sLOH4qsPonDA.jpeg)

ANTLR4 can compile the syntax file we define: it first performs lexical analysis on the SQL statement, splits it into indivisible parts, namely tokens, and divides these tokens into keywords, expressions, according to the dictionary values of different databases.

For example, in the image above, we get the keywords `SELECT`, `FROM`, `WHERE`, = and the variables `order_id`, `t_order`, `status`, `OK`.

3. Then ANTLR4 converts the output of the parser engine into the syntax tree as shown in the image above.

Based on the source code of Apache ShardingSphere, the above-mentioned process is reproduced as follows.
```java
String sql = "select order_id from t_order where status = 'OK'";
CacheOption cacheOption = new CacheOption(128, 1024L, 4);
SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption, false);
ParseContext parseContext = parserEngine.parse(sql, false);
```

4. The SQL Parser Engine of Apache ShardingSphere encapsulates and abstracts the ANTLR4 parser: it loads the SQL dialect parser through an SPI. Users can also extend data dialects through extension points of SPI. In addition, ShardingSphere adds a cache mechanism internally to improve performance. Take a look at the relevant code for parsing as follows:
```java
public ParseContext parse(final String sql) {
    ParseASTNode result = twoPhaseParse(sql);
    if (result.getRootNode() instanceof ErrorNode) {
        throw new SQLParsingException("Unsupported SQL of `%s`", sql);
    }
    return new ParseContext(result.getRootNode(), result.getHiddenTokens());
}

private ParseASTNode twoPhaseParse(final String sql) {
    DatabaseTypedSQLParserFacade sqlParserFacade = DatabaseTypedSQLParserFacadeRegistry.getFacade(databaseType);
    SQLParser sqlParser = SQLParserFactory.newInstance(sql, sqlParserFacade.getLexerClass(), sqlParserFacade.getParserClass(), sqlCommentParseEnabled);
    try {
        ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.SLL);
        return (ParseASTNode) sqlParser.parse();
    } catch (final ParseCancellationException ex) {
        ((Parser) sqlParser).reset();
        ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.LL);
        try {
            return (ParseASTNode) sqlParser.parse();
        } catch (final ParseCancellationException e) {
            throw new SQLParsingException("You have an error in your SQL syntax");
        }
    }
}
```

`twoPhaseParse` is the core of the parser. First, it will be loaded into the correct parser class according to the database type, and then a parser instance of ANTLR4 will be generated due to the reflection mechanism. Then, ANTLR4 provides two parsing methods: fast parsing is performed first, and if it fails, regular parsing will be performed. Users can obtain parsing results of most SQL statements via quick parsing, improving parsing performance as well. After parsing, we get the parse tree.

So how does Apache ShardingSphere get the formatted SQL statement from the parse tree?

In fact, ShardingSphere uses the `Visitor` method. ANTLR4 provides two ways to access syntax trees: Listener and `Visitor`. ShardingSphere chooses the latter to access syntax trees. The code below shows how to get formatted SQL from the syntax tree:
```java
SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "FORMAT", new Properties());
String result = visitorEngine.visit(parseContext);
```
Apache ShardingSphere’s `SQLVisitorEngine` also abstracts and encapsulates various dialect visitors. The core method is shown below:

```java
public <T> T visit(final ParseContext parseContext) {
    ParseTreeVisitor<T> visitor = SQLVisitorFactory.newInstance(databaseType, visitorType, SQLVisitorRule.valueOf(parseContext.getParseTree().getClass()), props);
    T result = parseContext.getParseTree().accept(visitor);
    appendSQLComments(parseContext, result);
    return result;
}
```
At first, in terms of the above-mentioned `Visitor` method, the visitor to be used is decided according to the database type and the type of the visitor, and the visitor is also internally instantiated by the reflection mechanism. Currently, `visitorType` supports two methods: `FORMAT` and `STATEMENT`. The latter is commonly used by Apache ShardingSphere and can convert SQL into `Statement` information, extract relevant context information, and serve the features such as data sharding. In fact, this is the only difference between SQL Parse Format and other ordinary parser engine functions.

Next, let’s still take the SQL statement as an example and provide specific code to show how `Visitor` formats it.

`MySQLFormatSQLVisitor` is used to visit SQL. Based on the `DEBUG` code, we can clearly see the execution path of this visit as shown in the figure below. Visitor traverses all parts of the syntax tree, and ANTLR4 generates default methods for visiting each node according to the defined grammar rules. Apache ShardingSphere leverages key methods and successfully develops complete the SQL formatting function.
![Image description](https://miro.medium.com/max/700/1*xjjACczbInC-K4t8EX-pEw.jpeg)

The following code can help us better understand how `Visitor` can format SQL.

When the `Visitor` traverses to `select`, the `Visitor` will format it first, and then visit `projection`. The internal formatting of `projection` will be further implemented through the `visitProjections` method.

Empty lines are handled before accessing `from`. The object instantiated by the `Visitor` maintains a `StringBuilder` to store the formatted result. Since the parser and visitor of each SQL are newly-created instantiated objects, there are no thread issues. After the final traversal, Apache ShardingSphere outputs the result in `StringBuilder`, and then we get formatted SQL.

```java
public String visitQuerySpecification(final QuerySpecificationContext ctx) {
    formatPrint("SELECT ");
    int selectSpecCount = ctx.selectSpecification().size();
    for (int i = 0; i < selectSpecCount; i++) {
        visit(ctx.selectSpecification(i));
        formatPrint(" ");
    }
    visit(ctx.projections());
    if (null != ctx.fromClause()) {
        formatPrintln();
        visit(ctx.fromClause());
    }
    if (null != ctx.whereClause()) {
        formatPrintln();
        visit(ctx.whereClause());
    }
    if (null != ctx.groupByClause()) {
        formatPrintln();
        visit(ctx.groupByClause());
    }
    if (null != ctx.havingClause()) {
        formatPrintln();
        visit(ctx.havingClause());
    }
    if (null != ctx.windowClause()) {
        formatPrintln();
        visit(ctx.windowClause());
    }
    return result.toString();
}
```

Now, based on the process analysis and code snippets above, you can understand the principle of SQL Parse Format.

## User Guide for SQL Parse Format
You may find it’s easy to use the SQL Parse Format function in Apache ShardingSphere as long as you know its principle.

As for Java applications, users only need to add dependencies and call the API.

- Add the Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-parser-engine</artifactId>
    <version>${project.version}</version>
</dependency>

<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-parser-mysql</artifactId>
    <version>${project.version}</version>
</dependency>
```

- Call the API
```java
public static void main(String[] args) {
    String sql = "select order_id from t_order where status = 'OK'";
    CacheOption cacheOption = new CacheOption(128, 1024L, 4);
    SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption, false);
    ParseContext parseContext = parserEngine.parse(sql, false);
    SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "FORMAT", new Properties());
    String result = visitorEngine.visit(parseContext);
    System.out.println(result);
}
```

- Parameters Supported by Properties
![Image description](https://miro.medium.com/max/550/1*1Ft7G0EKkayVy5vrbcsDBg.png)

You can also use DistSQL in ShardingSphere-Proxy to perform operations on the SQL Parse Format function:
```
mysql> FORMAT select order_id from t_user where status = 'OK';
+-----------------------------------------------------+
| formatted_result                                    |
+-----------------------------------------------------+
| SELECT order_id
FROM t_user
WHERE
        status = 'OK'; |
+-----------------------------------------------------+
```

In terms of the above-mentioned `Statement` mode, it can also enable users to easily view the results of `SQLStatement` converted from the SQL.
```
mysql> parse SELECT id, name FROM t_user WHERE status = 'ACTIVE' AND age > 18;
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| parsed_statement     | parsed_statement_detail                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| MySQLSelectStatement | {"projections":{"startIndex":7,"stopIndex":14,"distinctRow":false,"projections":[{"column":{"startIndex":7,"stopIndex":8,"identifier":{"value":"id","quoteCharacter":"NONE"}}},{"column":{"startIndex":11,"stopIndex":14,"identifier":{"value":"name","quoteCharacter":"NONE"}}}]},"from":{"tableName":{"startIndex":21,"stopIndex":26,"identifier":{"value":"t_user","quoteCharacter":"NONE"}}},"where":{"startIndex":28,"stopIndex":63,"expr":{"startIndex":34,"stopIndex":63,"left":{"startIndex":34,"stopIndex":50,"left":{"startIndex":34,"stopIndex":39,"identifier":{"value":"status","quoteCharacter":"NONE"}},"right":{"startIndex":43,"stopIndex":50,"literals":"ACTIVE"},"operator":"\u003d","text":"status \u003d \u0027ACTIVE\u0027"},"right":{"startIndex":56,"stopIndex":63,"left":{"startIndex":56,"stopIndex":58,"identifier":{"value":"age","quoteCharacter":"NONE"}},"right":{"startIndex":62,"stopIndex":63,"literals":18},"operator":"\u003e","text":"age \u003e 18"},"operator":"AND","text":"status \u003d \u0027ACTIVE\u0027 AND age \u003e 18"}},"unionSegments":[],"parameterCount":0,"commentSegments":[]} |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

For more [DistSQL](https://opensource.com/article/21/9/distsql) functions, please refer to the documentation: [https://shardingsphere.apache.org/document/current/cn/concepts/distsql/](https://shardingsphere.apache.org/document/current/cn/concepts/distsql/)

## Conclusion

Currently, Apache ShardingSphere’s Format function only supports [MySQL](https://www.mysql.com/). After understanding its concept and how to use it, if you’re interested, you are welcome to contribute to developing the SQL Parse Format function.

### Apache ShardingSphere Open Source Project Links:

[ShardingSphere Github
](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)
[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

## Author

**Chen Chuxin**
![Image description](https://miro.medium.com/max/634/1*smrIU5STVJsJRais0_Tghg.png)

> SphereEx Middleware Engineer & Apache ShardingSphere Committer

> Currently, he devotes himself to developing the kernel module of Apache ShardingSphere.
