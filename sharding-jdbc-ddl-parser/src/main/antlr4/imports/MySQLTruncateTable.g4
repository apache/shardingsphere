grammar MySQLTruncateTable;
import MySQLKeyword, DataType, Keyword,MySQLBase,BaseRule,Symbol;

truncateTable:
     TRUNCATE TABLE? tableName
     ;
