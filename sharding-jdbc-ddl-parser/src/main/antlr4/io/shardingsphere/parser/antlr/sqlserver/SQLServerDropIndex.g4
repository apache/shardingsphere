grammar SQLServerDropIndex;
import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

dropIndex:
    DROP INDEX indexName ON tableOrViewName (WITH LEFT_PAREN dropIndexOptions RIGHT_PAREN)?
    ;

dropIndexOptions:
    dropIndexOption (COMMA dropIndexOption)*
    ;

dropIndexOption:
    (MAXDOP EQ_OR_ASSIGN NUMBER)
    | (ONLINE EQ_OR_ASSIGN (ON | OFF))
    ;