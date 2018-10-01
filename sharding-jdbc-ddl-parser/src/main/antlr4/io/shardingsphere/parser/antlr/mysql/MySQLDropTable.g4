grammar MySQLDropTable;
import MySQLKeyword, DataType, Keyword,MySQLBase,BaseRule,Symbol;

dropTable:
    DROP TEMPORARY? TABLE (IF EXISTS)?
    tableName (COMMA tableName)*
    ;