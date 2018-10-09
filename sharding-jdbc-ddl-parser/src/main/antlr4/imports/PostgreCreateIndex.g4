grammar PostgreCreateIndex;

import PostgreKeyword, Keyword, PostgreBase, BaseRule;

createIndex
    : CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName 
    ;
