grammar SQLServerTruncate;
import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

truncate:
    TRUNCATE TABLE tableName (WITH LEFT_PAREN PARTITIONS LEFT_PAREN partitionExpressions RIGHT_PAREN RIGHT_PAREN)?
    ;