grammar SQLServerDropTable;

import SQLServerKeyword, Keyword, SQLServerBase, BaseRule;

dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;
