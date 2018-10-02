grammar MySQLCreateIndex;
import MySQLKeyword, DataType, Keyword,MySQLBase,BaseRule,Symbol;

createIndex:
    CREATE (UNIQUE | FULLTEXT | SPATIAL)? INDEX indexName
    indexType?
    ON tableName keyParts
    ;
 
