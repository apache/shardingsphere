grammar PostgreCreateIndex;

import PostgreKeyword, PostgreBase, BaseRule;

createIndex:
    CREATE UNIQUE? INDEX CONCURRENTLY? ((IF NOT EXISTS)? indexName)? ON tableName 
    ;
