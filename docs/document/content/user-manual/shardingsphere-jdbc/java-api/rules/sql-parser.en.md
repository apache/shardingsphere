+++
title = "SQL Parser"
weight = 6
+++

## Root Configuration

Class：org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration

Attributes：

| *name*                     | *DataType*      | *Description*                               |
|----------------------------|-----------------|---------------------------------------------|
| sqlCommentParseEnabled (?) | boolean         | Whether to parse SQL comments               |
| parseTreeCache (?)         | CacheOption     | Parse syntax tree local cache configuration |
| sqlStatementCache (?)      | CacheOption     | sql statement local cache configuration     |

## Cache option Configuration

Class：org.apache.shardingsphere.sql.parser.api.CacheOption

Attributes：

| *name*           | *DataType*   | *Description*                                                                              | *Default Value*                                                                                                         |
|------------------|--------------|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| initialCapacity  | int          | Initial capacity of local cache                                                            | parser syntax tree local cache default value 128, SQL statement cache default value 2000                                |
| maximumSize(?)   | long         | Maximum capacity of local cache                                                            | The default value of local cache for parsing syntax tree is 1024, and the default value of sql statement cache is 65535 |

