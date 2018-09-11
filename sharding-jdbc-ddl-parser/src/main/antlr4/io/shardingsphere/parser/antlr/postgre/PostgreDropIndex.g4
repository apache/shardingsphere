grammar PostgreDropIndex;
import PostgreKeyword, DataType, Keyword, PostgreBase,BaseRule,Symbol;

dropIndex:
    DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexNames (CASCADE | RESTRICT)?
    ;