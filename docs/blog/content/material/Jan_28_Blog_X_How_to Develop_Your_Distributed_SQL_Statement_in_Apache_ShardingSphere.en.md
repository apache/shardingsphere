+++

title = "How to Develop Your Distributed SQL Statement in Apache ShardingSphere"
weight = 30
chapter = true

+++

# How to Develop Your Distributed SQL Statement in Apache ShardingSphere

In the previous articles ["An Introduction to DistSQL"](https://opensource.com/article/21/9/distsql) and ["Integrating SCTL Into DistSQL’s RAL— Making Apache ShardingSphere Perfect for Database Management"](https://dzone.com/articles/integrating-sctl-into-distsqls-ral-making-apache-s), the Apache ShardingSphere Committers shared the motivations behind the development of DistSQL, explained its syntax system, and impressively showcased how you can use just one SQL to create a sharding table.

Today, to help you gain a better understanding of DistSQL and develop your own DistSQL syntax, our community author analyzes the design & development process of DistSQL and showcases how you can implement a brand new DistSQL grammar in four stages of the development life cycle (i.e. demand analysis, design, development & testing).

## What is DistSQL?

Like standard SQL, DistSQL or Distributed SQL is a built-in SQL language unique to ShardingSphere that provides incremental functional capabilities beyond standard SQL. Its design purpose is to empower resource and rule management with SQL operation capabilities. For more information about DistSQL, please read [“Build a data sharding service with DistSQL”](https://opensource.com/article/21/9/distsql).

## Why do you need DistSQL?

Redefining the boundary between middleware & database, and allowing developers to leverage [Apache ShardingSphere](https://opensource.com/article/21/9/distsql)as if they were using a database natively is DistSQL’s design goal.

Therefore, DistSQL provides a syntax structure and syntax validation system similar to standard SQL, to avoid a steep learning curve. Another advantage of DistSQL is its ability to manage resources and rules at the SQL level without configuration files.

## How to develop DistSQL?

### Preparation

**Toolkit**

1. ANTRL4 is a tool that translates your grammar to a parser/lexer in Java (or another target language). It’s used as a parser. You need to configure it before starting to develop your DistSQL. Want to get started with ANTLR 4? Please refer to this [ANTRL4 Concise Tutorial] (https://wizardforcel.gitbooks.io/antlr4-short-course/content/)

2. IntelliJ IDEA is an integrated development environment written in Java for developing computer software. You also need the plug-in [ANTLR v4] (https://plugins.jetbrains.com/plugin/7358-antlr-v4) to test the grammar rules defined by ANTRL4.

+ First, choose the right Test Rule:

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_1.png)

+ Input the statement to be verified in ANTLR Preview:

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_2.png)

You need to know the DistSQL execution process as well as the basics of synatics and plug-ins. The complete DistSQL execution process is truly complicated, but the awesome architecture of ShardingSphere allows developers to develop DistSQL features without having to pay attention to the whole process.

However, you still need to take care of the following core procedures:

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_3.png)

_Note: Here, we take data sharding as an example. Be aware that different features have different visitors._

###Practice

After understanding the execution process of DistSQL, you now can appreciate the practical case and learn how to develop your first DistSQL statement.

In the previous article [“An Introduction to DistSQL”](https://medium.com/nerd-for-tech/intro-to-distsql-an-open-source-more-powerful-sql-bada4099211),  the author showcased how you can leverage DistSQL to create a sharding with the statement `show sharding table rules`.

Now, we have a new request: How can you use a DistSQL statement to quickly query shard quantity of each table shard. The designed syntatic statement is as follows:

```
show sharding tables count [from schema] ;
```

* **Preparation**

1. MySQL contains databases and tabales for sharding.

2. Zookeeper is used as Registry Center

3. ShardingSphere-Proxy 5.0.0

* **Demonstration**

1. **Define the statement** 

Add the following statement definition into the file `src/main/antlr4/imports/sharding/RQLStatement.g4`. When it’s done, you can use ANTLR v4 to test it.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_4.png)

Please ensure that all keywords in that statement are defined. For example, `COUNT` is an undefined statement here, so you need to define it in `src/main/antlr4/imports/sharding/Keyword.g4'`.

After you define the statement, you also need to add it into the file `ShardingDistSQLStatement.g4`.  It's for the parsing router.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_5.png)

Now, it’s time to use  `shardingsphere-sharding-distsql-parser` to compile and generate the relevant objects.

2. **Parse the definition**

Then you also need to add a DistSQLStatement object of the definition in `shardingsphere-distsql-statement` to save the variable attributes of the statement. For example, the `schemaName` of the statement definition needs to be saved to the object `DistSQLStatement`.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_6.png)

Since ShardingSphere uses ANTLR’s Visitor mode, in terms of definition handling, it is required to rewrite `visitShowShardingTableCount` in `ShardingDistSQLStatementVisitor`. The purpose of this method is to create a `ShowShardingTablesCountStatement` object and save the related variable attributes to the object  `DistSQLStatement`.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_7.png)

`shardingsphere-distsql-statement` actually depends on `shardingsphere-sharding-distsql-parser`, so it's necessary to compile `shardingsphere-distsql-statement`.

3. **Handle data and return results**

Data handling is managed by the `execute`method of `Handler` or `Executor`, and `getRowData` returns the results. Different types of statement definitions focus on different things. For instance, when DistSQLResultSet is used as the result storage object, result data is assembled in the method 

Show the execution method and the `DistSQLResultSet` as shown in the below image:

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_8.png)

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_9.png)

In `ShardingTablesCountResultSet`, `init` gets and assembles data, and `getRowData` returna row data. `getType` is also obviously in the class. The method belongs to the `TypedSPI` interface, so `ShardingTablesCountResultSet` also needs to add `org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet` into the directory `src/main/resources/META-INF/services` of the current module to complete the SPI injection. The path and content are as follows:

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_10.png)

Now, you have successfully developed the feature of this statement definition.

4. **Finish your unit test and parse test**

When you complete the basic feature development, to ensure its continuous usability, you need to add test cases to the new class or method, and to complete parse tests for the new syntax. The following code block is the unit test of
`ShardingTablesCountResultSet`.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_11.png)

In addition to the unit test, you are also required to complete a parsing test for the grammar definition in `shardingsphere-parser-test`. The purpose is to parse the input DistSQL into a `DistSQLStatement` and then compare the parsed statement with your expected `TestCase` object. The steps are as follows:

a. Add the SQL you want to test in `src/main/resources/sql/supported/rql/show.xml`;

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_12.png)

b. Add a test case in `src/main/resources/case/rql/show.xml`;

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_13.png)

c. Add a `TestCase` object whose function is to save the result defined in the case

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_14.png)

d. Use the `SQLParserTestCases` class to load `TestCase`;

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_15.png)

e. Add the right `Assert` object to the `ShowRulesStatementAssert` judgment

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_16.png)

f. Execute the test method in `DistSQLParserParameterizedTest`. Now,the test comes to an end.

![](https://shardingsphere.apache.org/blog/img/DistSQL_Statement_Development_img_17.png)

Finally, you can execute the developed DistSQL verification function by using command line tools.

## Conclusion

DistSQL is one of the newest features released in version 5.0.0, and we will continue to improve it.
If you’re interested, you are welcome to develop the grammar system or provide awesome features to truly break the boundary between middleware and database.

### Author

Lan Chengxiang

![](https://shardingsphere.apache.org/blog/img/Lan_Chengxiang_Photo.png)

> SphereEx Middleware Development Engineer & Apache ShardingSphere Contributor. He focuses on designing and developing DistSQL.

### Open Source Project Links

[ShardingSphere Github](https://github.com/apache/shardingsphere)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack Channel] (https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
