grammar PostgreDropTable;

import PostgreKeyword, Keyword,PostgreBase, BaseRule;

dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;