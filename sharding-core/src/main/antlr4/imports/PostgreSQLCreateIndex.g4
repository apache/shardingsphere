grammar PostgreSQLCreateIndex;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule;

createIndex
    : CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName 
    ;
