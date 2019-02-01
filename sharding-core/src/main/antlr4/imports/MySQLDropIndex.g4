grammar MySQLDropIndex;

import MySQLKeyword, Keyword, BaseRule;

dropIndex
    : DROP INDEX (ONLINE | OFFLINE)? indexName ON tableName
    ;
