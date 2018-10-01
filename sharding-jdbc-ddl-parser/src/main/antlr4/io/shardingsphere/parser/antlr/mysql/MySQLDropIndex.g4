grammar MySQLDropIndex;
import MySQLKeyword, DataType, Keyword,MySQLBase,BaseRule,Symbol;

dropIndex:
    DROP INDEX (ONLINE | OFFLINE) indexName ON tableName
    ;