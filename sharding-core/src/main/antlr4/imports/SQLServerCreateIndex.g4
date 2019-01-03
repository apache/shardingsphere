grammar SQLServerCreateIndex;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

createIndex
    : CREATE UNIQUE? (CLUSTERED | NONCLUSTERED)? INDEX indexName ON tableName columnList
    ;
