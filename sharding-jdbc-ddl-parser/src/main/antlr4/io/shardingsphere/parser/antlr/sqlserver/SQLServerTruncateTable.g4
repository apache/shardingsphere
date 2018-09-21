grammar SQLServerTruncateTable;
import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

truncateTable:
    TRUNCATE TABLE tableName (WITH LEFT_PAREN PARTITIONS LEFT_PAREN partitionExpressions RIGHT_PAREN RIGHT_PAREN)?
    ;