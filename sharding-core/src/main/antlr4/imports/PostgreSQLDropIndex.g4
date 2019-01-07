grammar PostgreSQLDropIndex;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule;

dropIndex
    : DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexNames
    ;
