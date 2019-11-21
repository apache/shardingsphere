grammar PostgreSQLDropTable;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule;

dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;
