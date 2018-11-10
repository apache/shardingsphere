grammar MySQLDropTable;

import MySQLKeyword, Keyword, BaseRule;

dropTable
    : DROP TEMPORARY? TABLE (IF EXISTS)? tableNames
    ;
