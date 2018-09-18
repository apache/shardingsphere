grammar SQLServerDropTable;
import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

dropTable:
    DROP TABLE (IF EXISTS)? tableNames
    ;