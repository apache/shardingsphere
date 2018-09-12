grammar PostgreDropTable;
import PostgreKeyword, DataType, Keyword, PostgreBase,BaseRule,Symbol;

dropTable:
    DROP TABLE (IF EXISTS)? tableNames (CASCADE | RESTRICT)?
    ;