grammar MySQLCreateIndex;

import MySQLKeyword, Keyword, MySQLBase, BaseRule, Symbol;

createIndex:
    CREATE (UNIQUE | FULLTEXT | SPATIAL)? INDEX indexName indexType? ON tableName
    ;
