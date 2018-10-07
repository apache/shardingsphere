grammar SQLServerDropTable;

import SQLServerKeyword, SQLServerBase, BaseRule;

dropTable
    : DROP TABLE (IF EXISTS)? tableNames
    ;