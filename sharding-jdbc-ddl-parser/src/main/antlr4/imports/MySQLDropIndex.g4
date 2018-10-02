grammar MySQLDropIndex;
import MySQLKeyword, DataType, Keyword,MySQLBase,BaseRule,Symbol;

dropIndex:
     dropIndexDef ON tableName
    (algorithmOption | lockOption)*
    ;

 dropIndexDef:
    DROP indexAndKey indexName
    ;