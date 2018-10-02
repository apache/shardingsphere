grammar PostgreDropTable;

import PostgreKeyword, PostgreBase, BaseRule;

dropTable:
    DROP TABLE (IF EXISTS)? tableNames
    ;